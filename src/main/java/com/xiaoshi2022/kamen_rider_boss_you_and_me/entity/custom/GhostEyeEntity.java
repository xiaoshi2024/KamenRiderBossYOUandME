package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
    
    // 存储控制这个实体的玩家UUID
    private UUID controllingPlayerId;
    private Player controllingPlayer;
    
    // 获取控制玩家的UUID
    public UUID getControllingPlayerId() {
        return controllingPlayerId;
    }
    
    public GhostEyeEntity(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
        this.noPhysics = false; // 允许物理碰撞
        this.setInvulnerable(false); // 不是无敌的
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D) // 和玩家一样的血量
                .add(Attributes.MOVEMENT_SPEED, 0.3D) // 移动速度
                .add(Attributes.FLYING_SPEED, 0.6D) // 飞行速度
                .build();
    }

    // 设置控制这个实体的玩家
    public void setControllingPlayer(Player player) {
        this.controllingPlayerId = player.getUUID();
        this.controllingPlayer = player;
        // 同步玩家的生命值到实体
        this.setHealth(player.getHealth());
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
        
        // 如果是客户端，找到控制这个实体的玩家
        if (this.level().isClientSide()) {
            if (this.controllingPlayer == null && this.controllingPlayerId != null) {
                Entity entity = this.level().getPlayerByUUID(this.controllingPlayerId);
                if (entity instanceof Player) {
                    this.controllingPlayer = (Player) entity;
                }
            }
        }
        
        // 处理飞行移动
        if (this.controllingPlayer != null && this.controllingPlayer.isAlive()) {
            // 同步玩家的生命值变化
            if (!this.level().isClientSide()) {
                this.setHealth(this.controllingPlayer.getHealth());
            }
            
            // 飞行移动逻辑
            if (this.isInWater()) {
                // 在水中的移动
                this.setDeltaMovement(this.getDeltaMovement().add(
                        this.controllingPlayer.xxa * 0.03D, 
                        this.controllingPlayer.yya * 0.03D, 
                        this.controllingPlayer.zza * 0.03D));
            } else {
                // 在空中的飞行移动
                this.setDeltaMovement(this.getDeltaMovement().add(
                        this.controllingPlayer.xxa * 0.03D, 
                        this.controllingPlayer.yya * 0.03D, 
                        this.controllingPlayer.zza * 0.03D));
            }
            
            // 限制速度
            double maxSpeed = this.getAttributeValue(Attributes.FLYING_SPEED);
            this.setDeltaMovement(
                    clamp(this.getDeltaMovement().x, -maxSpeed, maxSpeed),
                    clamp(this.getDeltaMovement().y, -maxSpeed, maxSpeed),
                    clamp(this.getDeltaMovement().z, -maxSpeed, maxSpeed)
            );
            
            // 应用阻力
            this.setDeltaMovement(
                    this.getDeltaMovement().x * 0.95D,
                    this.getDeltaMovement().y * 0.95D,
                    this.getDeltaMovement().z * 0.95D
            );
            
            // 同步玩家的旋转
            this.setYRot(this.controllingPlayer.getYRot());
            this.yRotO = this.controllingPlayer.getYRot();
            this.setXRot(this.controllingPlayer.getXRot());
            this.xRotO = this.controllingPlayer.getXRot();
        } else if (!this.level().isClientSide()) {
            // 如果找不到控制玩家，移除实体
            this.discard();
        }
    }
    
    // 辅助方法：限制值的范围
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // 实体受到伤害时，将伤害传递给控制玩家
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.controllingPlayer != null && !this.level().isClientSide()) {
            // 将伤害传递给控制玩家
            this.controllingPlayer.hurt(source, amount);
            // 同步玩家的生命值
            this.setHealth(this.controllingPlayer.getHealth());
            // 如果玩家死亡，移除实体
            if (!this.controllingPlayer.isAlive()) {
                this.discard();
            }
        }
        return false; // 实体本身不会受到伤害
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
        if (this.controllingPlayerId != null) {
            compound.putUUID("ControllingPlayer", this.controllingPlayerId);
        }
    }
    
    // 加载实体数据
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("ControllingPlayer")) {
            this.controllingPlayerId = compound.getUUID("ControllingPlayer");
        }
    }
}