package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.api.IGiifuShockable;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.giifu.GiifuSleepingStateBlock;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.CurseSpaceSkillGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Random;

public class GiifuHumanEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    protected static final RawAnimation SPECIAL_ATTACK = RawAnimation.begin().thenPlay("special_attack");
    protected static final RawAnimation DEATH = RawAnimation.begin().thenPlay("death");
    public Random random;

    private int specialAttackCooldown = 0;

    private BlockPos sleepingPos = BlockPos.ZERO;

    private boolean isFlying = false;
    private static final double FLIGHT_TRIGGER_HEIGHT = 4.0D; // 触发飞行高度差

    /** 石像位置，ZERO 表示无石像（指令刷出） */
    private BlockPos statuePos = BlockPos.ZERO;
    /** 拉回检测冷却，防止每 tick 都跑一遍 */
    private int pullBackCd = 0;

    private int shockCooldown = 0;          // 冲击波冷却
    private static final int SHOCK_COOL     = 300; // 15 秒
    private static final float SHOCK_DAMAGE = 10.0F; // 基础伤害
    private static final double SHOCK_RANGE = 7.0D;   // 半径 7 格

    private final Goal floatGoal        = new FloatGoal(this);
    private final Goal strollGoal       = new RandomStrollGoal(this, 0.6D);

    /** 引力波冷却 **/
    private int gravWaveCd = 0;
    private static final int GRAV_WAVE_COOL = 200;        // 10 s
    private static final double GRAV_WAVE_RANGE = 20.0D;  // 20 格
    private static final int LEVITATION_TIME = 100;       // 5 s
    private static final ParticleType<DustParticleOptions> SCARLET = ParticleTypes.DUST;  // 猩红

    // 在 Random random; 附近添加
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.kamen_rider_boss_you_and_me.giifu_human"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    ).setDarkenScreen(true); // 可选：使屏幕变暗

    public GiifuHumanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        this.random = new Random();
        this.statuePos = BlockPos.ZERO;
        this.sleepingPos = BlockPos.ZERO;
        this.isFlying = false;

        // 设置Boss血条名称
        this.bossEvent.setName(Component.translatable("entity.kamen_rider_boss_you_and_me.giifu_human"));

        // 如果有飞行相关的特殊导航需求，可以在这里初始化
        // this.navigation = this.isFlying ? new FlyingPathNavigation(this, level) : new GroundPathNavigation(this, level);
    }
    
    // 设置实体属性
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D) // 增加地面移动速度
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 40.0D);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    public void setSleepingPos(BlockPos pos) {
        this.sleepingPos = pos.immutable();
    }

    @Override
    public void die(DamageSource source) {
        // 移除所有玩家从Boss事件
        if (!level().isClientSide) {
            this.bossEvent.removeAllPlayers();
        }

        // 复活逻辑（只在服务端执行且石像存在时）
        if (!level().isClientSide && !statuePos.equals(BlockPos.ZERO)) {
            BlockState state = level().getBlockState(statuePos);
            if (state.getBlock() instanceof GiifuSleepingStateBlock) {
                // 石像存在 → 必定复活
                GiifuHumanEntity newEntity = ModEntityTypes.GIIFU_HUMAN.get().create(level());
                if (newEntity != null) {
                    newEntity.setPos(statuePos.getX() + 0.5, statuePos.getY() + 1, statuePos.getZ() + 0.5);
                    newEntity.setStatuePos(statuePos);
                    level().addFreshEntity(newEntity);
                }
            }
        }

        // 必须调用父类方法以确保掉落物生成等正常死亡流程
        super.die(source);
    }

    public void setStatuePos(BlockPos pos) { this.statuePos = pos.immutable(); }
    public BlockPos getStatuePos() { return statuePos; }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        // 战斗
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new SpecialAttackGoal(this));

        // 地面移动 & 观察
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // 目标选择
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(2, new CurseSpaceSkillGoal(this));
        // 移除主动攻击玩家的目标选择器，基夫不主动攻击玩家
    }
    
    // 自定义特殊攻击目标
    private static class SpecialAttackGoal extends Goal {
        private final GiifuHumanEntity giifu;
        private int attackTick = 0;
        
        public SpecialAttackGoal(GiifuHumanEntity giifu) {
            this.giifu = giifu;
        }
        
        @Override
        public boolean canUse() {
            return giifu.getTarget() != null && giifu.specialAttackCooldown <= 0;
        }
        
        @Override
        public void start() {
            this.attackTick = 40;
            giifu.specialAttackCooldown = 100;
        }
        
        @Override
        public void tick() {
            this.attackTick--;
            
            if (this.attackTick <= 0 && giifu.getTarget() != null) {
                giifu.performSpecialAttack(giifu.getTarget());
            }
        }
        
        @Override
        public boolean canContinueToUse() {
            return this.attackTick > 0 && giifu.getTarget() != null;
        }
    }
    
    // 执行特殊攻击
    public void performSpecialAttack(LivingEntity target) {
        // 播放特殊攻击动画
        this.level().broadcastEntityEvent(this, (byte)4);
        
        // 对目标造成高额伤害
        if (target != null && !this.level().isClientSide) {
            target.hurt(this.damageSources().mobAttack(this), 30.0F);
            
            // 添加击退效果
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            target.push(dx * 2.0D, 1.0D, dz * 2.0D);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // 更新Boss血条
        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
        
        // 只在非和平模式下执行攻击逻辑
        if (this.level().getDifficulty() != Difficulty.PEACEFUL &&
                !(this.getTarget() instanceof Player && ((Player)this.getTarget()).isCreative())) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceTo(target) < 2.0D) {
                float damage = (float)this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
                target.hurt(this.damageSources().mobAttack(this), damage);
            }
        }

        // 粒子效果
        if (isFlying && level() instanceof ServerLevel srv) {
            srv.sendParticles(
                    new DustParticleOptions(new Vector3f(0.86F, 0.08F, 0.24F), 1.5F),
                    getX(), getY() - 0.5D, getZ(),
                    3, 0.2D, 0.1D, 0.2D, 0.1D);
        }

        LivingEntity target = getTarget();
        if (target != null) {
            double heightDiff = target.getY() - this.getY();
            double groundY = level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) getX(), (int) getZ());
            double offGround = getY() - groundY;

            // 飞行逻辑
            if (!isFlying && heightDiff >= FLIGHT_TRIGGER_HEIGHT) {
                setFlying(true);
            } else if (isFlying && heightDiff < 2.0D) {
                // 简化：只要目标在下面不到2格就降落
                setFlying(false);
            }

            // 飞行移动
            if (isFlying) {
                performFlightMovement(target);
            }
        }

        // 添加：没有目标时自动降落
        if (isFlying && target == null) {
            double groundY = level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) getX(), (int) getZ());
            if (getY() - groundY < 5.0D) {
                setFlying(false);
            }
        }

        // 引力波技能
        if (gravWaveCd-- <= 0 && target != null) {
            LivingEntity gravTarget = findGravTarget();
            if (gravTarget != null) {
                performGravWave(gravTarget);
                gravWaveCd = GRAV_WAVE_COOL;
            }
        }

        // 冲击波技能
        if (shockCooldown-- <= 0 && target != null &&
                distanceToSqr(target) <= SHOCK_RANGE * SHOCK_RANGE) {
            performShockWave();
            shockCooldown = SHOCK_COOL;
        }

        // 其他冷却
        if (specialAttackCooldown > 0) specialAttackCooldown--;
        if (--pullBackCd <= 0) {
            pullBackCd = 10;
            if (!statuePos.equals(BlockPos.ZERO) && !level().isClientSide) {
                double dist = distanceToSqr(statuePos.getX() + 0.5, statuePos.getY() + 0.5, statuePos.getZ() + 0.5);
                if (dist > 20 * 20) {
                    teleportTo(statuePos.getX() + 0.5, statuePos.getY() + 1, statuePos.getZ() + 0.5);
                    refreshDimensions();
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float)this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
        return target.hurt(this.damageSources().mobAttack(this), damage);
    }
    
    public void hurtNearbyEntities() {
        if (this.level().isClientSide()) return;

        // 获取攻击伤害属性，如果不存在则使用默认值
        double damage = this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());

        // 扫描周围实体
        for (LivingEntity entity : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(2.0D),
                e -> e != this && !(e instanceof Player && ((Player)e).isCreative()) && !isGiifuDisciple(e))
        ) {
            entity.hurt(this.damageSources().mobAttack(this), (float)damage);
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isFlying()) {
            // 飞行时的移动逻辑
            if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
                this.moveRelative(this.getSpeed(), travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            }
        } else {
            // 地面移动逻辑
            super.travel(travelVector);
        }
    }

    @Override
    public boolean onClimbable() {
        return !this.isFlying() && super.onClimbable();
    }

    @Override
    public boolean isNoGravity() {
        return this.isFlying() || super.isNoGravity();
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        if (this.isFlying == flying) return;
        this.isFlying = flying;

        // 飞行时停止地面导航
        if (flying) {
            this.getNavigation().stop();
        }

        // 设置无重力状态
        this.setNoGravity(flying);
    }

    // 飞行移动方法
    private void performFlightMovement(LivingEntity target) {
        if (target == null) return;

        Vec3 direction = target.position().subtract(this.position()).normalize();
        double speed = 0.12D; // 增加飞行速度

        // 设置飞行速度
        this.setDeltaMovement(
                direction.x * speed,
                // 保持一定高度，稍微向上飞
                Math.max(0.1D, direction.y * speed * 0.6D),
                direction.z * speed
        );
    }

    @Nullable
    private LivingEntity findGravTarget() {
        return level().getEntitiesOfClass(LivingEntity.class,
                        new AABB(blockPosition()).inflate(GRAV_WAVE_RANGE),
                        e -> e != this && e.isAlive() && !(e instanceof GiifuHumanEntity) && !isGiifuDisciple(e))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }
    
    /**
     * 判断实体是否是基夫门徒或基夫德莫斯
     */
    private boolean isGiifuDisciple(LivingEntity entity) {
        String className = entity.getClass().getName().toLowerCase();
        return !(entity instanceof Player) && 
               (className.contains("giifudemos") || className.contains("gifftarian") || className.contains("storious"));
    }

    private void performGravWave(LivingEntity target) {
        if (level().isClientSide) return;

        // 1. 漂浮效果
        target.addEffect(new MobEffectInstance(MobEffects.LEVITATION,
                LEVITATION_TIME, 3, false, false, true));

        // 2. 水平拉回：每 tick 微移
        Vec3 toMe = position().subtract(target.position()).normalize();
        double pullSpeed = 0.18D;
        target.setDeltaMovement(target.getDeltaMovement()
                .add(toMe.x * pullSpeed, 0, toMe.z * pullSpeed));

        // 3. 猩红粒子环
        if (level() instanceof ServerLevel srv) {
            for (int i = 0; i < 20; i++) {
                double angle = 2 * Math.PI * i / 20;
                double px = target.getX() + Math.cos(angle) * 1.2D;
                double pz = target.getZ() + Math.sin(angle) * 1.2D;
                double py = target.getY() + target.getBbHeight() * 0.5D;

                srv.sendParticles(new DustParticleOptions(new Vector3f(0.86F, 0.08F, 0.24F), 1.5F),
                        px, py, pz, 0, 0, 0, 0, 1);
            }
        }

        // 4. 音效
        level().playSound(null, blockPosition(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE,
                1.5F, 0.8F);
    }

    private void performShockWave() {
        Level level = this.level();
        if (level.isClientSide) return;

        // 1. 范围伤害（所有 LivingEntity）
        AABB box = new AABB(blockPosition()).inflate(SHOCK_RANGE);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e == this || isGiifuDisciple(e)) continue;
            // 基础伤害
            e.hurt(damageSources().mobAttack(this), SHOCK_DAMAGE);

            // 2. 只对玩家做"解除变身"判定
            if (e instanceof Player player) {
                // 2-1 骑士玩家实现接口 → 解除变身
                if (player instanceof IGiifuShockable knight) {
                    if (knight.onGiifuShockWave(player)) {
                        // 成功解除后发聊天
                        player.displayClientMessage(
                                Component.translatable("msg.kamen_rider.giifu_shock_break"),
                                true);
                    }
                }
                // 2-2 非骑士 / 未变身 → 仅伤害，无额外效果
            }
        }

        // 3. 客户端特效（粒子 + 音效）
        if (level instanceof ServerLevel srv) {
            srv.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    getX(), getY() + 1, getZ(), 1, 0, 0, 0, 1);
            srv.playSound(null, blockPosition(),
                    SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE,
                    1.0f, 0.8f);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        // 减少受到的伤害
        float modifiedDamage = damage;
        if (!(source.getEntity() instanceof Player)) {
            modifiedDamage *= 0.5F; // 对非玩家实体伤害减半
        }
        
        return super.hurt(source, modifiedDamage);
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::idleAnimController));
        controllers.add(new AnimationController<>(this, "attack_controller", 5, this::attackAnimController));
        controllers.add(new AnimationController<>(this, "special_attack_controller", 5, this::specialAttackAnimController));
        controllers.add(new AnimationController<>(this, "death_controller", 5, this::deathAnimController));
    }
    
    protected <E extends GiifuHumanEntity> PlayState idleAnimController(final AnimationState<E> event) {
        if (this.isDeadOrDying()) {
            return PlayState.STOP;
        }
        
        if (event.isMoving()) {
            return event.setAndContinue(WALK);
        } else {
            return event.setAndContinue(IDLE);
        }
    }
    
    protected <E extends GiifuHumanEntity> PlayState attackAnimController(final AnimationState<E> event) {
        if (this.swinging && !this.isDeadOrDying()) {
            return event.setAndContinue(ATTACK);
        }
        return PlayState.STOP;
    }
    
    protected <E extends GiifuHumanEntity> PlayState specialAttackAnimController(final AnimationState<E> event) {
        if (this.specialAttackCooldown < 60 && this.specialAttackCooldown > 0 && !this.isDeadOrDying()) {
            return event.setAndContinue(SPECIAL_ATTACK);
        }
        return PlayState.STOP;
    }
    
    protected <E extends GiifuHumanEntity> PlayState deathAnimController(final AnimationState<E> event) {
        if (this.isDeadOrDying()) {
            return event.setAndContinue(DEATH);
        }
        return PlayState.STOP;
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingLevel, boolean recentlyHit) {
        // 调用父类方法生成默认掉落物
        super.dropCustomDeathLoot(source, lootingLevel, recentlyHit);
        
        // 添加自定义掉落物 - 基夫眼珠，掉落2个
        this.spawnAtLocation(ModItems.GIIFU_EYEBALL.get(), 2);
    }
}