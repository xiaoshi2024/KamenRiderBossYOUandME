package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.VillagerEvents;
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

public class Gifftarian extends Monster implements GeoEntity {
    protected static final RawAnimation ROUT = RawAnimation.begin().thenPlay("rout");
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("Attack");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public static boolean shouldPlayRoutAnimation = false; // 全局布尔字段


    public Gifftarian(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    public final boolean hurt(DamageSource source, float damageAmount) { // 重写 hurt 方法
        // 调用父类的 hurt 方法
        return super.hurt(source, damageAmount);
    }

    @Override
    protected void registerGoals() {
        // 添加 AI 目标
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.37D, true)); // 近战攻击
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.45D)); // 随机漫步
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F)); // 看向玩家
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this)); // 随机环顾四周
        this.goalSelector.addGoal(7, new FloatGoal(this)); // 如果在水里，尝试浮在水面上
        // 添加目标选择
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this)); // 被攻击时反击

        // 攻击玩家
        this.targetSelector.addGoal(2, new CustomAttackGoal<>(this, Player.class, 20, true, false, CustomAttackGoal::isValidTarget));

        // 攻击普通村民
        this.targetSelector.addGoal(3, new CustomAttackGoal<>(this, Villager.class, 20, true, false, CustomAttackGoal::isValidTarget));
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
        controllers.add(new AnimationController<>(this, "rout", 5, this::routAnimController));
    }

    public PlayState routAnimController(AnimationState<Gifftarian> gifftarianAnimationState) {
        // 直接调用 shouldTransformToGifftarian 方法
        if (VillagerEvents.shouldTransformToGifftarian())
            gifftarianAnimationState.getController().setAnimation(ROUT);
        VillagerEvents.shouldPlayRoutAnimation = false; // 重置全局状态
        return PlayState.CONTINUE;
    }



    protected <E extends Gifftarian> PlayState idleAnimController(final AnimationState<E> event) {
        if (event.isMoving())
            return event.setAndContinue(RUN);
        else
            return event.setAndContinue(IDLE); // 当不移动时播放 idle 动画
    }

    protected <E extends Gifftarian> PlayState attackAnimController(final AnimationState<E> event) {
        if (this.swinging) {
            return event.setAndContinue(ATTACK);
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}