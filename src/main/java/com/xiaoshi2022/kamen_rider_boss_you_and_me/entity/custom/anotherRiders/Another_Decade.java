package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.skill.aidcdskill.AnotherDimensionKick;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.skill.aidcdskill.AnotherWorldSummon;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.skill.aidcdskill.BlastPrison;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class Another_Decade extends Monster implements GeoEntity {

    /* ---------- Geckolib ---------- */
    private static final RawAnimation IDLE   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    /* ---------- 预知系统 ---------- */
    private static final int PRECOG_COOLDOWN = 20;       // 每 20 tick 跑一次
    private static final int PRECOG_FRAMES   = 4;        // 只存 4 帧
    private static final double PRECOG_RANGE_SQ = 6.0 * 6.0;

    /* 放在字段区，与 precog 列表并列 */
    private static final int TELEPORT_COOLDOWN = 60;   // 3 秒冷却
    public int teleportCooldown = 0;

    private final List<PrecogSnapshot> precog = new ObjectArrayList<>(PRECOG_FRAMES);
    private int precogTimer = 0;

    /* 半血狂暴 */
    private boolean enraged = false;
    
    /* 安全状态检测相关 - 用于变回时间王族 */
    private int safetyTimer = 0;
    private static final int SAFETY_TIME_REQUIRED = 200; // 10秒安全时间
    private static final int THREAT_CHECK_RANGE = 15; // 威胁检测范围
    
    /* 技能检查计时器 */
    public int skillCheckTimer = 0;

    public Another_Decade(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        // 初始化技能相关的NBT数据
        this.getPersistentData().putInt("AnotherDimensionKickCooldown", 0);
        this.getPersistentData().putInt("BlastPrisonCooldown", 0);
        this.getPersistentData().putInt("AnotherWorldSummonCooldown", 0);
        this.getPersistentData().putBoolean("IsSummoning", false);
        this.getPersistentData().putInt("SummonTimer", 0);
        this.getPersistentData().putInt("BlastPrisonStage", 0);
        this.getPersistentData().putInt("BlastPrisonTimer", 0);
    }

    // 获取随机源
    public RandomSource getRNG() { return this.random; }
    
    // 获取狂暴状态（供技能类使用）
    public boolean isEnraged() {
        return enraged;
    }
    
    // 更新技能冷却和状态
    private void updateSkills() {
        // 更新AnotherDimensionKick冷却
        AnotherDimensionKick.updateCooldown(this);
        
        // 更新BlastPrison状态
        BlastPrison.update(this);
        
        // 更新AnotherWorldSummon状态
        AnotherWorldSummon.update(this);
    }
    
    // 技能使用AI目标类
    private static class SkillUseGoal extends Goal {
        private final Another_Decade mob;

        SkillUseGoal(Another_Decade mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // 如果正在召唤过程中，不使用其他技能
            if (mob.getPersistentData().getBoolean("IsSummoning")) {
                return false;
            }

            // 确保有目标且在战斗状态
            return mob.getTarget() != null && mob.isAlive();
        }

        @Override
        public void tick() {
            // 每20tick检查一次技能使用条件
            if (mob.skillCheckTimer > 0) {
                mob.skillCheckTimer--;
            } else {
                mob.skillCheckTimer = 20;
                
                // 检查目标是否在高处
                boolean isTargetHighUp = false;
                if (mob.getTarget() != null) {
                    double heightDifference = mob.getTarget().getY() - mob.getY();
                    isTargetHighUp = heightDifference > 2.0D; // 目标在2格以上视为高处
                }

                // 根据优先级决定使用哪个技能
                // 如果目标在高处，优先使用AnotherDimensionKick
                if (isTargetHighUp && AnotherDimensionKick.canUse(mob)) {
                    AnotherDimensionKick.use(mob);
                }
                // 1. 首先检查是否需要使用BlastPrison（群攻/保命）
                else if (BlastPrison.canUse(mob)) {
                    BlastPrison.use(mob);
                }
                // 2. 然后检查是否需要使用AnotherWorldSummon（召唤支援）
                else if (AnotherWorldSummon.canUse(mob)) {
                    AnotherWorldSummon.use(mob);
                }
                // 3. 最后检查是否可以使用AnotherDimensionKick（单体爆发）
                else if (AnotherDimensionKick.canUse(mob)) {
                    AnotherDimensionKick.use(mob);
                }
            }
        }
    }

    /* ---------- AI ---------- */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        // 移除不兼容的TeleportBehindGoal
        this.goalSelector.addGoal(2, new PrecogDodgeGoal(this)); // 闪避 goal
        this.goalSelector.addGoal(3, new SkillUseGoal(this)); // 技能使用 goal
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.23D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Villager.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        // 如果目标是时劫者，不能攻击
        if (target instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity) {
            return false;
        }
        // 否则使用默认行为
        return super.canAttack(target);
    }

    /* ---------- 主 tick ---------- */
    @Override
    public void tick() {
        super.tick();

        // 冷却计时
        if (teleportCooldown > 0) {
            teleportCooldown--;
        }

        // 半血狂暴检查
        if (this.getHealth() <= this.getMaxHealth() * 0.5 && !enraged) {
            enraged = true;
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 999999, 1));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 999999, 1));
        }

        // 预知系统更新
        if (precogTimer > 0) {
            precogTimer--;
        } else {
            precogTimer = PRECOG_COOLDOWN;
            Player player = this.level().getNearestPlayer(this, 16.0D);
            if (player != null && this.distanceToSqr(player) < PRECOG_RANGE_SQ) {
                predictPath(player);
            }
        }

        // 更新技能状态
        updateSkills();

        // 检查安全状态并可能变回时间王族
        checkSafetyAndRevert();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // 半血时给予玩家减速效果
        if (enraged) {
            List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(8.0D));
            for (Player player : players) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // 使用自定义攻击力属性
        if (target instanceof LivingEntity livingTarget) {
            // 获取自定义攻击力值
            double customDamage = this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
            // 应用伤害
            float damage = (float) customDamage;
            // 如果处于狂暴状态，额外增加伤害
            if (enraged) {
                damage *= 1.2F; // 狂暴状态额外20%伤害
            }

            boolean result = livingTarget.hurt(this.damageSources().mobAttack(this), damage);
            if (result) {
                hurtNearbyEntities();
                // 播放攻击音效
                this.playSound(SoundEvents.GENERIC_HURT, 1.0F, 1.0F);
            }
            return result;
        }
        return false;
    }

    public void hurtNearbyEntities() {
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(3.0D), entity ->
                        entity != this && entity instanceof Player &&
                        !(entity instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity));

        // 获取自定义攻击力的一半作为范围伤害
        double customDamage = this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
        float areaDamage = (float) (customDamage * 0.5);

        for (LivingEntity entity : nearbyEntities) {
            // 对周围敌人造成额外伤害
            entity.hurt(this.damageSources().mobAttack(this), areaDamage);
            // 击退效果
            Vec3 vec3 = entity.position().subtract(this.position()).normalize().scale(0.5D);
            entity.push(vec3.x, 0.2D, vec3.z);
        }
    }

    private void predictPath(Player player) {
        // 预测玩家路径
        if (precog.size() >= PRECOG_FRAMES) {
            precog.remove(0);
        }
        precog.add(PrecogSnapshot.of(player.position(), player.getDeltaMovement()));
    }

    public Vec3 getPrecog(int ticks) {
        if (precog.isEmpty() || ticks >= precog.size()) {
            return null;
        }
        return precog.get(ticks).pos.add(precog.get(ticks).vel.scale(ticks * 0.5));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            if (state.isMoving()) {
                state.setAnimation(WALK);
            } else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "attackController", 2, state -> {
            if (this.swinging && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                state.setAnimation(ATTACK);
                this.swinging = false;
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private static class PrecogDodgeGoal extends Goal {
        private final Another_Decade mob;
        private int strafeTimer = 0;

        PrecogDodgeGoal(Another_Decade mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.JUMP)); // 只抢跳跃权
        }

        @Override
        public boolean canUse() {
            Player player = mob.level().getNearestPlayer(mob, 16.0D);
            if (player == null || mob.distanceToSqr(player) > PRECOG_RANGE_SQ) {
                return false;
            }

            Vec3 predictedPos = mob.getPrecog(1);
            if (predictedPos == null) {
                return false;
            }

            // 计算预测位置与自身的距离
            double distance = predictedPos.distanceTo(mob.position());
            return distance < 2.0D && strafeTimer == 0;
        }

        @Override
        public void tick() {
            if (strafeTimer == 0) {
                // 随机向左右闪避
                double strafe = mob.random.nextBoolean() ? 1.0D : -1.0D;
                mob.push(strafe, 0.3D, 0.0D);
                strafeTimer = 20; // 冷却20tick
            } else {
                strafeTimer--;
            }
        }
    }

    private static final class PrecogSnapshot {
        Vec3 pos;
        Vec3 vel;
        static PrecogSnapshot of(Vec3 p, Vec3 v) {
            PrecogSnapshot s = new PrecogSnapshot();
            s.pos = p;
            s.vel = v;
            return s;
        }
    }

    // 检查安全状态并在安全时变回时间王族
    private void checkSafetyAndRevert() {
        if (this.level().isClientSide()) return;

        // 检查附近是否有威胁
        boolean isThreatPresent = !this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(THREAT_CHECK_RANGE),
                entity -> {
                    if (entity instanceof Player || entity instanceof Monster) {
                        return (entity instanceof Mob mob && mob.getTarget() == this) ||
                               (entity instanceof Player player && !player.getMainHandItem().isEmpty());
                    }
                    return false;
                }
        ).isEmpty();

        if (isThreatPresent) {
            // 有威胁，重置安全计时器
            safetyTimer = 0;
        } else {
            // 无威胁，增加安全计时器
            safetyTimer++;

            // 如果安全时间足够长，变回时间王族
            if (safetyTimer >= SAFETY_TIME_REQUIRED) {
                revertToTimeRoyalty();
            }
        }
    }

    // 变回时间王族的方法
    private void revertToTimeRoyalty() {
        if (this.level().isClientSide()) return;

        try {
            // 检查是否有保存的时间王族数据
            if (this.getPersistentData().contains("StoredTimeRoyalty")) {
                CompoundTag storedData = this.getPersistentData().getCompound("StoredTimeRoyalty");
                CompoundTag timeRoyaltyTag = storedData.getCompound("TimeRoyaltyData");
                String originalType = storedData.getString("OriginalType");

                // 创建时间王族实体
                EntityType<?> type = EntityType.byString(originalType).orElse(com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes.TIME_ROYALTY.get());
                com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeRoyaltyEntity timeRoyalty = (com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeRoyaltyEntity) type.create(this.level());

                // 恢复数据
                timeRoyalty.load(timeRoyaltyTag);
                timeRoyalty.copyPosition(this);

                // 设置变身后的状态
                timeRoyalty.getPersistentData().putBoolean("HasTransformed", true);
                timeRoyalty.setTransformationCooldown(60); // 3秒冷却

                // 添加到世界
                this.level().addFreshEntity(timeRoyalty);
                this.discard();

                // 显示消息 - 只显示给附近的玩家
                if (this.level() instanceof ServerLevel serverLevel) {
                    double messageRange = 30.0D; // 消息显示范围
                    serverLevel.getPlayers(p -> p.distanceToSqr(this) <= messageRange * messageRange).forEach(player -> {
                        player.displayClientMessage(Component.literal("异类帝骑变回了时间王族！"), true);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hit) {
        super.dropCustomDeathLoot(source, looting, hit);

        /* 1. 必掉带剩余耐久的表盘 - DCD模式 */
        int remain = this.getPersistentData().getInt("DropDamage");
        ItemStack disk = new ItemStack(ModItems.AIZIOWC.get());
        // 设置为DCD模式
        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.setUseCount(disk, com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.MAX_DAMAGE - remain);
        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc item = (com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc) disk.getItem();
        item.switchMode(disk, com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.Mode.DCD);
        spawnAtLocation(disk);

        /* 2. 额外 2-3 个骑士电路板 */
        ItemStack boards = new ItemStack(
                com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.RIDER_CIRCUIT_BOARD.get(),
                2 + this.random.nextInt(2));
        spawnAtLocation(boards);

        /* 3. 还原时间王族 */
        try {
            // 检查是否有保存的时间王族数据
            if (this.getPersistentData().contains("StoredTimeRoyalty")) {
                CompoundTag storedData = this.getPersistentData().getCompound("StoredTimeRoyalty");
                CompoundTag timeRoyaltyTag = storedData.getCompound("TimeRoyaltyData");
                String originalType = storedData.getString("OriginalType");

                // 创建时间王族实体
                EntityType<?> type = EntityType.byString(originalType).orElse(com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes.TIME_ROYALTY.get());
                com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeRoyaltyEntity timeRoyalty = (com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeRoyaltyEntity) type.create(this.level());

                // 恢复数据
                timeRoyalty.load(timeRoyaltyTag);
                timeRoyalty.copyPosition(this);

                // 设置变身后的状态
                timeRoyalty.getPersistentData().putBoolean("HasTransformed", true);
                timeRoyalty.setTransformationCooldown(60); // 3秒冷却

                // 添加到世界
                ((ServerLevel) this.level()).addFreshEntity(timeRoyalty);
            }
        } catch (Exception e) {
            // 如果还原失败，记录错误但不影响其他掉落
            e.printStackTrace();
        }
    }

    @Override
    public boolean isCustomNameVisible() {
        return true;
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }
}