package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
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

import java.util.Arrays;

public class ElementaryInvesHelheim extends Monster implements GeoEntity {
    protected static final RawAnimation GOMAD = RawAnimation.begin().thenPlay("gomad");
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("Attack");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final String[] VARIANTS = {"red", "blue", "green"};
    private String variant = "blue"; // 默认变种

    public ElementaryInvesHelheim(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public void setVariant(String variant) {
        if (Arrays.asList(VARIANTS).contains(variant)) {
            this.variant = variant;
        }
    }

    public String getVariant() {
        return variant;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Variant", Tag.TAG_STRING)) {
            this.variant = compound.getString("Variant");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Variant", this.variant);
    }

    @Override
    public final boolean hurt(DamageSource source, float damageAmount) { // 重写 hurt 方法
        // 调用父类的 hurt 方法
        return super.hurt(source, damageAmount);
    }

    @Override
    protected void registerGoals() {
        // 添加 AI 目标
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.34D, true)); // 近战攻击
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.45D)); // 随机漫步
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F)); // 看向玩家
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this)); // 随机环顾四周
        this.goalSelector.addGoal(7, new FloatGoal(this)); // 如果在水里，尝试浮在水面上
        // 添加目标选择
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this)); // 被攻击时反击

        // 攻击玩家
        this.targetSelector.addGoal(2, new ElementaryInvesHelheim.CustomAttackGoal<>(this, Player.class, 20, true, false, ElementaryInvesHelheim.CustomAttackGoal::isValidTarget));

        // 攻击普通村民
        this.targetSelector.addGoal(3, new ElementaryInvesHelheim.CustomAttackGoal<>(this, Villager.class, 20, true, false, ElementaryInvesHelheim.CustomAttackGoal::isValidTarget));
    }

    // 自定义攻击目标逻辑
    private static class CustomAttackGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public CustomAttackGoal(Mob mob, Class<T> targetClass, int chance, boolean checkSight, boolean onlyNearby, java.util.function.Predicate<LivingEntity> targetPredicate) {
            super(mob, targetClass, chance, checkSight, onlyNearby, targetPredicate);
        }

        // 静态方法，用于目标验证
        private static boolean isValidTarget(LivingEntity target) {
            // 如果目标是 GiifuDemosEntity 或 StoriousEntity，则不攻击
            if (target instanceof GiifuDemosEntity || target instanceof StoriousEntity) {
                return false;
            }

            // 如果目标是普通村民或玩家，则攻击
            return target instanceof Player || target instanceof Villager;
        }
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "run", 5, this::idleAnimController));
        controllers.add(new AnimationController<>(this, "Attack", 5, this::attackAnimController));
    }


    protected <E extends ElementaryInvesHelheim> PlayState idleAnimController(final AnimationState<E> event) {
        if (event.isMoving())
            return event.setAndContinue(RUN);
        else
            return event.setAndContinue(IDLE); // 当不移动时播放 idle 动画
    }

    protected <E extends ElementaryInvesHelheim> PlayState attackAnimController(final AnimationState<E> event) {
        if (this.swinging) {
            if (getRandom().nextInt(100) < 40) { // 40% 概率触发凶暴
            return event.setAndContinue(GOMAD);
        }else if (getRandom().nextInt(100) < 70){ // 70% 概率触发普通攻击
            return event.setAndContinue(ATTACK);
            }
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}