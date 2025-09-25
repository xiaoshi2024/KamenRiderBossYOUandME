package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DukeKnightEntity extends PathfinderMob implements GeoEntity {

    /* ---------- GeckLib ---------- */
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /* ---------- 实体逻辑 ---------- */
    private Player owner;
    private boolean shouldVanish = false;
    private int vanishTicks = 0;
    private static final int VANISH_DELAY = 20; // 1 秒
    private int attackOrder = 0; // 攻击顺序: 0表示不参与顺序攻击, 1-3表示攻击顺序
    private boolean canAttack = true; // 是否可以攻击
    private static final int ATTACK_COOLDOWN = 10; // 攻击冷却时间(刻)
    private int noTargetTicks = 0; // 无攻击目标的时间计数
    private static final int MAX_NO_TARGET_TICKS = 200; // 10秒(20刻/秒)
    private boolean vanishSoundPlayed = false; // 消失音效是否已播放
    private boolean teleportSoundPlayed = false; // 传送音效是否已播放
    private int teleportCooldownTicks = 0; // 传送音效冷却计时
    private boolean isSpeedBoosted = false; // 是否处于速度加成状态
    private int speedBoostTicks = 0; // 速度加成剩余时间
    private static final int MAX_SPEED_BOOST_TICKS = 100; // 最大速度加成时间(5秒)
    
    // 假面骑士Duke基础buff相关
    private boolean isInvisible = false; // 是否处于光学迷彩状态
    private int invisibilityTicks = 0; // 光学迷彩剩余时间
    private static final int MAX_INVISIBILITY_TICKS = 60; // 最大光学迷彩时间(3秒)
    private boolean combatAnalysisActive = false; // 战斗数据分析是否激活
    private int analysisTicks = 0; // 数据分析剩余时间
    private static final int MAX_ANALYSIS_TICKS = 120; // 最大数据分析时间(6秒)

    public DukeKnightEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public void setShouldVanish() {
        this.shouldVanish = true;
    }
    
    // 设置攻击顺序
    public void setAttackOrder(int order) {
        this.attackOrder = order;
    }
    
    // 获取攻击顺序
    public int getAttackOrder() {
        return this.attackOrder;
    }
    
    // 设置是否可以攻击
    public void setCanAttack(boolean canAttack) {
        this.canAttack = canAttack;
    }
    
    // 获取是否可以攻击
    public boolean canAttack() {
        return this.canAttack;
    }
    
    // 设置武器
    public void setWeapon(ItemStack weapon) {
        if (!level().isClientSide) {
            this.setItemInHand(InteractionHand.MAIN_HAND, weapon.copy());
            // 确保物品不会被破坏
            this.getItemInHand(InteractionHand.MAIN_HAND).setCount(1);
        }
    }

    /* ---------- AI ---------- */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && canAttack;
            }
        });
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D) // 基于火龙能量武装的高速移动能力
                .add(Attributes.ATTACK_DAMAGE, 16.0D) // 2倍玩家基础攻击力
                .add(Attributes.FOLLOW_RANGE, 30.0D) // 增强追踪范围
                .add(Attributes.ARMOR, 8.0D) // 添加基础护甲
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D) // 添加护甲韧性
                .build();
    }

    /* ---------- 每刻逻辑 ---------- */
    @Override
    public void tick() {
        super.tick();
        
        // 处理速度加成
        handleSpeedBoost();
        
        // 处理假面骑士Duke基础buff
        handleCombatAnalysis();
        handleInvisibility();
        
        // 每10秒有几率激活战斗数据分析
        if (!combatAnalysisActive && random.nextFloat() < 0.05F && !level().isClientSide) {
            activateCombatAnalysis();
        }
        
        // 每15秒有几率激活光学迷彩
        if (!isInvisible && random.nextFloat() < 0.03F && !level().isClientSide) {
            activateInvisibility();
        }

        if (shouldVanish) {
            vanishTicks++;
            if (level().isClientSide) {
                for (int i = 0; i < 5; i++) {
                    level().addParticle(ParticleTypes.CLOUD,
                            getRandomX(0.5), getRandomY(), getRandomZ(0.5),
                            0, 0, 0);
                }
            }
            if (vanishTicks >= VANISH_DELAY) {
                if (!level().isClientSide && !vanishSoundPlayed) {
                    playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    vanishSoundPlayed = true;
                }
                discard();
                return; // 确保discard后立即返回，防止任何后续逻辑执行
            }
        } else {
            // 重置音效标志
            vanishSoundPlayed = false;
        }
        
        // 减少传送音效冷却计时
        if (teleportCooldownTicks > 0) {
            teleportCooldownTicks--;
        }

        // 限制离开本体5格远
        if (owner != null) {
            double distanceToOwner = distanceTo(owner);
            if (distanceToOwner > 5.0D) {
                // 如果超过5格，传送回主人附近
                double angle = Math.random() * Math.PI * 2;
                double offsetX = Math.sin(angle) * 1.5;
                double offsetZ = Math.cos(angle) * 1.5;
                setPos(owner.getX() + offsetX, owner.getY(), owner.getZ() + offsetZ);
                
                // 播放传送音效
                if (!level().isClientSide && teleportCooldownTicks <= 0) {
                    level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1.0F, 1.0F);
                    teleportCooldownTicks = 20; // 设置20刻(1秒)冷却
                }
            }
        }

        // 锁定主人最近攻击目标
        if (owner != null && !level().isClientSide) {
            LivingEntity target = owner.getLastHurtMob();
            if (target != null && target.isAlive() && distanceTo(target) < 20.0D) {
                setTarget(target);
                noTargetTicks = 0; // 重置无目标计时
            }
        }
        
        // 10秒无攻击目标消失
        if (!shouldVanish && getTarget() == null) {
            noTargetTicks++;
            if (noTargetTicks >= MAX_NO_TARGET_TICKS) {
                setShouldVanish();
            }
        } else {
            noTargetTicks = 0; // 重置无目标计时
        }
        
        // 攻击顺序处理
        if (attackOrder > 0 && !canAttack && !level().isClientSide) {
            if (ATTACK_COOLDOWN <= 0) {
                setCanAttack(true);
            }
        }
    }
    
    // 处理速度加成和残影效果
    private void handleSpeedBoost() {
        // 每秒有10%几率获得速度加成（如果还没有加成）
        if (!isSpeedBoosted && random.nextFloat() < 0.1F && !level().isClientSide) {
            activateSpeedBoost();
        }
        
        // 如果处于速度加成状态
        if (isSpeedBoosted) {
            // 减少剩余时间
            speedBoostTicks--;
            
            // 当移动时创建残影效果
            if (this.getDeltaMovement().lengthSqr() > 0.01 && level().isClientSide) {
                createAfterimages();
            }
            
            // 如果加成时间结束
            if (speedBoostTicks <= 0) {
                deactivateSpeedBoost();
            }
        }
    }
    
    // 激活速度加成
    private void activateSpeedBoost() {
        isSpeedBoosted = true;
        speedBoostTicks = MAX_SPEED_BOOST_TICKS;
        
        // 增加移动速度（基础速度的1.5倍）
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D * 1.5D);
        
        // 播放速度提升音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, this.getSoundSource(), 0.5F, 1.5F);
    }
    
    // 停用速度加成
    private void deactivateSpeedBoost() {
        isSpeedBoosted = false;
        
        // 恢复基础移动速度
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }
    
    // 创建高速移动时的残影效果
    private void createAfterimages() {
        // 每5刻创建一个残影
        if (tickCount % 5 == 0) {
            // 获取实体当前位置和上一个位置之间的中间点
            double prevX = this.xo;
            double prevY = this.yo;
            double prevZ = this.zo;
            double currentX = this.getX();
            double currentY = this.getY();
            double currentZ = this.getZ();
            
            // 创建粒子效果作为残影
            for (int i = 0; i < 3; i++) {
                double alpha = (i + 1) / 4.0; // 从0.25到0.75的透明度
                this.level().addParticle(ParticleTypes.CLOUD, 
                    prevX + (currentX - prevX) * alpha, 
                    prevY + (currentY - prevY) * alpha + 0.5, 
                    prevZ + (currentZ - prevZ) * alpha, 
                    0, 0, 0);
            }
        }
    }

    /* ---------- 交互 ---------- */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        
        // 如果右键点击且主人是该玩家
        if (hand == InteractionHand.MAIN_HAND && player == this.owner) {
            // 发射音速箭矢
            shootSonicArrow();
            return InteractionResult.SUCCESS;
        }
        
        return super.mobInteract(player, hand);
    }
    
    // 发射音速箭矢的方法 - 基于Sonic Volley必杀技
    private void shootSonicArrow() {
        LivingEntity target = this.getTarget();
        if (target != null && !target.isAlive()) {
            return;
        }
        
        // 如果没有目标，尝试锁定主人的攻击目标
        if (target == null && this.owner != null) {
            target = this.owner.getLastHurtMob();
            if (target == null || !target.isAlive() || this.distanceTo(target) > 30.0D) {
                return;
            }
        }
        
        // 创建箭矢实体
        AbstractArrow arrow = new Arrow(this.level(), this.getX(), this.getY() + 1.0D, this.getZ());
        
        // 设置箭矢属性
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - arrow.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        
        // 设置箭矢速度和伤害 - 强化版Sonic Volley
        arrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.8F, 10.0F); // 提升速度，减少扩散
        
        // 基础伤害为原来的1.8倍，如果战斗分析激活则额外增加
        double damageMultiplier = 1.8D;
        if (combatAnalysisActive) {
            damageMultiplier *= 1.3D; // 战斗分析状态下额外增加30%伤害
        }
        arrow.setBaseDamage(arrow.getBaseDamage() * damageMultiplier);
        
        // 添加箭矢到世界
        this.level().addFreshEntity(arrow);
        
        // 播放射击音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARROW_SHOOT, this.getSoundSource(), 1.0F, 1.2F);
        
        // 播放攻击动画
        this.swing(InteractionHand.MAIN_HAND);
        
        // 射击后消失
        this.setShouldVanish();
    }
    
    /* ---------- 攻击 ---------- */
    @Override
    public boolean doHurtTarget(Entity entity) {
        // 触发攻击动画
        this.swing(InteractionHand.MAIN_HAND);
        boolean result = super.doHurtTarget(entity);
        if (result && entity instanceof LivingEntity && !level().isClientSide) {
            setCanAttack(false); // 攻击后设置冷却
            
            // 查找同组的其他骑士，设置他们的攻击顺序
            if (attackOrder > 0 && attackOrder < 3) {
                for (Entity nearbyEntity : level().getEntities(this, this.getBoundingBox().inflate(30.0D))) {
                    if (nearbyEntity instanceof DukeKnightEntity) {
                        DukeKnightEntity otherKnight = (DukeKnightEntity) nearbyEntity;
                        if (otherKnight.getAttackOrder() == attackOrder + 1 && owner == otherKnight.getOwner()) {
                            otherKnight.setCanAttack(true);
                            break;
                        }
                    }
                }
            }
            
            // 攻击一次后就消失
            setShouldVanish();
        }
        return result;
    }

    // 处理战斗数据分析
    private void handleCombatAnalysis() {
        if (combatAnalysisActive) {
            analysisTicks--;
            
            // 战斗分析状态下，每20刻（1秒）在脚部生成粒子效果
            if (level().isClientSide && analysisTicks % 20 == 0) {
                for (int i = 0; i < 4; i++) {
                    level().addParticle(ParticleTypes.ENCHANT, 
                        getX() + (random.nextDouble() - 0.5) * 1.0, 
                        getY(), 
                        getZ() + (random.nextDouble() - 0.5) * 1.0, 
                        0, 0, 0);
                }
            }
            
            if (analysisTicks <= 0) {
                deactivateCombatAnalysis();
            }
        }
    }
    
    // 激活战斗数据分析
    private void activateCombatAnalysis() {
        combatAnalysisActive = true;
        analysisTicks = MAX_ANALYSIS_TICKS;
        
        // 战斗分析状态下，提升攻击力10%
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(16.0D * 1.1D);
        
        // 播放激活音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, this.getSoundSource(), 0.5F, 1.5F);
    }
    
    // 停用战斗数据分析
    private void deactivateCombatAnalysis() {
        combatAnalysisActive = false;
        
        // 恢复基础攻击力
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(16.0D);
    }
    
    // 处理光学迷彩
    private void handleInvisibility() {
        if (isInvisible) {
            invisibilityTicks--;
            
            // 光学迷彩状态下，持续生成粒子效果
            if (level().isClientSide && invisibilityTicks % 10 == 0) {
                for (int i = 0; i < 3; i++) {
                    level().addParticle(ParticleTypes.CLOUD, 
                        getX() + (random.nextDouble() - 0.5) * 0.8, 
                        getY() + random.nextDouble() * 1.5, 
                        getZ() + (random.nextDouble() - 0.5) * 0.8, 
                        0, 0, 0);
                }
            }
            
            if (invisibilityTicks <= 0) {
                deactivateInvisibility();
            }
        }
    }
    
    // 激活光学迷彩
    private void activateInvisibility() {
        isInvisible = true;
        invisibilityTicks = MAX_INVISIBILITY_TICKS;
        
        // 播放激活音效
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PORTAL_AMBIENT, this.getSoundSource(), 0.5F, 1.5F);
    }
    
    // 停用光学迷彩
    private void deactivateInvisibility() {
        isInvisible = false;
    }
    
    /* ---------- 伤害处理 ---------- */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 光学迷彩状态下，有30%几率闪避伤害
        if (isInvisible && random.nextFloat() < 0.3F) {
            return false;
        }
        
        // 战斗分析状态下，减少15%受到的伤害
        if (combatAnalysisActive) {
            amount *= 0.85F;
        }
        
        return super.hurt(source, amount);
    }
    
    /* ---------- NBT ---------- */
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        
        // 读取buff状态
        this.isSpeedBoosted = tag.getBoolean("IsSpeedBoosted");
        this.speedBoostTicks = tag.getInt("SpeedBoostTicks");
        this.isInvisible = tag.getBoolean("IsInvisible");
        this.invisibilityTicks = tag.getInt("InvisibilityTicks");
        this.combatAnalysisActive = tag.getBoolean("CombatAnalysisActive");
        this.analysisTicks = tag.getInt("AnalysisTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        
        // 保存buff状态
        tag.putBoolean("IsSpeedBoosted", this.isSpeedBoosted);
        tag.putInt("SpeedBoostTicks", this.speedBoostTicks);
        tag.putBoolean("IsInvisible", this.isInvisible);
        tag.putInt("InvisibilityTicks", this.invisibilityTicks);
        tag.putBoolean("CombatAnalysisActive", this.combatAnalysisActive);
        tag.putInt("AnalysisTicks", this.analysisTicks);
    }

    /* ---------- GeckLib 动画 ---------- */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> event) {
        if (this.swinging) { // 攻击
            event.getController().setAnimation(RawAnimation.begin().thenPlay("attack"));
            return PlayState.CONTINUE;
        }
        if (event.isMoving()) { // 行走
            event.getController().setAnimation(RawAnimation.begin().thenLoop("walk"));
        } else { // 待机
            event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }
}