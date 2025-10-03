package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.*;

public class DarkKivaSealBarrierEntity extends LivingEntity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SIZE = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EXISTENCE_TICKS = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ON_GROUND = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_OWNER_UUID = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.STRING);

    private static final int MAX_EXISTENCE_TICKS = 300;
    private static final double MIN_SIZE = 1.0D;
    private static final double MAX_SIZE = 10.0D;
    private static final double DEFAULT_SIZE = 3.0D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerId;
    private final Map<UUID, TrappedEntityData> trappedEntities = new HashMap<>();

    private static class TrappedEntityData {
        public final LivingEntity entity;
        public final Vec3 initialPosition;
        public double pullProgress;
        public int trapTicks;

        public TrappedEntityData(LivingEntity entity, Vec3 initialPosition) {
            this.entity = entity;
            this.initialPosition = initialPosition;
            this.pullProgress = 0.0;
            this.trapTicks = 0;
        }
    }

    public DarkKivaSealBarrierEntity(EntityType<? extends DarkKivaSealBarrierEntity> entityType, Level level) {
        super(entityType, level);
        setSize(DEFAULT_SIZE);
        setActive(true);
        setExistenceTicks(0);
        setOnGround(false);
        this.noCulling = true;
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .build();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTIVE, false);
        this.entityData.define(DATA_SIZE, (int)(DEFAULT_SIZE * 100));
        this.entityData.define(DATA_EXISTENCE_TICKS, 0);
        this.entityData.define(DATA_ON_GROUND, false);
        this.entityData.define(DATA_OWNER_UUID, "");
    }

    public void setOwner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.ownerId = owner.getUUID();
            this.entityData.set(DATA_OWNER_UUID, owner.getUUID().toString());
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.ownerId == null) {
            String ownerUUIDStr = this.entityData.get(DATA_OWNER_UUID);
            if (!ownerUUIDStr.isEmpty()) {
                try {
                    this.ownerId = UUID.fromString(ownerUUIDStr);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }

        if (this.ownerId == null) return null;

        // 优先查找玩家
        Player player = level().getPlayerByUUID(this.ownerId);
        if (player != null) return player;

        // 查找其他实体
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(50.0D))) {
            if (entity.getUUID().equals(this.ownerId)) {
                return entity;
            }
        }

        return null;
    }

    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }

    public void setActive(boolean active) {
        this.entityData.set(DATA_ACTIVE, active);
    }

    public double getSize() {
        return this.entityData.get(DATA_SIZE) / 100.0;
    }

    public void setSize(double size) {
        double clampedSize = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size));
        this.entityData.set(DATA_SIZE, (int)(clampedSize * 100));
        this.refreshDimensions();
    }

    public void adjustSize(double delta) {
        setSize(getSize() + delta);
    }

    public int getExistenceTicks() {
        return this.entityData.get(DATA_EXISTENCE_TICKS);
    }

    public void setExistenceTicks(int ticks) {
        this.entityData.set(DATA_EXISTENCE_TICKS, ticks);
    }

    public boolean isOnGround() {
        return this.entityData.get(DATA_ON_GROUND);
    }

    public void setOnGround(boolean onGround) {
        this.entityData.set(DATA_ON_GROUND, onGround);
    }

    @Override
    public void tick() {
        super.tick();

        int currentTicks = getExistenceTicks() + 1;
        setExistenceTicks(currentTicks);

        // 检查是否应该消失
        if (currentTicks >= MAX_EXISTENCE_TICKS || level().isClientSide) {
            if (!level().isClientSide) {
                forceReleaseAllEntities();
                this.discard();
            }
            return;
        }

        // 清理无效的实体
        cleanupInvalidEntities();

        // 应用控制效果
        if (isActive()) {
            findAndTrapNewEntities();
            updateTrappedEntities();
        }

        // 粒子效果
        if (level().isClientSide) {
            spawnParticles();
        }

        // 检查是否落地
        if (!isOnGround() && this.onGround()) {
            setOnGround(true);
            level().playSound(null, this.blockPosition(), SoundEvents.STONE_HIT, SoundSource.BLOCKS, 1.0F, 1.5F);
        }
    }

    private void cleanupInvalidEntities() {
        Iterator<Map.Entry<UUID, TrappedEntityData>> iterator = trappedEntities.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, TrappedEntityData> entry = iterator.next();
            TrappedEntityData data = entry.getValue();
            LivingEntity entity = data.entity;

            if (entity == null || entity.isRemoved() || !entity.isAlive() ||
                    entity == getOwner() || !canTrapEntity(entity)) {
                if (entity != null) {
                    releaseEntityControl(entity);
                }
                iterator.remove();
            }
        }
    }

    private void findAndTrapNewEntities() {
        AABB searchBox = this.getBoundingBox().inflate(getSize());
        List<LivingEntity> nearbyEntities = level().getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> canTrapEntity(entity));

        for (LivingEntity entity : nearbyEntities) {
            UUID entityUUID = entity.getUUID();
            if (!trappedEntities.containsKey(entityUUID)) {
                trapEntity(entity);
            }
        }
    }

    private boolean canTrapEntity(LivingEntity entity) {
        // 不 trapping 自己、主人、已经死亡的实体
        if (entity == this || entity == getOwner() || !entity.isAlive()) {
            return false;
        }

        // 不 trapping 创造模式的玩家
        if (entity instanceof Player player) {
            return !player.getAbilities().instabuild;
        }

        return true;
    }

    private void trapEntity(LivingEntity entity) {
        UUID entityUUID = entity.getUUID();

        // 设置控制标记
        entity.getPersistentData().putBoolean("SealBarrierControlled", true);
        entity.getPersistentData().putUUID("SealBarrierOwner", this.getUUID());

        // 应用控制效果
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.hurtMarked = true;

        // 对于生物，禁用AI
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        // 存储实体数据
        TrappedEntityData data = new TrappedEntityData(entity, entity.position());
        trappedEntities.put(entityUUID, data);

        // 音效
        level().playSound(null, this.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
    }

    private void updateTrappedEntities() {
        for (TrappedEntityData data : trappedEntities.values()) {
            if (data.entity == null) continue;

            LivingEntity entity = data.entity;
            data.trapTicks++;

            // 持续应用控制效果
            entity.setNoGravity(true);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hurtMarked = true;

            // 更新拉扯进度
            data.pullProgress = Math.min(1.0, data.pullProgress + 0.02);

            // 计算目标位置（向结界中心移动）
            Vec3 targetPos = data.initialPosition.lerp(this.position().add(0, 0.5, 0), data.pullProgress);

            // 传送实体到目标位置
            entity.teleportTo(targetPos.x, targetPos.y, targetPos.z);

            // 粒子效果
            if (level().isClientSide) {
                spawnEntityParticles(entity);
            }
        }
    }

    private void spawnEntityParticles(LivingEntity entity) {
        for (int i = 0; i < 2; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 0.4;
            double offsetY = random.nextDouble() * 0.8;
            double offsetZ = (random.nextDouble() - 0.5) * 0.4;
            level().addParticle(ParticleTypes.CRIMSON_SPORE,
                    entity.getX() + offsetX, entity.getY() + offsetY, entity.getZ() + offsetZ,
                    0.0D, 0.02D, 0.0D);
        }
    }

    private void spawnParticles() {
        double size = getSize();
        for (int i = 0; i < 8; i++) {
            double angle = (double) i * Math.PI * 2.0D / 8.0D;
            double x = this.getX() + Math.cos(angle) * size * 0.5D;
            double z = this.getZ() + Math.sin(angle) * size * 0.5D;
            level().addParticle(ParticleTypes.ENCHANT, x, this.getY() + 0.5D, z, 0.0D, 0.05D, 0.0D);
        }
        level().addParticle(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
    }

    private void forceReleaseAllEntities() {
        for (TrappedEntityData data : trappedEntities.values()) {
            if (data.entity != null) {
                releaseEntityControl(data.entity);
            }
        }
        trappedEntities.clear();
    }

    private void releaseEntityControl(LivingEntity entity) {
        if (entity == null) return;

        // 移除控制标记
        entity.getPersistentData().remove("SealBarrierControlled");
        entity.getPersistentData().remove("SealBarrierOwner");

        // 恢复重力
        entity.setNoGravity(false);
        entity.hurtMarked = false;

        // 恢复生物AI
        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }

        // 对玩家的特殊处理
        if (entity instanceof Player player) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, -0.3, 0));
        }
    }

    @Override
    protected AABB makeBoundingBox() {
        double size = getSize();
        return new AABB(
                this.getX() - size, this.getY(), this.getZ() - size,
                this.getX() + size, this.getY() + size, this.getZ() + size
        );
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable((float) getSize(), (float) getSize());
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true; // 允许被选中
    }


    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.setActive(nbt.getBoolean("Active"));
        this.setSize(nbt.getDouble("Size"));
        this.setExistenceTicks(nbt.getInt("ExistenceTicks"));
        this.setOnGround(nbt.getBoolean("OnGround"));
        if (nbt.hasUUID("OwnerId")) {
            this.ownerId = nbt.getUUID("OwnerId");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Active", this.isActive());
        nbt.putDouble("Size", this.getSize());
        nbt.putInt("ExistenceTicks", this.getExistenceTicks());
        nbt.putBoolean("OnGround", this.isOnGround());
        if (this.ownerId != null) {
            nbt.putUUID("OwnerId", this.ownerId);
        }
    }

    public void handleLeftClick() {
        // 加速拉扯进度
        for (TrappedEntityData data : trappedEntities.values()) {
            data.pullProgress = Math.min(1.0, data.pullProgress + 0.3);
        }
        level().playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 1.2F);
    }

    public void handleRightClick() {
        teleportEntitiesToPlayer();
        level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void teleportEntitiesToPlayer() {
        LivingEntity owner = getOwner();
        if (owner == null) return;

        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, TrappedEntityData> entry : trappedEntities.entrySet()) {
            TrappedEntityData data = entry.getValue();
            if (data.entity != null && data.entity.isAlive()) {
                // 计算玩家前方的位置
                Vec3 lookVector = owner.getLookAngle().normalize();
                Vec3 teleportPos = owner.position().add(
                        lookVector.x * 2,
                        owner.getEyeHeight() - 0.5,
                        lookVector.z * 2
                );

                // 传送实体
                data.entity.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                releaseEntityControl(data.entity);
                toRemove.add(entry.getKey());

                // 粒子效果
                if (level().isClientSide) {
                    for (int i = 0; i < 5; i++) {
                        double offsetX = (random.nextDouble() - 0.5) * 1.0;
                        double offsetY = random.nextDouble() * 1.0;
                        double offsetZ = (random.nextDouble() - 0.5) * 1.0;
                        level().addParticle(ParticleTypes.PORTAL,
                                data.entity.getX() + offsetX, data.entity.getY() + offsetY, data.entity.getZ() + offsetZ,
                                0.0D, 0.1D, 0.0D);
                    }
                }
            }
        }

        // 移除已传送的实体
        for (UUID uuid : toRemove) {
            trappedEntities.remove(uuid);
        }
    }

    public boolean isEntityTrapped(Entity entity) {
        return entity instanceof LivingEntity && trappedEntities.containsKey(entity.getUUID());
    }

    public void forceReleaseEntity(LivingEntity entity) {
        if (entity != null) {
            UUID entityUUID = entity.getUUID();
            if (trappedEntities.containsKey(entityUUID)) {
                releaseEntityControl(entity);
                trappedEntities.remove(entityUUID);
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (isOnGround()) {
                state.setAnimation(RawAnimation.begin().then("animation.darkkivasealbarrier.extrusion", Animation.LoopType.HOLD_ON_LAST_FRAME));
            } else {
                state.setAnimation(RawAnimation.begin().then("animation.darkkivasealbarrier.idle", Animation.LoopType.LOOP));
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void remove(RemovalReason reason) {
        // 确保在移除时释放所有实体
        if (!level().isClientSide) {
            forceReleaseAllEntities();
        }
        super.remove(reason);
    }
}