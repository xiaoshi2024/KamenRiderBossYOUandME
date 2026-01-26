package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Roidmude;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

// import forge.net.mca.entity.VillagerEntityMCA;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class BrainRoidmudeEntity extends PathfinderMob implements GeoAnimatable {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation RUN = RawAnimation.begin().thenPlay("run");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(BrainRoidmudeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RUNNING = SynchedEntityData.defineId(BrainRoidmudeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MIMICKING = SynchedEntityData.defineId(BrainRoidmudeEntity.class, EntityDataSerializers.BOOLEAN);
    
    private int attackCooldown = 0;
    private static final int MAX_ATTACK_COOLDOWN = 20;
    private int mimicryCooldown = 0;
    private static final int MAX_MIMICRY_COOLDOWN = 200;
    private int dangerCooldown = 0;
    private static final int MAX_DANGER_COOLDOWN = 100;
    
    public BrainRoidmudeEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
    }
    
    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        
        // 实体生成时立即尝试拟态附近的村民
        if (!this.level().isClientSide() && !this.getPersistentData().contains("StoredVillager")) {
            this.tryMimicVillager();
        }
    }
    
    public static AttributeSupplier createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D).build();
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new EscapeGoal(this, 1.5D)); // 添加逃跑目标
        
        // 根据难度设置目标选择
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true, (entity) -> {
            // 只攻击敌对玩家（例如生存模式玩家），和平模式下不攻击
            if (entity instanceof Player player) {
                return !(player.isCreative() || player.isSpectator() || player.level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL);
            }
            return false;
        }));
        
        // 在非和平模式下才攻击怪物
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Monster.class, true, (entity) -> {
            return this.level().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL;
        }));
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
        this.entityData.define(RUNNING, false);
        this.entityData.define(MIMICKING, false);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttacking(tag.getBoolean("Attacking"));
        this.setRunning(tag.getBoolean("Running"));
        this.setMimicking(tag.getBoolean("Mimicking"));
        if (tag.contains("StoredVillager")) {
            this.getPersistentData().put("StoredVillager", tag.getCompound("StoredVillager"));
        }
        // 读取安全计时器
        if (tag.contains("SafetyTimer")) {
            this.safetyTimer = tag.getInt("SafetyTimer");
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Attacking", this.isAttacking());
        tag.putBoolean("Running", this.isRunning());
        tag.putBoolean("Mimicking", this.isMimicking());
        if (this.getPersistentData().contains("StoredVillager")) {
            tag.put("StoredVillager", this.getPersistentData().getCompound("StoredVillager"));
        }
        // 保存安全计时器
        tag.putInt("SafetyTimer", this.safetyTimer);
    }
    
    public boolean isMimicking() {
        return this.entityData.get(MIMICKING);
    }
    
    public void setMimicking(boolean mimicking) {
        this.entityData.set(MIMICKING, mimicking);
    }
    
    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }
    
    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }
    
    public boolean isRunning() {
        return this.entityData.get(RUNNING);
    }
    
    public void setRunning(boolean running) {
        this.entityData.set(RUNNING, running);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        
        if (this.mimicryCooldown > 0) {
            this.mimicryCooldown--;
        }
        
        if (this.dangerCooldown > 0) {
            this.dangerCooldown--;
        }
        
        if (this.teleportCooldown > 0) {
            this.teleportCooldown--;
        }
        
        // 更新奔跑状态
        if (this.getTarget() != null && this.distanceToSqr(this.getTarget()) > 4.0D) {
            this.setRunning(true);
        } else if (this.getDeltaMovement().lengthSqr() > 0.01D) {
            // 当实体正在移动时，也使用奔跑动画
            this.setRunning(true);
        } else {
            this.setRunning(false);
        }
        
        // 危险检测和状态切换
        if (!this.level().isClientSide()) {
            checkSafetyAndTransform();
        }
        
        // 释放毒雾粒子效果
        if (this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            if (target != null) {
                // 如果有锁定目标，粒子效果围绕目标
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.WITCH, 
                            target.getX() + (this.random.nextDouble() - 0.5) * target.getBbWidth(), 
                            target.getY() + this.random.nextDouble() * target.getBbHeight(), 
                            target.getZ() + (this.random.nextDouble() - 0.5) * target.getBbWidth(), 
                            0.0D, 0.1D, 0.0D);
                }
            } else {
                // 如果没有锁定目标，粒子效果围绕自己
                for (int i = 0; i < 2; i++) {
                    this.level().addParticle(ParticleTypes.WITCH, 
                            this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), 
                            this.getY() + this.random.nextDouble() * this.getBbHeight(), 
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), 
                            0.0D, 0.1D, 0.0D);
                }
            }
        }
    }
    
    private void tryMimicVillager() {
        // 只有在没有存储的村民数据时才尝试拟态新的村民
        if (!this.getPersistentData().contains("StoredVillager")) {
            // 查找附近的村民，使用MCAUtil来判断是否是MCA村民
            java.util.List<net.minecraft.world.entity.npc.Villager> villagers = this.level().getEntitiesOfClass(
                    net.minecraft.world.entity.npc.Villager.class, 
                    this.getBoundingBox().inflate(32.0D)
            );
            
            if (!villagers.isEmpty()) {
                // 找到最近的MCA村民
                net.minecraft.world.entity.npc.Villager nearestVillager = null;
                double nearestDistance = Double.MAX_VALUE;
                
                for (net.minecraft.world.entity.npc.Villager villager : villagers) {
                    // 使用MCAUtil检查是否是MCA村民
                    if (com.xiaoshi2022.kamen_rider_boss_you_and_me.util.MCAUtil.isVillagerEntityMCA(villager)) {
                        double distance = this.distanceToSqr(villager);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestVillager = villager;
                        }
                    }
                }
                
                if (nearestVillager != null) {
                    // 拟态MCA村民
                    this.mimicVillager(nearestVillager);
                }
            }
        }
    }
    
    private void mimicVillager(Object villager) {
        // 保存村民数据
        CompoundTag villagerTag = new CompoundTag();
        String villagerType = "unknown";
        
        try {
            // 使用反射调用save方法
            java.lang.reflect.Method saveMethod = villager.getClass().getMethod("save", CompoundTag.class);
            saveMethod.invoke(villager, villagerTag);
            
            // 使用反射获取类型信息
            java.lang.reflect.Method getTypeMethod = villager.getClass().getMethod("getType");
            Object entityType = getTypeMethod.invoke(villager);
            villagerType = entityType.toString();
            
            // 保存村民的原始类型
            CompoundTag storedData = new CompoundTag();
            storedData.put("VillagerData", villagerTag);
            storedData.putString("OriginalType", villagerType);
            
            this.getPersistentData().put("StoredVillager", storedData);
            
            // 播放拟态粒子效果
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(
                        ParticleTypes.GLOW, 
                        this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                        this.getY() + this.random.nextDouble() * this.getBbHeight(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                        0.0D, 0.1D, 0.0D
                );
            }
            
            // 添加调试信息
            if (!this.level().isClientSide()) {
                System.out.println("Brain Roidmude: Successfully mimicked villager - " + villagerType);
                // 立即转换为村民形态
                this.transformToVillager();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Brain Roidmude: Failed to mimic villager - Error: " + e.getMessage());
        }
    }
    
    /* 安全状态检测相关 - 用于变回人类形态 */
    private int safetyTimer = 0;
    private static final int SAFETY_TIME_REQUIRED = 200; // 10秒安全时间
    private static final int THREAT_CHECK_RANGE = 15; // 威胁检测范围
    
    /* 逃跑相关 */
    private int teleportCooldown = 0;
    private static final int TELEPORT_COOLDOWN = 60; // 3秒冷却
    private static final int ESCAPE_HEALTH_THRESHOLD = 30; // 生命值阈值，低于此值会尝试逃跑
    private static final double ESCAPE_RANGE = 30.0D; // 逃跑距离范围
    
    // 检查安全状态并在安全时变回人类形态
    private void checkSafetyAndTransform() {
        if (this.level().isClientSide()) return;

        // 检查附近是否有威胁
        boolean isThreatPresent = !this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(THREAT_CHECK_RANGE),
                entity -> {
                    if (entity instanceof Player) {
                        // 只有在非和平模式下，且玩家正在攻击实体时，才视为威胁
                        return this.level().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL && 
                               ((Player)entity).getLastHurtMob() == this;
                    } else if (entity instanceof net.minecraft.world.entity.monster.Monster) {
                        // 只有当怪物的目标是实体时，才视为威胁
                        return entity instanceof Mob mob && mob.getTarget() == this;
                    }
                    return false;
                }
        ).isEmpty();

        // 检查自身是否受伤（只有生命值低于50%才视为受伤）
        boolean isHurt = this.getHealth() < this.getMaxHealth() * 0.5F;
        
        // 检查是否有直接目标
        boolean hasTarget = this.getTarget() != null;
        
        boolean isInDanger = isThreatPresent || isHurt || hasTarget;

        if (isInDanger) {
            // 有威胁，重置安全计时器
            safetyTimer = 0;
            
            // 处于危险状态，变回怪人态
            if (this.isMimicking()) {
                this.setMimicking(false);
                this.dangerCooldown = MAX_DANGER_COOLDOWN;
                
                // 播放变回怪人态的粒子效果
                for (int i = 0; i < 20; i++) {
                    this.level().addParticle(
                            ParticleTypes.SMOKE, 
                            this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + this.random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            0.0D, 0.1D, 0.0D
                    );
                }
                
                System.out.println("Brain Roidmude: Entering danger mode, reverting to monster form");
            }
        } else {
            // 无威胁，增加安全计时器
            safetyTimer++;

            // 如果有存储的村民数据，立即拟态为村民，不需要等待安全计时器
            if (!this.isMimicking() && this.dangerCooldown <= 0) {
                if (this.getPersistentData().contains("StoredVillager")) {
                    // 真正变成人类形态：创建村民实体并替换
                    this.transformToVillager();
                } else if (this.mimicryCooldown <= 0) {
                    // 没有存储的村民数据，尝试拟态新的村民
                    this.tryMimicVillager();
                }
            }
        }
        
        // 添加调试信息
        if (this.tickCount % 100 == 0) {
            System.out.println("Brain Roidmude: Safety timer - " + safetyTimer + ", Is in danger - " + isInDanger + ", Is mimicking - " + this.isMimicking());
            if (this.getPersistentData().contains("StoredVillager")) {
                System.out.println("Brain Roidmude: Has stored villager data");
            } else {
                System.out.println("Brain Roidmude: No stored villager data");
            }
        }
    }
    
    @Override
    public boolean doHurtTarget(Entity entity) {
        if (super.doHurtTarget(entity)) {
            if (entity instanceof LivingEntity livingEntity) {
                // 注入剧毒
                livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                
                // 有几率释放念动波
                if (this.random.nextFloat() < 0.3F) {
                    this.releaseTelekinesis(livingEntity);
                }
            }
            return true;
        }
        return false;
    }
    
    private void releaseTelekinesis(LivingEntity target) {
        // 实现念动波效果
        if (!this.level().isClientSide()) {
            target.knockback(2.0F, this.getX() - target.getX(), this.getZ() - target.getZ());
            
            // 造成额外伤害
            target.hurt(this.damageSources().mobAttack(this), 5.0F);
        }
    }
    
    private void releaseFireball(LivingEntity target) {
        // 实现火球攻击
        if (!this.level().isClientSide()) {
            // 这里可以创建一个火球实体并发射
            // 暂时使用粒子效果模拟
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.FLAME, 
                        this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), 
                        this.getY() + this.getBbHeight() / 2, 
                        this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(), 
                        (target.getX() - this.getX()) / 20, 
                        (target.getY() - this.getY()) / 20, 
                        (target.getZ() - this.getZ()) / 20);
            }
            
            // 造成远程伤害
            target.hurt(this.damageSources().mobAttack(this), 6.0F);
            
            // 添加燃烧效果
            target.setSecondsOnFire(5);
        }
    }
    
    private void releasePoisonGas() {
        // 实现毒雾释放
        if (!this.level().isClientSide()) {
            // 创建毒雾区域效果
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0D))) {
                if (entity != this && !(entity instanceof BrainRoidmudeEntity)) {
                    // 注入剧毒
                    entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    // 添加缓慢效果
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                }
            }
        }
    }
    
    private void releaseTentacleAttack(LivingEntity target) {
        // 实现触手攻击
        if (!this.level().isClientSide()) {
            // 造成伤害
            target.hurt(this.damageSources().mobAttack(this), 7.0F);
            
            // 注入剧毒
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 150, 1));
            
            // 造成击退
            target.knockback(1.5F, this.getX() - target.getX(), this.getZ() - target.getZ());
        }
    }
    
    private boolean canUltraEvolve() {
        // 检查是否可以超进化
        // 这里可以添加超进化的条件，比如生命值低于50%
        return this.getHealth() / this.getMaxHealth() < 0.5F;
    }
    
    private void ultraEvolve() {
        // 实现超进化
        if (!this.level().isClientSide() && canUltraEvolve()) {
            // 提高属性，但不立即回满生命值
            // 保存当前生命值百分比
            float healthPercentage = this.getHealth() / this.getMaxHealth();
            
            // 添加力量效果
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 2));
            // 添加速度效果
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
            // 添加抗性效果
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
            
            // 播放超进化粒子效果
            for (int i = 0; i < 20; i++) {
                this.level().addParticle(ParticleTypes.GLOW, 
                        this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2, 
                        this.getY() + (this.random.nextDouble() - 0.5) * this.getBbHeight() * 2, 
                        this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2, 
                        0.0D, 0.1D, 0.0D);
            }
        }
    }
    
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        
        // 服务器端AI逻辑
        if (this.getTarget() != null) {
            // 有目标时的行为
            
            // 检查是否可以超进化
            if (canUltraEvolve()) {
                ultraEvolve();
            }
            
            // 随机使用特殊攻击
            if (this.attackCooldown <= 0 && this.random.nextFloat() < 0.1F) {
                this.attackCooldown = MAX_ATTACK_COOLDOWN;
                
                // 随机选择一种特殊攻击
                int attackType = this.random.nextInt(3);
                switch (attackType) {
                    case 0:
                        releaseFireball(this.getTarget());
                        break;
                    case 1:
                        releasePoisonGas();
                        break;
                    case 2:
                        if (this.distanceToSqr(this.getTarget()) < 9.0D) {
                            releaseTentacleAttack(this.getTarget());
                        }
                        break;
                }
            }
        }
    }
    
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        spawnGroupData = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        
        // 设置生命值为最大生命值，避免因难度过低导致生命值为 0 或负数
        this.setHealth(this.getMaxHealth());
        
        return spawnGroupData;
    }
    
    private PlayState predicate(AnimationState<BrainRoidmudeEntity> event) {
        if (this.isAttacking()) {
            event.getController().setAnimation(ATTACK);
        } else if (this.isRunning()) {
            event.getController().setAnimation(RUN);
        } else {
            event.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    @Override
    public double getTick(Object object) {
        return this.tickCount;
    }
    
    // 逃跑AI目标类
    private static class EscapeGoal extends Goal {
        private final BrainRoidmudeEntity mob;
        private final double speedModifier;
        
        public EscapeGoal(BrainRoidmudeEntity mob, double speedModifier) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        
        @Override
        public boolean canUse() {
            // 只有在生命值低、有目标且非和平模式时才会尝试逃跑
            return mob.getHealth() <= ESCAPE_HEALTH_THRESHOLD && mob.getTarget() != null && mob.teleportCooldown <= 0 && mob.level().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL;
        }
        
        @Override
        public boolean canContinueToUse() {
            // 一旦开始逃跑，就会完成整个逃跑过程
            return false;
        }
        
        @Override
        public void start() {
            // 执行瞬移逃跑
            if (!mob.level().isClientSide()) {
                // 随机选择一个安全的位置
                for (int i = 0; i < 10; i++) { // 尝试10次找到安全位置
                    double randomX = mob.getX() + (mob.random.nextDouble() - 0.5) * ESCAPE_RANGE;
                    double randomY = mob.getY() + mob.random.nextInt(10) - 5;
                    double randomZ = mob.getZ() + (mob.random.nextDouble() - 0.5) * ESCAPE_RANGE;
                    
                    // 检查位置是否安全（在固体方块上，且上方有足够空间）
                    if (mob.level().getBlockState(new net.minecraft.core.BlockPos((int)randomX, (int)randomY, (int)randomZ)).isSolid() &&
                        mob.level().getBlockState(new net.minecraft.core.BlockPos((int)randomX, (int)randomY + 1, (int)randomZ)).isAir() &&
                        mob.level().getBlockState(new net.minecraft.core.BlockPos((int)randomX, (int)randomY + 2, (int)randomZ)).isAir()) {
                        
                        // 执行瞬移
                        mob.teleportTo(randomX, randomY + 1, randomZ);
                        
                        // 播放瞬移粒子效果
                        for (int j = 0; j < 20; j++) {
                            mob.level().addParticle(
                                    ParticleTypes.PORTAL, 
                                    mob.getX() + (mob.random.nextDouble() - 0.5) * mob.getBbWidth(),
                                    mob.getY() + mob.random.nextDouble() * mob.getBbHeight(),
                                    mob.getZ() + (mob.random.nextDouble() - 0.5) * mob.getBbWidth(),
                                    0.0D, 0.0D, 0.0D
                            );
                        }
                        
                        // 设置冷却时间
                        mob.teleportCooldown = TELEPORT_COOLDOWN;
                        
                        // 输出调试信息
                        System.out.println("Brain Roidmude: Teleported to escape danger!");
                        
                        break;
                    }
                }
            }
        }
    }
    
    // 转换为村民实体的方法（软依赖MCA）
    private void transformToVillager() {
        if (this.level().isClientSide()) return;
        
        try {
            // 查找最近的村民，使用MCAUtil来判断是否是MCA村民
            java.util.List<net.minecraft.world.entity.npc.Villager> villagers = this.level().getEntitiesOfClass(
                    net.minecraft.world.entity.npc.Villager.class, 
                    this.getBoundingBox().inflate(32.0D)
            );
            
            if (!villagers.isEmpty()) {
                // 找到最近的MCA村民
                net.minecraft.world.entity.npc.Villager nearestVillager = null;
                double nearestDistance = Double.MAX_VALUE;
                
                for (net.minecraft.world.entity.npc.Villager villager : villagers) {
                    // 使用MCAUtil检查是否是MCA村民
                    if (com.xiaoshi2022.kamen_rider_boss_you_and_me.util.MCAUtil.isVillagerEntityMCA(villager)) {
                        double distance = this.distanceToSqr(villager);
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestVillager = villager;
                        }
                    }
                }
                
                if (nearestVillager != null) {
                    System.out.println("Brain Roidmude: Found nearest MCA villager, creating new villager");
                    
                    // 获取村民的NBT数据
                    CompoundTag villagerTag = new CompoundTag();
                    nearestVillager.save(villagerTag);
                    
                    // 保存原始位置和旋转
                    double originalX = this.getX();
                    double originalY = this.getY();
                    double originalZ = this.getZ();
                    float originalYaw = this.getYRot();
                    float originalPitch = this.getXRot();
                    
                    // 移除UUID相关数据，确保使用新的UUID
                    if (villagerTag.contains("UUID")) {
                        villagerTag.remove("UUID");
                    }
                    if (villagerTag.contains("UUIDMost")) {
                        villagerTag.remove("UUIDMost");
                    }
                    if (villagerTag.contains("UUIDLeast")) {
                        villagerTag.remove("UUIDLeast");
                    }
                    
                    // 移除位置和旋转数据，使用Brain Roidmude的位置
                    if (villagerTag.contains("Pos")) {
                        villagerTag.remove("Pos");
                    }
                    if (villagerTag.contains("Rotation")) {
                        villagerTag.remove("Rotation");
                    }
                    
                    // 尝试创建村民实体（使用通用类型）
                    net.minecraft.world.entity.Entity newVillager = null;
                    
                    // 打印村民类型信息
                    System.out.println("Brain Roidmude: Nearest villager type - " + nearestVillager.getType().toString());
                    System.out.println("Brain Roidmude: Nearest villager class - " + nearestVillager.getClass().getName());
                    
                    // 尝试1: 直接使用最近村民的类型创建
                    try {
                        net.minecraft.world.entity.EntityType<?> entityType = nearestVillager.getType();
                        System.out.println("Brain Roidmude: Trying to create villager using entityType: " + entityType);
                        
                        newVillager = entityType.create(this.level());
                        if (newVillager != null) {
                            System.out.println("Brain Roidmude: Successfully created villager using entityType");
                        }
                    } catch (Exception e) {
                        System.out.println("Brain Roidmude: Failed to create villager using entityType: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // 尝试2: 通过字符串获取实体类型
                    if (newVillager == null) {
                        try {
                            String typeString = nearestVillager.getType().toString();
                            System.out.println("Brain Roidmude: Trying to create villager using type string: " + typeString);
                            
                            net.minecraft.world.entity.EntityType<?> entityType = net.minecraft.world.entity.EntityType.byString(typeString).orElse(null);
                            System.out.println("Brain Roidmude: Entity type found: " + (entityType != null));
                            
                            if (entityType != null) {
                                System.out.println("Brain Roidmude: Entity type base class: " + entityType.getBaseClass().getName());
                                // 使用字符串检查代替直接类型检查
                                if (entityType.getBaseClass().getName().contains("VillagerEntityMCA")) {
                                    System.out.println("Brain Roidmude: Entity type is assignable to VillagerEntityMCA");
                                    newVillager = entityType.create(this.level());
                                    if (newVillager != null) {
                                        System.out.println("Brain Roidmude: Successfully created villager using type string");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Brain Roidmude: Failed to create MCA villager using type string: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    // 尝试3: 使用默认的MCA村民类型
                    if (newVillager == null) {
                        try {
                            System.out.println("Brain Roidmude: Trying to create default MCA villager");
                            
                            // 尝试获取所有已注册的实体类型
                            for (net.minecraft.world.entity.EntityType<?> registeredType : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.stream().toList()) {
                                if (registeredType.getBaseClass().getName().contains("VillagerEntityMCA")) {
                                    System.out.println("Brain Roidmude: Found MCA villager entity type: " + registeredType.toString());
                                    try {
                                        newVillager = registeredType.create(this.level());
                                        if (newVillager != null) {
                                            System.out.println("Brain Roidmude: Successfully created villager from registered type");
                                            break;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Brain Roidmude: Failed to create villager from registered type: " + e.getMessage());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Brain Roidmude: Failed to find registered MCA villager types: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    // 尝试4: 直接使用构造函数创建
                    if (newVillager == null) {
                        try {
                            System.out.println("Brain Roidmude: Trying to create villager using constructor");
                            
                            // 尝试使用反射创建实例
                            Class<?>[] paramTypes = {net.minecraft.world.entity.EntityType.class, Level.class};
                            java.lang.reflect.Constructor<?> constructor = nearestVillager.getClass().getConstructor(paramTypes);
                            
                            net.minecraft.world.entity.EntityType<?> entityType = nearestVillager.getType();
                            newVillager = (net.minecraft.world.entity.Entity) constructor.newInstance(entityType, this.level());
                            
                            if (newVillager != null) {
                                System.out.println("Brain Roidmude: Successfully created villager using constructor");
                            }
                        } catch (Exception e) {
                            System.out.println("Brain Roidmude: Failed to create villager using constructor: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    if (newVillager != null) {
                        // 加载村民数据到新实体
                        newVillager.load(villagerTag);
                        newVillager.moveTo(originalX, originalY, originalZ, originalYaw, originalPitch);
                        
                        // 保存Brain Roidmude的数据到新村民实体
                        CompoundTag brainData = new CompoundTag();
                        this.save(brainData);
                        newVillager.getPersistentData().put("BrainRoidmudeData", brainData);
                        newVillager.getPersistentData().putBoolean("IsMimicking", true);
                        
                        // 保存村民数据以供后续使用
                        CompoundTag storedData = new CompoundTag();
                        storedData.put("VillagerData", villagerTag);
                        storedData.putString("OriginalType", nearestVillager.getType().toString());
                        this.getPersistentData().put("StoredVillager", storedData);
                        
                        // 播放转换粒子效果
                        for (int i = 0; i < 30; i++) {
                            this.level().addParticle(
                                    ParticleTypes.GLOW, 
                                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                                    this.getY() + this.random.nextDouble() * this.getBbHeight(),
                                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                                    0.0D, 0.1D, 0.0D
                            );
                        }
                        
                        // 添加新村民到世界
                        this.level().addFreshEntity(newVillager);
                        
                        // 移除原来的Brain Roidmude
                        this.discard();
                        
                        System.out.println("Brain Roidmude: Successfully created new MCA villager with Brain Roidmude data");
                    } else {
                        System.out.println("Brain Roidmude: Failed to create MCA villager entity");
                    }
                }
            } else {
                System.out.println("Brain Roidmude: No MCA villagers found nearby");
            }
        } catch (Exception e) {
            System.out.println("Brain Roidmude: Failed to create MCA villager: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
