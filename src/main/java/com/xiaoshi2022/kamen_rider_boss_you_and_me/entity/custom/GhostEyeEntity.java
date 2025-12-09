package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class GhostEyeEntity extends LivingEntity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("fly");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    public GhostEyeEntity(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
        this.noPhysics = false; // 允许物理碰撞
        this.setInvulnerable(false); // 不是无敌的

        // 确保实体可见但不显示名称
        this.setCustomNameVisible(false);  // 确保自定义名称不可见
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // 和玩家一样的血量
                .add(Attributes.MOVEMENT_SPEED, 0.3D) // 移动速度
                .add(Attributes.FLYING_SPEED, 0.6D) // 飞行速度
                .build();
    }


    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GhostEyeEntity> event) {
        // 播放idle动画
        event.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();
        
        // 应用阻力
        this.setDeltaMovement(
                this.getDeltaMovement().x * 0.95D,
                this.getDeltaMovement().y * 0.95D,
                this.getDeltaMovement().z * 0.95D
        );
    }
    
    // 辅助方法：限制值的范围
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // 实体受到伤害的处理
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 实体本身处理伤害
        return super.hurt(source, amount);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(Entity entity) {
        super.doPush(entity);
    }

    @Override
    protected void pushEntities() {
        super.pushEntities();
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false; // 不会受到坠落伤害
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return java.util.Collections.emptyList();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
    
    // 保存实体数据
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }
    
    // 加载实体数据
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
    }
    
    @Override
    public boolean isCustomNameVisible() {
        return false; // 隐藏实体名称
    }
    
    // 确保实体ID不显示
    @Override
    public boolean shouldShowName() {
        return false;
    }

}