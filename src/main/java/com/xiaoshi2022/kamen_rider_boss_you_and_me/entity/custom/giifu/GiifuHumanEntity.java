package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GiifuHumanEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    protected static final RawAnimation SPECIAL_ATTACK = RawAnimation.begin().thenPlay("special_attack");
    protected static final RawAnimation DEATH = RawAnimation.begin().thenPlay("death");
    
    private int specialAttackCooldown = 0;
    
    public GiifuHumanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
    }
    
    // 设置实体属性
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 20.0D);
    }
    
    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // 战斗相关目标
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new SpecialAttackGoal(this));
        
        // 移动和观察目标
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new FloatGoal(this));
        
        // 目标选择
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
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
            this.attackTick = 40; // 准备时间
            giifu.specialAttackCooldown = 100; // 冷却时间
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
        
        // 更新特殊攻击冷却
        if (this.specialAttackCooldown > 0) {
            this.specialAttackCooldown--;
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