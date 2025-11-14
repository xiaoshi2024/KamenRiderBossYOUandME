package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class DarkGhostRiderKickEntity extends LivingEntity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private UUID targetPlayerId;
    private int setupTicks = 0;
    private int maxLifespan = 400;
    private int lifeTicks = 0;
    private boolean isFinish = false;
    private int finishTimer = 0;

    private static final boolean DEBUG = true;

    public DarkGhostRiderKickEntity(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvulnerable(true);
        this.setCustomNameVisible(false);
        this.setCustomName(null);

        if (DEBUG && !world.isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("DarkGhostRiderKickEntity created with ID: {}", this.getId());
        }
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D).build();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<DarkGhostRiderKickEntity> event) {
        if (this.isRemoved()) {
            return PlayState.STOP;
        }

        event.getController().setAnimation(IDLE);

        if (isFinish && finishTimer > 100 && !this.level().isClientSide()) {
            this.discard();
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        if (this.isRemoved()) {
            return;
        }

        super.tick();

        setupTicks++;
        lifeTicks++;
        finishTimer++;

        // 调试信息
        if (DEBUG && setupTicks % 20 == 0 && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("DarkGhostRiderKickEntity[{}] tick: pos={}, targetPlayerId={}, lifeTicks={}",
                    this.getId(), this.position(), targetPlayerId, lifeTicks);
        }

        // 检查生命周期
        if (lifeTicks > maxLifespan && !this.level().isClientSide()) {
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("DarkGhostRiderKickEntity[{}]: Max lifespan reached, discarding", this.getId());
            }
            this.discard();
            return;
        }

        // 获取目标玩家
        Player targetPlayer = null;
        if (this.targetPlayerId != null && !this.level().isClientSide()) {
            for (ServerLevel serverLevel : this.level().getServer().getAllLevels()) {
                Entity entity = serverLevel.getPlayerByUUID(this.targetPlayerId);
                if (entity instanceof Player) {
                    targetPlayer = (Player) entity;
                    break;
                }
            }

            if (targetPlayer == null && DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.warn("DarkGhostRiderKickEntity[{}]: Target player not found with UUID: {}",
                        this.getId(), targetPlayerId);
            }
        }

        if (targetPlayer != null && targetPlayer.isAlive()) {
            // 检查玩家是否还在踢击状态
            boolean isPlayerKicking = targetPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables()).kick;

            if (!isPlayerKicking) {
                // 玩家已停止踢击，移除实体
                if (DEBUG && !this.level().isClientSide()) {
                    kamen_rider_boss_you_and_me.LOGGER.info("DarkGhostRiderKickEntity[{}]: Player stopped kicking, discarding", this.getId());
                }
                if (!this.level().isClientSide()) {
                    this.discard();
                }
                return;
            }

            // 关键修改：确保不建立骑乘关系
            if (this.isPassenger()) {
                this.stopRiding();
            }
            if (targetPlayer.isPassenger() && targetPlayer.getVehicle() == this) {
                targetPlayer.stopRiding();
            }

            // 同步旋转
            this.setYRot(targetPlayer.getYRot());
            this.yRotO = targetPlayer.getYRot();
            this.setXRot(targetPlayer.getXRot() * 0.5F);

            // 关键修改：简化位置跟随，不干扰玩家移动
            // 在玩家前方显示特效，但不影响玩家物理
            double distance = 1.5;
            Vec3 lookVec = targetPlayer.getLookAngle();

            this.setPos(targetPlayer.getX() + lookVec.x * distance,
                    targetPlayer.getY() + 0.5,
                    targetPlayer.getZ() + lookVec.z * distance);

            // 给玩家添加效果
            if (!targetPlayer.level().isClientSide()) {
                targetPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
            }

            // 粒子效果
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL,
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        2, 0.2, 0.2, 0.2, 0.01);
            }

            // 重置设置计时器
            setupTicks = 0;
        } else if (setupTicks > 40 && !this.level().isClientSide()) {
            // 长时间找不到目标玩家时移除实体
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("DarkGhostRiderKickEntity[{}]: No target player found, discarding", this.getId());
            }
            this.discard();
        }
    }

    public void setTargetPlayer(Player player) {
        this.targetPlayerId = player.getUUID();

        // 关键修改：不建立骑乘关系
        if (DEBUG && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("DarkGhostRiderKickEntity[{}]: Target player set to: {} (NO MOUNTING)",
                    this.getId(), player.getScoreboardName());
        }
    }

    public void startFinish() {
        this.isFinish = true;

        if (DEBUG && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("DarkGhostRiderKickEntity[{}]: Animation started", this.getId());
        }
    }

    public UUID getTargetPlayerId() {
        return targetPlayerId;
    }

    public void setTargetPlayerId(UUID playerId) {
        this.targetPlayerId = playerId;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
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

    @Override
    protected void blockedByShield(LivingEntity p_21015_) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("TargetPlayer")) {
            this.targetPlayerId = tag.getUUID("TargetPlayer");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetPlayerId != null) {
            tag.putUUID("TargetPlayer", this.targetPlayerId);
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public Component getCustomName() {
        return null;
    }

    @Override
    public Component getName() {
        return Component.empty();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }
}