package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.misc;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class DimensionalBarrier extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");
    private UUID summonerUUID;
    private int lifetime = 120; // 6秒生命周期

    public DimensionalBarrier(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public DimensionalBarrier(Level level, UUID summonerUUID) {
        super(ModEntityTypes.DIMENSIONAL_BARRIER.get(), level);
        this.summonerUUID = summonerUUID;
        this.noPhysics = true;
    }

    // Entity类默认不会因为距离远而移除，所以不需要覆盖

    @Override
    public void tick() {
        super.tick();
        
        // 减少生命周期
        if (--lifetime <= 0) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        // 保持静止
        this.setDeltaMovement(0, 0, 0);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return true;
    }
    
    @Override
    protected void defineSynchedData() {
        // 不需要同步数据
    }
    
    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        // 读取保存的数据
        if (compound.hasUUID("summonerUUID")) {
            this.summonerUUID = compound.getUUID("summonerUUID");
        }
        this.lifetime = compound.getInt("lifetime");
    }
    
    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        // 保存额外的数据
        if (this.summonerUUID != null) {
            compound.putUUID("summonerUUID", this.summonerUUID);
        }
        compound.putInt("lifetime", this.lifetime);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::idleAnimationPredicate));
    }

    private <E extends GeoEntity> PlayState idleAnimationPredicate(AnimationState<E> event) {
        event.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public UUID getSummonerUUID() {
        return summonerUUID;
    }

    public void setSummonerUUID(UUID summonerUUID) {
        this.summonerUUID = summonerUUID;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    // Entity类不需要Mob相关的生成检查方法
}