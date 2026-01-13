package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class NoxSpecialEntity extends LivingEntity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 使用EntityData来同步数据
    private static final EntityDataAccessor<Optional<UUID>> DATA_TARGET_PLAYER = SynchedEntityData.defineId(NoxSpecialEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private int lifetimeTicks = 0;
    private static final int MAX_LIFETIME = 40; // 2秒 = 40刻

    // 相对位置偏移（相对于玩家中心）
    private float yOffset = 0.0f; // 垂直偏移（从玩家脚部开始计算）
    private float forwardOffset = 0.0f; // 前后偏移
    private float sideOffset = 0.0f; // 左右偏移

    public NoxSpecialEntity(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvulnerable(true);
        this.noCulling = true;
        this.setSilent(true); // 静音
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TARGET_PLAYER, Optional.empty());
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D)
                .build();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<NoxSpecialEntity> event) {
        event.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        lifetimeTicks++;

        // 获取目标玩家
        Player targetPlayer = getTargetPlayer();

        if (targetPlayer != null && targetPlayer.isAlive()) {
            // 计算附体位置（相对于玩家的位置和旋转）
            calculateAttachmentPosition(targetPlayer);

            // 同步旋转
            this.setYRot(targetPlayer.getYRot());
            this.setXRot(targetPlayer.getXRot());
            this.setYHeadRot(targetPlayer.getYHeadRot());

            // 同步渲染旋转
            this.yRotO = targetPlayer.yRotO;
            this.xRotO = targetPlayer.xRotO;
            this.yHeadRotO = targetPlayer.yHeadRotO;

            // 同步姿态和姿势
            this.setPose(targetPlayer.getPose());
            this.setShiftKeyDown(targetPlayer.isShiftKeyDown());

            // 同步玩家的高度变化（如蹲下、游泳）
            syncPlayerHeight(targetPlayer);

            // 跟随玩家维度变化
            if (this.level() != targetPlayer.level()) {
                this.changeDimension((ServerLevel) targetPlayer.level());
            }

        } else if (!this.level().isClientSide()) {
            // 如果目标玩家不存在或死亡，立即销毁
            this.discard();
            return;
        }

        // 生命周期结束，自动销毁
        if (lifetimeTicks >= MAX_LIFETIME && !this.level().isClientSide()) {
            this.discard();
        }
    }

    /**
     * 计算附着在玩家身上的位置
     */
    private void calculateAttachmentPosition(Player player) {
        // 获取玩家的前方方向向量
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        // 将角度转换为弧度
        double yawRad = Math.toRadians(-yaw);
        double pitchRad = Math.toRadians(-pitch);

        // 计算方向向量（只使用Yaw，忽略Pitch的影响）
        double lookX = Math.sin(yawRad);
        double lookZ = Math.cos(yawRad);

        // 计算玩家的右侧向量（与视线垂直）
        double rightX = Math.cos(yawRad);
        double rightZ = -Math.sin(yawRad);

        // 计算位置：
        // 1. 玩家脚部位置（Y坐标）
        double baseX = player.getX();
        double baseY = player.getY(); // 直接使用玩家的Y坐标（脚部位置）
        double baseZ = player.getZ();

        // 2. 向前偏移（水平方向）
        baseX += lookX * forwardOffset;
        baseZ += lookZ * forwardOffset;

        // 3. 向右侧偏移（水平方向）
        baseX += rightX * sideOffset;
        baseZ += rightZ * sideOffset;

        // 4. 垂直偏移（从脚部开始）
        baseY += yOffset;

        // 设置实体位置
        this.setPos(baseX, baseY, baseZ);

        // 同步上一帧位置以实现平滑渲染
        if (this.tickCount == 1) {
            this.xo = baseX;
            this.yo = baseY;
            this.zo = baseZ;
        }
    }

    /**
     * 同步玩家的高度变化
     */
    private void syncPlayerHeight(Player player) {
        // 根据玩家姿态调整特效高度
        switch (player.getPose()) {
            case SWIMMING:
            case FALL_FLYING:
            case SPIN_ATTACK:
                // 在这些姿势下，玩家可能会改变高度
                // 保持与玩家相同的相对高度
                break;
            case CROUCHING:
                // 蹲下时，玩家高度降低
                // 我们的特效会跟随玩家降低高度
                break;
            default:
                // 站立时保持正常高度
                break;
        }
    }

    /**
     * 获取目标玩家
     */
    @Nullable
    public Player getTargetPlayer() {
        Optional<UUID> uuid = this.entityData.get(DATA_TARGET_PLAYER);
        if (uuid.isPresent()) {
            Entity entity = this.level().getPlayerByUUID(uuid.get());
            if (entity instanceof Player) {
                return (Player) entity;
            }
        }
        return null;
    }

    /**
     * 设置目标玩家
     */
    public void setTargetPlayer(Player player) {
        this.entityData.set(DATA_TARGET_PLAYER, Optional.of(player.getUUID()));

        // 设置默认偏移（可调整）
        this.yOffset = player.getBbHeight() * 0.5f; // 默认在玩家腰部高度
        this.forwardOffset = 0.0f;  // 在玩家前方
        this.sideOffset = 0.0f;     // 不偏移

        // 立即同步初始位置
        if (!this.level().isClientSide()) {
            calculateAttachmentPosition(player);
            this.setYRot(player.getYRot());
            this.setXRot(player.getXRot());
        }
    }

    /**
     * 设置附体偏移
     * @param yOffset 垂直偏移（从玩家脚部开始，正值向上）
     * @param forwardOffset 前后偏移（正值向前）
     * @param sideOffset 左右偏移（正值向右）
     */
    public void setAttachmentOffset(float yOffset, float forwardOffset, float sideOffset) {
        this.yOffset = yOffset;
        this.forwardOffset = forwardOffset;
        this.sideOffset = sideOffset;
    }

    /**
     * 设置相对于玩家身高的百分比偏移
     * @param heightPercentage 高度百分比（0.0 = 脚部，1.0 = 头部，0.5 = 腰部）
     */
    public void setHeightPercentage(float heightPercentage) {
        Player player = getTargetPlayer();
        if (player != null) {
            this.yOffset = player.getBbHeight() * heightPercentage;
        }
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
            this.entityData.set(DATA_TARGET_PLAYER, Optional.of(tag.getUUID("TargetPlayer")));
        }
        lifetimeTicks = tag.getInt("LifetimeTicks");
        yOffset = tag.getFloat("YOffset");
        forwardOffset = tag.getFloat("ForwardOffset");
        sideOffset = tag.getFloat("SideOffset");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        Optional<UUID> uuid = this.entityData.get(DATA_TARGET_PLAYER);
        uuid.ifPresent(value -> tag.putUUID("TargetPlayer", value));
        tag.putInt("LifetimeTicks", lifetimeTicks);
        tag.putFloat("YOffset", yOffset);
        tag.putFloat("ForwardOffset", forwardOffset);
        tag.putFloat("SideOffset", sideOffset);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // 不处理摔落伤害
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
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        // 确保实体生成时不会被裁剪
        this.setBoundingBox(this.getBoundingBox().inflate(0.5));
    }

    // 添加：确保实体不会被其他实体推动
    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    /**
     * 创建并生成附体特效实体
     */
    public static NoxSpecialEntity createAndAttach(Level level, Player player) {
        NoxSpecialEntity entity = new NoxSpecialEntity(
                ModEntityTypes.NOX_SPECIAL.get(),
                level
        );

        // 设置位置和旋转
        entity.setPos(player.getX(), player.getY(), player.getZ());
        entity.setYRot(player.getYRot());
        entity.setXRot(player.getXRot());

        // 设置目标玩家
        entity.setTargetPlayer(player);

        // 添加到世界
        level.addFreshEntity(entity);

        return entity;
    }
}