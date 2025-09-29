package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DarkKivaSealBarrierEntity extends LivingEntity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SIZE = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EXISTENCE_TICKS = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ON_GROUND = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_OWNER_UUID = SynchedEntityData.defineId(DarkKivaSealBarrierEntity.class, EntityDataSerializers.STRING);

    private static final int MAX_EXISTENCE_TICKS = 300; // 15 seconds
    private static final double MIN_SIZE = 1.0D;
    private static final double MAX_SIZE = 10.0D; // 增加范围，原来为5.0D
    private static final double DEFAULT_SIZE = 3.0D; // 增加默认大小，原来为2.0D

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerId;
    private final Map<UUID, LivingEntity> trappedEntities = new HashMap<>(); // 存储被控制的实体
    private final Map<UUID, Vec3> initialPositions = new HashMap<>(); // 存储实体的初始位置
    private final Map<UUID, Double> pullProgress = new HashMap<>(); // 存储拉扯进度

    public DarkKivaSealBarrierEntity(EntityType<? extends DarkKivaSealBarrierEntity> entityType, Level level) {
        super(entityType, level);
        setSize(DEFAULT_SIZE);
        setActive(true);
        setExistenceTicks(0);
        setOnGround(false);
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D).build();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTIVE, false);
        this.entityData.define(DATA_SIZE, (int)(DEFAULT_SIZE * 100));
        this.entityData.define(DATA_EXISTENCE_TICKS, 0);
        this.entityData.define(DATA_ON_GROUND, false);
        this.entityData.define(DATA_OWNER_UUID, UUID.randomUUID().toString());
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
            return null;
        }
        return level().getPlayerByUUID(this.ownerId);
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
        // Update hitbox when size changes
        this.setBoundingBox(this.makeBoundingBox());
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

        // Update existence ticks
        int currentTicks = getExistenceTicks() + 1;
        setExistenceTicks(currentTicks);

        // Check if the entity should expire
        if (currentTicks >= MAX_EXISTENCE_TICKS) {
            // 释放所有被控制的实体
            for (LivingEntity entity : trappedEntities.values()) {
                releaseEntityControl(entity);
            }
            trappedEntities.clear();
            initialPositions.clear();
            pullProgress.clear();
            
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        // Apply高级粘性效果和控制敌人
        if (isActive()) {
            AABB searchBox = this.getBoundingBox().inflate(getSize());
            List<LivingEntity> nearbyEntities = level().getEntitiesOfClass(LivingEntity.class, searchBox, 
                    entity -> entity != this && entity != getOwner() && 
                            !(entity instanceof Player) && 
                            this.canAttack(entity));

            // 处理新进入范围的实体
            for (LivingEntity entity : nearbyEntities) {
                UUID entityUUID = entity.getUUID();
                if (!trappedEntities.containsKey(entityUUID)) {
                    // 捕获实体并保存初始位置
                    trapEntity(entity);
                    initialPositions.put(entityUUID, entity.position());
                    pullProgress.put(entityUUID, 0.0);
                    
                    // 吸引空中实体向下
                    if (!entity.onGround()) {
                        Vec3 pullDown = new Vec3(0, -0.3, 0); // 向下拉力
                        entity.setDeltaMovement(entity.getDeltaMovement().add(pullDown));
                    }
                }
            }

            // 更新所有被控制实体的状态
            updateTrappedEntities();
        }

        // Spawn particles
        if (level().isClientSide) {
            spawnParticles();
        }

        // Check if on ground for extrusion animation
        if (!isOnGround() && this.onGround()) {
            setOnGround(true);
            // Trigger extrusion animation
            level().playSound(null, this.blockPosition(), SoundEvents.STONE_HIT, SoundSource.BLOCKS, 1.0F, 1.5F);
        }
    }

    // 捕获实体并施加控制效果
    private void trapEntity(LivingEntity entity) {
        UUID entityUUID = entity.getUUID();
        trappedEntities.put(entityUUID, entity);
        
        // 施加减速和控制效果
        entity.setNoGravity(true); // 取消重力效果，让实体悬浮
        entity.setDeltaMovement(Vec3.ZERO); // 停止移动
        entity.hurtMarked = true; // 防止实体被击退
        
        // 播放捕获音效
        level().playSound(null, this.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
    }

    // 释放实体控制
    private void releaseEntityControl(LivingEntity entity) {
        entity.setNoGravity(false);
        entity.hurtMarked = false;
        
        // 播放释放音效
        level().playSound(null, this.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.5F, 1.2F);
    }

    // 更新所有被控制实体的状态
    private void updateTrappedEntities() {
        // 使用迭代器避免并发修改异常
        for (Iterator<Map.Entry<UUID, LivingEntity>> it = trappedEntities.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, LivingEntity> entry = it.next();
            UUID entityUUID = entry.getKey();
            LivingEntity entity = entry.getValue();
            
            // 检查实体是否仍然有效
            if (entity.isRemoved() || !this.canAttack(entity)) {
                releaseEntityControl(entity);
                it.remove();
                initialPositions.remove(entityUUID);
                pullProgress.remove(entityUUID);
                continue;
            }
            
            // 持续施加控制效果
            entity.setNoGravity(true);
            
            // 应用粘性效果 - 阻止实体移动
            if (!entity.getType().getCategory().isFriendly()) {
                // 使用更现代的方式禁用AI
                entity.getPersistentData().putBoolean("NoAI", true);
            }
            
            // 根据拉扯进度定位实体
            double progress = pullProgress.getOrDefault(entityUUID, 0.0);
            Vec3 initialPos = initialPositions.get(entityUUID);
            Vec3 sealPos = this.position();
            
            // 计算当前应该在的位置
            Vec3 targetPos = initialPos.lerp(sealPos, progress);
            
            // 设置实体位置，保持Y轴略微高于封印实体
            entity.teleportTo(targetPos.x, sealPos.y + 0.5, targetPos.z);
            
            // 生成控制粒子效果
            if (level().isClientSide) {
                for (int i = 0; i < 3; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 0.4;
                    double offsetY = random.nextDouble() * 0.8;
                    double offsetZ = (random.nextDouble() - 0.5) * 0.4;
                    level().addParticle(ParticleTypes.CRIMSON_SPORE, 
                            entity.getX() + offsetX, entity.getY() + offsetY, entity.getZ() + offsetZ,
                            0.0D, 0.02D, 0.0D);
                }
            }
        }
    }

    // 将敌人拉向玩家
    private void pullTowardsPlayer(LivingEntity entity) {
        Player owner = getOwner() instanceof Player ? (Player) getOwner() : null;
        if (owner == null) return;
        
        Vec3 ownerPos = owner.position().add(0, 1.0, 0); // 玩家眼睛高度
        Vec3 entityPos = entity.position();
        
        // 计算向玩家的方向并应用轻微的拉力
        Vec3 direction = ownerPos.subtract(entityPos).normalize().scale(0.05);
        entity.setDeltaMovement(direction);
    }

    // 将敌人拉回封印位置
    private void pullBackToSeal(LivingEntity entity) {
        Vec3 sealPos = this.position().add(0, 0.5, 0);
        Vec3 entityPos = entity.position();
        
        // 计算向封印的方向并应用轻微的拉力
        Vec3 direction = sealPos.subtract(entityPos).normalize().scale(0.05);
        entity.setDeltaMovement(direction);
    }
    
    // 将敌人传送到玩家面前
    private void teleportEntitiesToPlayer() {
        Player owner = getOwner() instanceof Player ? (Player) getOwner() : null;
        if (owner == null) return;
        
        // 遍历所有被控制的实体
        for (Map.Entry<UUID, LivingEntity> entry : trappedEntities.entrySet()) {
            LivingEntity entity = entry.getValue();
            if (entity != null && entity.isAlive()) {
                // 计算玩家前方2格的位置
                Vec3 lookVector = owner.getLookAngle().normalize();
                Vec3 teleportPos = owner.position().add(
                    lookVector.x * 2, 
                    owner.getEyeHeight() - 1.0, 
                    lookVector.z * 2
                );
                
                // 确保位置在地面之上
                BlockPos teleportBlockPos = BlockPos.containing(teleportPos);
                while (!level().getBlockState(teleportBlockPos.below()).isAir() && teleportPos.y < level().getMaxBuildHeight()) {
                    teleportBlockPos = teleportBlockPos.above();
                    teleportPos = new Vec3(teleportBlockPos.getX() + 0.5, teleportBlockPos.getY(), teleportBlockPos.getZ() + 0.5);
                }
                
                // 传送实体
                entity.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                
                // 添加传送粒子效果
                if (level().isClientSide) {
                    for (int i = 0; i < 10; i++) {
                        double offsetX = (random.nextDouble() - 0.5) * 1.0;
                        double offsetY = random.nextDouble() * 1.0;
                        double offsetZ = (random.nextDouble() - 0.5) * 1.0;
                        level().addParticle(ParticleTypes.PORTAL, 
                                entity.getX() + offsetX, entity.getY() + offsetY, entity.getZ() + offsetZ,
                                0.0D, 0.1D, 0.0D);
                    }
                }
                
                // 重置控制效果
                releaseEntityControl(entity);
                entry.setValue(null);
            }
        }
        
        // 清除所有被控制的实体记录
        trappedEntities.clear();
        initialPositions.clear();
        pullProgress.clear();
    }
    
    // 检查实体是否被此封印实体控制
    public boolean isEntityTrapped(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        
        UUID entityUUID = entity.getUUID();
        return trappedEntities.containsKey(entityUUID);
    }
    
    // 获取拉扯进度映射的getter方法
    public Map<UUID, Double> getPullProgress() {
        return pullProgress;
    }

    private void spawnParticles() {
        double size = getSize();
        for (int i = 0; i < 8; i++) {
            double angle = (double) i * Math.PI * 2.0D / 8.0D;
            double x = this.getX() + Math.cos(angle) * size * 0.5D;
            double z = this.getZ() + Math.sin(angle) * size * 0.5D;
            level().addParticle(ParticleTypes.ENCHANT, x, this.getY() + 0.5D, z, 0.0D, 0.05D, 0.0D);
        }
        // Center particle
        level().addParticle(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected AABB makeBoundingBox() {
        double size = getSize() / 2.0D;
        return new AABB(
                this.getX() - size, this.getY(), this.getZ() - size,
                this.getX() + size, this.getY() + size, this.getZ() + size
        );
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true; // 无敌状态
    }

    public boolean isCollidable() {
        return false; // 无物理碰撞
    }

    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT; // 默认使用右臂
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        // 此实体不持有物品，所以不需要实现具体逻辑
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        // 此实体不持有物品，返回空物品堆
        return ItemStack.EMPTY;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        // 此实体不持有装备，返回空集合
        return Collections.emptyList();
    }

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

    // Handle left click (push back to center) - 将结界中的敌人打回结界中心
    public void handleLeftClick() {
        if (getOwner() != null) {
            // 遍历所有被控制的实体，将它们拉回结界中心
            for (Map.Entry<UUID, LivingEntity> entry : trappedEntities.entrySet()) {
                LivingEntity entity = entry.getValue();
                if (entity != null && entity.isAlive()) {
                    // 重置拉扯进度，使实体回到结界中心
                    pullProgress.put(entry.getKey(), 1.0);
                }
            }
            // 播放击退音效
            level().playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
    }

    // Handle right click (teleport to player) - 将结界中的敌人传送到玩家面前
    public void handleRightClick() {
        if (getOwner() != null) {
            // 调用新的传送方法
            teleportEntitiesToPlayer();
            
            // 播放传送音效
            level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
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
        return false; // 隐藏实体ID显示
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true; // Allow interaction
    }
}