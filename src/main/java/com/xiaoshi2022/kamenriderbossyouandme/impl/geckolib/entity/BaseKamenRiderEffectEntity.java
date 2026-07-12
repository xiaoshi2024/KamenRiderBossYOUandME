package com.xiaoshi2022.kamenriderbossyouandme.impl.geckolib.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

public abstract class BaseKamenRiderEffectEntity extends Entity implements GeoEntity {
    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected final String riderName;
    protected final String entityName;

    protected final Map<String, AnimationController<BaseKamenRiderEffectEntity>> controllers = new HashMap<>();

    public BaseKamenRiderEffectEntity(EntityType<?> entityType, Level level,
                                      String riderName, String entityName) {
        super(entityType, level);
        this.riderName = riderName;
        this.entityName = entityName;

        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registerAnimationControllers(registrar);
    }

    protected abstract void registerAnimationControllers(AnimatableManager.ControllerRegistrar registrar);

    protected void addController(AnimatableManager.ControllerRegistrar registrar, String name,
                                 AnimationController<?> controller) {
        controllers.put(name, (AnimationController<BaseKamenRiderEffectEntity>) controller);
        registrar.add(controller);
    }

    @Nullable
    public AnimationController<BaseKamenRiderEffectEntity> getController(String name) {
        return controllers.get(name);
    }

    protected AnimationController<BaseKamenRiderEffectEntity> createLoopController(String animationName) {
        return new AnimationController<>(this, animationName + "_controller", 0, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenLoop(animationName));
            return PlayState.CONTINUE;
        });
    }

    protected AnimationController<BaseKamenRiderEffectEntity> createOnceController(String animationName) {
        return new AnimationController<>(this, animationName + "_controller", 0, state -> {
            state.getController().setAnimation(
                    RawAnimation.begin().then(animationName, Animation.LoopType.HOLD_ON_LAST_FRAME)
            );
            return PlayState.CONTINUE;
        });
    }

    // 资源路径生成 - 使用自己的MODID
    protected ResourceLocation getModelPath() {
        return ResourceLocation.fromNamespaceAndPath(MODID,
                "geo/entity/" + entityName.toLowerCase() + ".geo.json");
    }

    protected ResourceLocation getTexturePath() {
        return ResourceLocation.fromNamespaceAndPath(MODID,
                "textures/entity/" + entityName.toLowerCase() + ".png");
    }

    protected ResourceLocation getAnimationPath() {
        return ResourceLocation.fromNamespaceAndPath(MODID,
                "animations/entity/" + entityName.toLowerCase() + ".animation.json");
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object entity) {
        return tickCount;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {}

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    public boolean fireImmune() {
        return true;
    }

    public float getCurrentAlpha() {
        return 1.0f;
    }

    public boolean shouldApplyTransparency() {
        return false;
    }
}