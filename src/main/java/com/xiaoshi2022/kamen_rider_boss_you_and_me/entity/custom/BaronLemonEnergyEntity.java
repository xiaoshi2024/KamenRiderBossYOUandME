package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

import static net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT;

/**
 * 巴隆柠檬形态的柠檬能量实体
 * 生成在目标周围，定住范围内的敌人
 */
public class BaronLemonEnergyEntity extends LivingEntity implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int existenceTicks = 0;
    private static final int MAX_EXISTENCE_TICKS = 100; // 5秒存在时间
    private UUID ownerId = null; // 存储释放者的UUID
    private static final float LEMON_ENERGY_RANGE = 3.0f; // 柠檬能量实体范围（增加范围以覆盖原来多个实体的效果区域）

    public BaronLemonEnergyEntity(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvulnerable(true); // 设置为无敌
    }
    
    // 用于BaronLemonAbilityHandler的构造函数
    public BaronLemonEnergyEntity(Level level, double x, double y, double z, UUID ownerUUID) {
        super(ModEntityTypes.BARON_LEMON_ENERGY.get(), level);
        this.setPos(x, y, z);
        this.ownerId = ownerUUID;
        this.noPhysics = true;
        this.setInvulnerable(true);
    }
    
    // 设置持续时间的方法（供BaronLemonAbilityHandler调用）
    public void setDuration(int duration) {
        // 由于我们的实体已经有固定的MAX_EXISTENCE_TICKS
        // 这个方法可以留空或根据需要调整
    }

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D).build();
    }

    @Override
    public void tick() {
        super.tick();

        existenceTicks++;

        // 检测并影响范围内的敌人
        if (!this.level().isClientSide()) {
            // 创建一个范围检测盒
            for (Entity nearbyEntity : this.level().getEntities(this,
                    this.getBoundingBox().inflate(LEMON_ENERGY_RANGE),
                    entity -> entity instanceof LivingEntity && !(entity instanceof BaronLemonEnergyEntity))) {
                
                // 检查是否是生物实体
                if (nearbyEntity instanceof LivingEntity livingEntity) {
                    // 检查是否是玩家且是释放者本人
                boolean isOwnerPlayer = nearbyEntity instanceof Player && this.ownerId != null && this.ownerId.equals(((Player)nearbyEntity).getUUID());
                boolean isAllied = false;
                if (this.ownerId != null) {
                    Player ownerPlayer = this.level().getPlayerByUUID(this.ownerId);
                    isAllied = ownerPlayer != null && nearbyEntity.isAlliedTo(ownerPlayer);
                }
                    
                    // 如果不是释放者本人且不是盟友，则施加效果
                    if (!isOwnerPlayer && !isAllied) {
                        // 给敌人添加定身效果（高等级缓慢，使其完全无法移动）
                        livingEntity.addEffect(new MobEffectInstance(
                                MobEffects.MOVEMENT_SLOWDOWN,
                                100, // 5秒持续时间
                                255, // 最高等级（完全无法移动）
                                false, 
                                true // 显示粒子效果
                        ));
                        
                        // 添加跳跃抑制效果（大幅降低跳跃高度）
                        livingEntity.addEffect(new MobEffectInstance(
                                MobEffects.JUMP,
                                100, // 5秒持续时间
                                -3, // 负等级大幅降低跳跃高度，避免使用极端值导致暴毙
                                false,
                                true
                        ));
                        
                        // 添加挖掘减速效果（防止破坏方块）
                        livingEntity.addEffect(new MobEffectInstance(
                                MobEffects.DIG_SLOWDOWN,
                                100, // 5秒持续时间
                                255, // 最高等级
                                false,
                                true
                        ));
                        
                        // 添加柠檬能量的特殊粒子效果（可以在客户端处理）
                        spawnLemonParticles(livingEntity);
                    }
                }
            }
        }

        // 存在时间结束，移除实体
        if (existenceTicks >= MAX_EXISTENCE_TICKS && !this.level().isClientSide()) {
            this.discard();
        }
    }

    // 生成柠檬能量粒子效果
    private void spawnLemonParticles(Entity target) {
        if (this.level().isClientSide()) {
            for (int i = 0; i < 3; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = this.random.nextDouble() * 1.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                
                // 使用附魔暴击粒子效果作为柠檬能量粒子
                this.level().addParticle(
                        ENCHANTED_HIT,
                        target.getX() + offsetX,
                        target.getY() + offsetY,
                        target.getZ() + offsetZ,
                        0.0D, 0.1D, 0.0D
                    );
                }
            }
        }

    // 以下是必要的方法实现
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<BaronLemonEnergyEntity> event) {
        event.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isAffectedByPotions() {
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
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.existenceTicks = nbt.getInt("ExistenceTicks");
        // 读取所有者ID
        if (nbt.hasUUID("OwnerId")) {
            this.ownerId = nbt.getUUID("OwnerId");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("ExistenceTicks", this.existenceTicks);
        // 保存所有者ID
        if (this.ownerId != null) {
            nbt.putUUID("OwnerId", this.ownerId);
        }
    }

    @Override
    public void checkDespawn() {
        // 防止自然消失
    }
    
    @Override
    public boolean isNoGravity() {
        return true; // 禁用重力
    }
    
    @Override
    public boolean isPickable() {
        return false; // 不可被拾取/选择
    }
    
    @Override
    public boolean isCustomNameVisible() {
        return false; // 隐藏实体ID显示
    }
    
    // 设置实体的所有者（释放者）
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    
    // 获取实体的所有者（释放者）
    public UUID getOwnerId() {
        return this.ownerId;
    }
}