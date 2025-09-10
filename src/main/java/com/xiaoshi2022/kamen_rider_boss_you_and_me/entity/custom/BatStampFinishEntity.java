package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.core.BlockPos;
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

public class BatStampFinishEntity extends LivingEntity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("skick");
    private static final RawAnimation FINISH = RawAnimation.begin().thenPlay("skick");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean isFinish = false;
    private int finishTimer = 0;
    private UUID targetPlayerId;
    private int setupTicks = 0; // 添加设置计时器


    public BatStampFinishEntity(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvulnerable(true);
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
        if (isFinish) {
            event.getController().setAnimation(FINISH);
            if (finishTimer > 100) {
                this.discard();
            }
        } else {
            event.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        setupTicks++;

        // 获取目标玩家
        Player targetPlayer = null;
        if (this.targetPlayerId != null) {
            Entity entity = this.level().getPlayerByUUID(this.targetPlayerId);
            if (entity instanceof Player) {
                targetPlayer = (Player) entity;
            }
        }

        if (targetPlayer != null && targetPlayer.isAlive()) {
            // 检查玩家是否还在踢击状态
            boolean isPlayerKicking = targetPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables()).kcik;

            if (!isPlayerKicking) {
                // 玩家已停止踢击，移除实体
                if (!this.level().isClientSide()) {
                    this.discard();
                }
                return;
            }

            // 在前几tick尝试建立骑乘关系
            if (setupTicks < 20 && !this.isPassenger()) {
                this.startRiding(targetPlayer, true);
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

            // 重置设置计时器，防止实体被丢弃
            setupTicks = 0;
        } else if (setupTicks > 40 && !this.level().isClientSide()) {
            // 只有在长时间找不到目标玩家时才移除实体
            this.discard();
        }

        if (isFinish) {
            finishTimer++;
            if (finishTimer > 100 && !this.level().isClientSide()) {
                this.discard();
            }
        }
    }

    public void startFinish() {
        isFinish = true;
        finishTimer = 0;

        // 触发动画播放
        triggerAnim("controller", "skick");

    }


    public void setTargetPlayer(Player player) {
        this.targetPlayerId = player.getUUID();
        // 立即尝试建立骑乘关系
        this.startRiding(player, true);
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
}