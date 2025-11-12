package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.levelgen.Heightmap;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.AnotherDenlinerEntity;

public class Another_Den_o extends Monster implements GeoEntity {

    /* ---------- Geckolib ---------- */
    private static final RawAnimation ENTER = RawAnimation.begin().thenLoop("enter");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack1");
    private static final RawAnimation ATTACKT = RawAnimation.begin().thenPlay("attack2");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    
    /* 动画状态跟踪 */
    private boolean isEnterAnimationPlaying = false;
    private AnimationController<Another_Den_o> enterController;
    private AnimationController<Another_Den_o> attackController;
    private boolean isHalfHealthAnimationPlayed = false;
    private int enterAnimationDelay = 2; // 延迟几tick再播放入场动画，确保控制器已初始化

    /* ---------- 预知系统 ---------- */
    private static final int PRECOG_COOLDOWN = 20;       // 每 20 tick 跑一次
    private static final int PRECOG_FRAMES   = 4;        // 只存 4 帧
    private static final double PRECOG_RANGE_SQ = 6.0 * 6.0;

    /* 放在字段区，与 precog 列表并列 */
    private static final int TELEPORT_COOLDOWN = 60;   // 3 秒冷却
    public int teleportCooldown = 0;

    private final List<PrecogSnapshot> precog = new ObjectArrayList<>(PRECOG_FRAMES);
    private int precogTimer = 0;

    /* 跳跃追击相关 */
    public static final int JUMP_ATTACK_COOLDOWN = 40; // 2秒冷却
    public int jumpAttackCooldown = 0;
    private boolean isJumping = false;

    /* 半血狂暴 */
    private boolean enraged = false;
    private boolean isTeleporting = false; // 防止重复创建班列实体的标记

    public Another_Den_o(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        // 标记需要播放入场动画，但具体播放会在控制器初始化后通过tick方法处理
        isEnterAnimationPlaying = true;
    }

    // 获取随机源
    public RandomSource getRNG() { return this.random; }

