package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.core.BlockPos;
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

public class NecromEyexEntity extends LivingEntity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation FINISH = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private UUID targetPlayerId;
    private int setupTicks = 0; // 添加设置计时器
    private int maxLifespan = 400; // 最大生命周期（以tick为单位，约20秒，增加一倍时间）
    private int lifeTicks = 0; // 生命周期计时器
    private int retryMountTicks = 0; // 骑乘重试计时器
    private boolean isFinish = false; // 控制动画状态的变量
    private int finishTimer = 0; // 动画完成计时器
    
    // 添加调试标记
    private static final boolean DEBUG = true;

    public NecromEyexEntity(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvulnerable(true);

        // 添加以下设置来隐藏实体ID
        this.setCustomNameVisible(false);  // 确保自定义名称不可见
        this.setCustomName(null);  // 清除自定义名称

        // 添加调试日志
        if (DEBUG && !world.isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity created with ID: {}", this.getId());
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

    private PlayState predicate(AnimationState<NecromEyexEntity> event) {
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
        finishTimer++;

        // 调试：显示实体位置和状态
        if (DEBUG && setupTicks % 20 == 0 && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity[{}] tick: pos={}, isPassenger={}, targetPlayerId={}, lifeTicks={}", 
                    this.getId(), this.position(), this.isPassenger(), targetPlayerId, lifeTicks);
        }

        // 检查生命周期，防止无限存在
        if (lifeTicks > maxLifespan && !this.level().isClientSide()) {
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity[{}]: Max lifespan reached, discarding", this.getId());
            }
            // 在服务器端丢弃实体前，确保解除骑乘关系
            if (this.isPassenger()) {
                this.stopRiding();
            }
            this.discard();
            return;
        }

        // 获取目标玩家 - 改进为可以在任何维度找到玩家
        Player targetPlayer = null;
        if (this.targetPlayerId != null && !this.level().isClientSide()) {
            // 遍历所有维度查找玩家
            for (ServerLevel serverLevel : this.level().getServer().getAllLevels()) {
                Entity entity = serverLevel.getPlayerByUUID(this.targetPlayerId);
                if (entity instanceof Player) {
                    targetPlayer = (Player) entity;
                    break;
                }
            }
            
            if (targetPlayer == null && DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.warn("NecromEyexEntity[{}]: Target player not found with UUID: {}", 
                        this.getId(), targetPlayerId);
            }
        } else if (DEBUG && !this.level().isClientSide() && this.targetPlayerId == null) {
            kamen_rider_boss_you_and_me.LOGGER.warn("NecromEyexEntity[{}]: No target player ID set", this.getId());
        }

        if (targetPlayer != null && targetPlayer.isAlive()) {
            // 检查玩家是否还在踢击状态
            boolean isPlayerKicking = targetPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables()).kcik;

            if (!isPlayerKicking) {
                // 玩家已停止踢击，移除实体
                if (DEBUG && !this.level().isClientSide()) {
                    kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity[{}]: Player stopped kicking, discarding", this.getId());
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
                retryMountTicks++;
                // 每5tick尝试一次骑乘，避免过于频繁的尝试
                if (retryMountTicks >= 5) {
                    if (DEBUG && !this.level().isClientSide()) {
                        kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity[{}]: Trying to start riding player {}", 
                                this.getId(), targetPlayer.getScoreboardName());
                    }
                    boolean mounted = this.startRiding(targetPlayer, true);
                    if (DEBUG && mounted && !this.level().isClientSide()) {
                        kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity[{}]: Successfully mounted player {}", 
                                this.getId(), targetPlayer.getScoreboardName());
                    }
                    retryMountTicks = 0;
                }
            } else {
                // 重置重试计数器
                retryMountTicks = 0;
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

            // 添加额外的粒子效果来帮助调试实体位置
            if (DEBUG && this.level().isClientSide()) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        0.0D, 0.1D, 0.0D);
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
                kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity[{}]: No target player found, discarding", this.getId());
            }
            // 在服务器端丢弃实体前，确保解除骑乘关系
            if (this.isPassenger()) {
                this.stopRiding();
            }
            this.discard();
        }


    }



    public void setTargetPlayer(Player player) {
        this.targetPlayerId = player.getUUID();
        // 立即尝试建立骑乘关系
        boolean mounted = this.startRiding(player, true);
        
        // 添加调试日志
        if (DEBUG && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity[{}]: Target player set to: {}, mounted={}", 
                    this.getId(), player.getScoreboardName(), mounted);
        }
    }
    
    // 添加startFinish方法，用于启动动画
    public void startFinish() {
        this.isFinish = true;
        
        // 添加调试日志
        if (DEBUG && !this.level().isClientSide()) {
            kamen_rider_boss_you_and_me.LOGGER.info("NecromEyexEntity[{}]: Animation started", this.getId());
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