package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class BatStampFinishEntity extends LivingEntity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("skick");
    private static final RawAnimation FINISH = RawAnimation.begin().thenPlay("skick");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean isFinish = false;
    private int finishTimer = 0;
    private UUID targetPlayerId;
    private int setupTicks = 0; // 添加设置计时器
    private int maxLifespan = 200; // 最大生命周期（以tick为单位，约10秒）
    private int lifeTicks = 0; // 生命周期计时器
    
    // 添加调试标记
    private static final boolean DEBUG = true;

    public BatStampFinishEntity(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvulnerable(true);
        
        // 添加调试日志
        if (DEBUG && !world.isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity created with ID: {}", this.getId());
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

    private PlayState predicate(AnimationState<BatStampFinishEntity> event) {
        // 检查实体是否已经被标记为删除，如果是则不再执行任何逻辑
        if (this.isRemoved()) {
            return PlayState.STOP;
        }
        
        if (isFinish) {
            event.getController().setAnimation(FINISH);
            if (finishTimer > 100 && !this.level().isClientSide()) {
                // 在服务器端丢弃实体前，确保解除骑乘关系
                if (this.isPassenger()) {
                    this.stopRiding();
                }
                this.discard();
            }
        } else {
            event.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        // 检查实体是否已经被标记为删除，如果是则不再执行任何逻辑
        if (this.isRemoved()) {
            return;
        }

        super.tick();

        setupTicks++;
        lifeTicks++;

        // 调试：显示实体位置和状态
        if (DEBUG && setupTicks % 20 == 0 && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}] tick: pos={}, isPassenger={}, targetPlayerId={}, lifeTicks={}", 
                    this.getId(), this.position(), this.isPassenger(), targetPlayerId, lifeTicks);
        }

        // 检查生命周期，防止无限存在
        if (lifeTicks > maxLifespan && !this.level().isClientSide()) {
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}]: Max lifespan reached, discarding", this.getId());
            }
            // 在服务器端丢弃实体前，确保解除骑乘关系
            if (this.isPassenger()) {
                this.stopRiding();
            }
            this.discard();
            return;
        }

        // 获取目标玩家
        Player targetPlayer = null;
        if (this.targetPlayerId != null) {
            Entity entity = this.level().getPlayerByUUID(this.targetPlayerId);
            if (entity instanceof Player) {
                targetPlayer = (Player) entity;
            } else {
                if (DEBUG && !this.level().isClientSide()) {
                    kamen_rider_boss_you_and_me.LOGGER.warn("BatStampFinishEntity[{}]: Target player not found with UUID: {}", 
                            this.getId(), targetPlayerId);
                }
            }
        } else {
            if (DEBUG && !this.level().isClientSide()) {
                kamen_rider_boss_you_and_me.LOGGER.warn("BatStampFinishEntity[{}]: No target player ID set", this.getId());
            }
        }

        if (targetPlayer != null && targetPlayer.isAlive()) {
            // 检查玩家是否还在踢击状态
            boolean isPlayerKicking = targetPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables()).kick;

            if (!isPlayerKicking) {
                // 玩家已停止踢击，移除实体
                if (DEBUG && !this.level().isClientSide()) {
                    kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}]: Player stopped kicking, discarding", this.getId());
                }
                if (!this.level().isClientSide()) {
                    // 在服务器端丢弃实体前，确保解除骑乘关系
                    if (this.isPassenger()) {
                        this.stopRiding();
                    }
                    this.discard();
                }
                return;
            }

            // 持续尝试建立骑乘关系，直到成功
            if (!this.isPassenger()) {
                if (DEBUG && !this.level().isClientSide() && setupTicks % 10 == 0) {
                    kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}]: Trying to start riding player {}", 
                            this.getId(), targetPlayer.getScoreboardName());
                }
                boolean mounted = this.startRiding(targetPlayer, true);
                if (DEBUG && mounted && !this.level().isClientSide()) {
                    kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}]: Successfully mounted player {}", 
                            this.getId(), targetPlayer.getScoreboardName());
                }
            }

            // 同步旋转
            this.setYRot(targetPlayer.getYRot());
            this.yRotO = targetPlayer.getYRot();
            this.setXRot(targetPlayer.getXRot() * 0.5F);

            // 同步位置（跟随玩家）
            if (!this.isPassenger()) {
                // 调整位置 - 在玩家前方
                double distance = 1.5;
                double radians = Math.toRadians(targetPlayer.getYRot());
                double offsetX = -Math.sin(radians) * distance;
                double offsetZ = Math.cos(radians) * distance;

                this.setPos(targetPlayer.getX() + offsetX,
                        targetPlayer.getY() + targetPlayer.getEyeHeight() - 0.5,
                        targetPlayer.getZ() + offsetZ);
            }

            // 给玩家添加效果
            if (!targetPlayer.level().isClientSide()) {
                targetPlayer.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
            }

            // 播放调试粒子效果
            if (DEBUG && this.level().isClientSide()) {
                this.level().addParticle(ParticleTypes.END_ROD, 
                        this.getX(), this.getY() + 0.5, this.getZ(), 
                        0.0D, 0.0D, 0.0D);
            }

            // 重置设置计时器
            setupTicks = 0;
        } else if (setupTicks > 40 && !this.level().isClientSide()) {
            // 只有在长时间找不到目标玩家时才移除实体
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}]: No target player found, discarding", this.getId());
            }
            // 在服务器端丢弃实体前，确保解除骑乘关系
            if (this.isPassenger()) {
                this.stopRiding();
            }
            this.discard();
        }

        if (isFinish) {
            finishTimer++;
            if (finishTimer > 100 && !this.level().isClientSide()) {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}]: Finish timer expired, discarding", this.getId());
                }
                // 在服务器端丢弃实体前，确保解除骑乘关系
                if (this.isPassenger()) {
                    this.stopRiding();
                }
                this.discard();
            }
        }
    }

    public void startFinish() {
        setFinishAnimation(true);

        // 服务端同步到所有客户端
        if (!this.level().isClientSide()) {
            // 使用专用的动画数据包同步动画到所有客户端
            PacketHandler.sendToAllTracking(
                    new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.BatStampFinishAnimationPacket(
                            this.getId(), 
                            true
                    ),
                    this
            );
        }
        
        // 添加调试日志
        if (DEBUG && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}]: Starting finish animation", this.getId());
        }
    }
    
    /**
     * 设置动画状态，供数据包处理使用
     */
    public void setFinishAnimation(boolean start) {
        this.isFinish = start;
        this.finishTimer = 0;
        
        // 触发动画播放
        triggerAnim("controller", "skick");
    }

    public void setTargetPlayer(Player player) {
        this.targetPlayerId = player.getUUID();
        // 立即尝试建立骑乘关系
        boolean mounted = this.startRiding(player, true);
        
        // 添加调试日志
        if (DEBUG && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("BatStampFinishEntity[{}]: Target player set to: {}, mounted={}", 
                    this.getId(), player.getScoreboardName(), mounted);
        }
    }

    // 添加getTargetPlayerId方法，供KickendProcedure使用
    public UUID getTargetPlayerId() {
        return targetPlayerId;
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
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // 不处理摔落伤害
    }

    @Override
    public boolean isNoGravity() {
        return true; // 禁用重力
    }

    @Override
    public boolean canBeCollidedWith() {
        return false; // 不可被碰撞
    }

    @Override
    public boolean isPickable() {
        return false; // 不可被拾取/选择
    }
    
    @Override
    public boolean isCustomNameVisible() {
        return false; // 隐藏实体ID显示
    }
}