    /* ---------- AI ---------- */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
//        this.goalSelector.addGoal(2, new TeleportBehindGoal(this));
        this.goalSelector.addGoal(2, new PrecogDodgeGoal(this)); // 闪避 goal
        this.goalSelector.addGoal(2, new JumpAttackGoal(this)); // 跳跃追击高处玩家
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.23D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // 添加目标选择器
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        
        // 添加玩家目标，但排除时劫者
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(
                this, 
                Player.class, 
                true
        ));
        
        // 添加村民目标，但排除时劫者
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(
                this, 
                Villager.class, 
                true
        ));
        
        // 重写checkAttackable方法，在entity类中实现对时劫者的排除
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

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            // 客户端处理动画逻辑
            // 延迟触发入场动画
            if (isEnterAnimationPlaying && enterAnimationDelay > 0) {
                enterAnimationDelay--;
                if (enterAnimationDelay <= 0 && enterController != null) {
                    enterController.setAnimation(ENTER);
                    enterController.forceAnimationReset();
                }
            }
            
            // 半血动画触发
            if (!enraged && this.getHealth() < this.getMaxHealth() / 2 && !isHalfHealthAnimationPlayed) {
                isHalfHealthAnimationPlayed = true;
                isEnterAnimationPlaying = true;
                if (enterController != null) {
                    enterController.setAnimation(ENTER);
                    enterController.forceAnimationReset();
                }
            }
            return;
        }

        /* 半血狂暴 */
        if (!enraged && this.getHealth() < this.getMaxHealth() / 2) {
            enraged = true;
            this.playSound(SoundEvents.WITHER_SPAWN, 1.0F, 1.0F);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35);
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, 1));
        }

        /* 更新预测 */
        if (--precogTimer <= 0) {
            precogTimer = PRECOG_COOLDOWN;
            precog.clear();
            if (getTarget() instanceof Player p && distanceToSqr(p) < PRECOG_RANGE_SQ) {
                predictPath(p);
            }
        }
        if (teleportCooldown > 0) teleportCooldown--;
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 获取当前维度名称
        String currentDimension = this.level().dimension().location().toString();
        // 检查是否在时间的沙漠维度或时间领域维度
        boolean isInTimeDesert = currentDimension.equals("kamen_rider_weapon_craft:the_desertof_time");
        boolean isInTimeRealm = currentDimension.equals("kamen_rider_boss_you_and_me:time_realm");
        boolean isInAllowedDimension = isInTimeDesert || isInTimeRealm;
        
        // 只在非允许维度中，异类电王无法被杀死
        // 但只有在非时之沙漠维度中才会召唤时之列车
        if (!this.level().isClientSide() && !isInAllowedDimension) {
            // 计算伤害后的剩余生命值
            float remainingHealth = this.getHealth() - amount;
            
            // 如果伤害会导致死亡，则阻止死亡并触发传送
            if (remainingHealth <= 0 || this.getHealth() <= this.getMaxHealth() * 0.4f) {
                // 如果不在传送冷却中，则触发传送
                if (!isTeleporting && teleportCooldown <= 0) {
                    // 标记为正在传送，防止重复创建班列
                    isTeleporting = true;
                    
                    // 只在非时之沙漠维度中召唤时之列车
                    if (!isInTimeDesert) {
                        // 查找附近的时劫者
                        List<com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity> nearbyTimeJackers = this.level().getEntitiesOfClass(
                                com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity.class,
                                this.getBoundingBox().inflate(20.0D)
                        );
                        
                        // 如果附近有时劫者，才触发传送逻辑
                        if (!nearbyTimeJackers.isEmpty()) {
                            // 召唤异类电班列
                            com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.AnotherDenlinerEntity denliner = new com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.AnotherDenlinerEntity(this.level());
                            denliner.setPos(this.getX(), this.getY() + 2, this.getZ());
                            this.level().addFreshEntity(denliner);
                            
                            // 保存自己和时劫者到班列中
                            List<Entity> entitiesToSave = new ArrayList<>();
                            entitiesToSave.add(this);
                            entitiesToSave.add(nearbyTimeJackers.get(0)); // 保存最近的一个时劫者
                            
                            // 保存实体并传送 - 传递一个假的玩家（null），因为实际上是AI触发的
                            denliner.saveEntities(null, entitiesToSave.toArray(new Entity[0]));
                            
                            // 添加日志确认实体已保存
                            com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.LOGGER.info("Another_Den_o: Entities saved to denliner, teleportation will be handled by denliner");
                        } else {
                            // 如果附近没有时劫者，则允许正常受到伤害
                            return super.hurt(source, amount);
                        }
                    } else {
                        // 在时之沙漠维度中，直接重置传送标记，不召唤时之列车
                        isTeleporting = false;
                    }
                    
                    // 设置一个延迟来重置传送标记，确保传送过程完成
                    this.level().getServer().getPlayerList().getPlayers().forEach(player -> {
                        // 这里只是为了确保服务器线程上执行
                        player.getServer().execute(() -> {
                            // 延迟重置标记，给班列足够时间完成传送
                            try {
                                Thread.sleep(1000);
                                isTeleporting = false;
                                teleportCooldown = TELEPORT_COOLDOWN;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    });
                }
                
                // 修改：如果附近没有时劫者，允许正常击败异类电王
                List<com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity> nearbyTimeJackers = this.level().getEntitiesOfClass(
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity.class,
                        this.getBoundingBox().inflate(20.0D)
                );
                
                if (nearbyTimeJackers.isEmpty()) {
                    // 如果附近没有时劫者，允许正常受到伤害和死亡
                    return super.hurt(source, amount);
                } else {
                    // 如果附近有时劫者，但传送冷却中，则限制生命值但不杀死
                    if (this.getHealth() > 1.0F) {
                        this.setHealth(1.0F);
                    }
                    return true; // 表示受到了攻击，但伤害被限制
                }
            }
            
            // 如果伤害不足以导致死亡，则允许部分伤害，但不能杀死
            if (remainingHealth <= 1.0F) {
                this.setHealth(1.0F);
                return true;
            }
        }
        
        // 在允许维度中，正常处理伤害
        return super.hurt(source, amount);
    }
    
    @Override
    public void aiStep() {
        super.aiStep();
        
        // 如果实体被移除，重置传送标记
        if (this.isRemoved()) {
            isTeleporting = false;
        }
        // 只在非和平模式下执行攻击逻辑
        if (this.level().getDifficulty() != Difficulty.PEACEFUL &&
                !(this.getTarget() instanceof Player && ((Player)this.getTarget()).isCreative())) {
            LivingEntity target = this.getTarget();
            // 不攻击时劫者
            if (target != null && this.distanceTo(target) < 2.0D && 
                !(target instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity)) {
                float damage = (float)this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
                target.hurt(this.damageSources().mobAttack(this), damage);
            }
        }
        
        // 更新跳跃状态和冷却
        if (isJumping && this.onGround()) {
            isJumping = false;
        }
        if (jumpAttackCooldown > 0) {
            jumpAttackCooldown--;
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

        // 扫描周围实体，但排除时劫者实体
        for (LivingEntity entity : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(2.0D),
                e -> e != this && !(e instanceof Player && ((Player)e).isCreative()) && 
                     !(e instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity))
        ) {
            entity.hurt(this.damageSources().mobAttack(this), (float)damage);
        }
    }

    /* ---------- 预测轨迹 ---------- */
    private void predictPath(Player player) {
        Vec3 pos = player.position();
        Vec3 vel = player.getDeltaMovement();
        for (int i = 0; i < PRECOG_FRAMES; i++) {
            vel = vel.add(0, -0.08, 0).scale(0.98);
            pos = pos.add(vel);
            if (pos.y < 0) pos = new Vec3(pos.x, 0, pos.z);
            precog.add(PrecogSnapshot.of(pos, vel));
        }
    }

    /* ---------- 给 AI 用的接口 ---------- */
    public Vec3 getPrecog(int ticks) {
        int idx = ticks / 10;
        return idx < 0 || idx >= precog.size() ? null : precog.get(idx).pos;
    }

    /* ---------- Geckolib ---------- */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // ENTER动画控制器
        enterController = new AnimationController<>(this, "enter", 5, e -> {
            if (!isEnterAnimationPlaying) {
                return PlayState.STOP;
            }
            // 播放一段时间后切换回默认动画
            if (e.getController().getAnimationState() == AnimationController.State.STOPPED) {
                isEnterAnimationPlaying = false;
                return PlayState.STOP;
            }
            return PlayState.CONTINUE;
        });
        controllers.add(enterController);
        
        // 移动动画控制器
        controllers.add(new AnimationController<>(this, "move", 5, e -> {
            if (isEnterAnimationPlaying) {
                return PlayState.STOP; // ENTER动画播放时暂停移动动画
            }
            return e.isMoving() ? e.setAndContinue(WALK) : e.setAndContinue(IDLE);
        }));
        
        // 攻击动画控制器（两种攻击动画随机切换）
        attackController = new AnimationController<>(this, "attack", 5, e -> {
            if (isEnterAnimationPlaying) {
                return PlayState.STOP; // ENTER动画播放时暂停攻击动画
            }
            if (e.getController().getAnimationState() == AnimationController.State.STOPPED && swinging) {
                // 随机选择攻击动画1或攻击动画2
                RawAnimation chosenAttack = this.random.nextBoolean() ? ATTACK : ATTACKT;
                return e.setAndContinue(chosenAttack);
            }
            return PlayState.STOP;
        });
        controllers.add(attackController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    /* ---------- 闪避 Goal ---------- */
    private static class PrecogDodgeGoal extends Goal {
        private final Another_Den_o mob;
        private int strafeTimer = 0;

        PrecogDodgeGoal(Another_Den_o mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.JUMP)); // 只抢跳跃权
        }

        @Override
        public boolean canUse() {
            return mob.getTarget() instanceof Player
                    && mob.distanceToSqr(mob.getTarget()) < PRECOG_RANGE_SQ;
        }

        @Override
        public void tick() {
            Vec3 future = mob.getPrecog(10);
            if (future == null) return;

            Vec3 to = future.subtract(mob.position()).normalize();
            if (to.dot(mob.getLookAngle()) > 0.8) {
                if (--strafeTimer <= 0) {
                    Vec3 side = new Vec3(-to.z, 0, to.x).normalize();
                    side = mob.getRandom().nextBoolean() ? side : side.scale(-1);
                    mob.setDeltaMovement(side.scale(0.4));
                    strafeTimer = 20;
                }
            }
        }
    }

    /* ---------- 跳跃追击 Goal ---------- */
    private static class JumpAttackGoal extends Goal {
        private final Another_Den_o mob;
        private static final double MIN_HEIGHT_DIFFERENCE = 2.0D; // 最小高度差
        private static final double MAX_DISTANCE_SQ = 12.0D * 12.0D; // 最大距离平方

        JumpAttackGoal(Another_Den_o mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // 检查是否有目标、冷却时间、目标是否在高处、是否在地面上
            return mob.getTarget() instanceof Player target &&
                   mob.jumpAttackCooldown <= 0 &&
                   !mob.isJumping &&
                   mob.onGround() &&
                   target.getY() - mob.getY() >= MIN_HEIGHT_DIFFERENCE &&
                   mob.distanceToSqr(target) <= MAX_DISTANCE_SQ;
        }

        @Override
        public void start() {
            Player target = (Player) mob.getTarget();
            if (target == null) return;

            // 计算方向向量
            Vec3 direction = target.position().subtract(mob.position());
            // 增加更大的垂直分量使跳跃更高（9格方块高度）
            direction = new Vec3(direction.x, direction.y + 6.0, direction.z).normalize();

            // 设置速度和跳跃状态 - 增加跳跃力度以达到9格高度
            double speed = mob.enraged ? 1.8D : 1.5D; // 提高跳跃速度以实现9格高度跳跃
            mob.setDeltaMovement(direction.scale(speed));
            mob.setJumping(true);
            mob.isJumping = true;
            mob.jumpAttackCooldown = JUMP_ATTACK_COOLDOWN;
            
            // 移除跳跃音效播放
        }

        @Override
        public boolean canContinueToUse() {
            // 只在跳跃过程中继续执行
            return mob.isJumping && !mob.onGround();
        }

        @Override
        public void tick() {
            // 跳跃过程中持续朝向目标
            Player target = (Player) mob.getTarget();
            if (target != null) {
                mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }

    /* ---------- 快照 ---------- */
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

    /* ---------- 掉落 ---------- */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        
        // 检查是否在时之沙漠维度
        if (!level().isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level();
            ResourceLocation dimensionType = serverLevel.dimension().location();
            
            // 如果在时之沙漠维度被击败，生成电班列
            if (dimensionType.toString().equals("kamen_rider_weapon_craft:the_desertof_time")) {
                try {
                    // 创建电班列实体
                    AnotherDenlinerEntity denliner = ModEntityTypes.ANOTHER_DENLINER.get().create(serverLevel);
                    if (denliner != null) {
                        // 设置电班列位置在异类电王死亡位置
                        denliner.setPos(this.getX(), this.getY(), this.getZ());
                        
                        // 添加到世界
                        serverLevel.addFreshEntity(denliner);
                        
                        // 记录日志
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.LOGGER.info("Another_Den_o: Created AnotherDenliner at position: " + this.position());
                    }
                } catch (Exception e) {
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.LOGGER.error("Another_Den_o: Error creating AnotherDenliner: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hit) {
        super.dropCustomDeathLoot(source, looting, hit);

        /* 1. 必掉带剩余耐久的表盘 - 电王模式 */
        int remain = this.getPersistentData().getInt("DropDamage");
        ItemStack disk = new ItemStack(ModItems.AIZIOWC.get());
        // 设置为电王模式
        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.setUseCount(disk, com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.MAX_DAMAGE - remain);
        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc item = (com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc) disk.getItem();
        item.switchMode(disk, com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc.Mode.DEN_O);
        spawnAtLocation(disk);

        /* 2. 额外 2-3 个骑士电路板 */
        ItemStack boards = new ItemStack(
                com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.RIDER_CIRCUIT_BOARD.get(),
                2 + this.random.nextInt(2));
        spawnAtLocation(boards);

        /* 3. 把村民还原 */
        CompoundTag villagerTag = this.getPersistentData().getCompound("StoredVillager");
        String entityId = villagerTag.getString("id");
        EntityType<?> originalType = EntityType.byString(entityId)
                .orElse(EntitiesMCA.MALE_VILLAGER.get());
        VillagerEntityMCA restored = (VillagerEntityMCA) originalType.create((ServerLevel) this.level());
        restored.load(villagerTag);
        restored.setPos(this.getX(), this.getY(), this.getZ());
        restored.setHealth(restored.getMaxHealth());
        ((ServerLevel) this.level()).addFreshEntity(restored);
    }
    @Override
    public boolean isCustomNameVisible() {
        return false; // 隐藏实体ID显示
    }
    
    // 确保实体ID不显示
    @Override
    public boolean shouldShowName() {
        return false;
    }
    
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // 移除骑士通票右键传送功能
        // 现在玩家需要击败异类电王后通过生成的电班列返回主世界
        return super.mobInteract(player, hand);
    }
}
