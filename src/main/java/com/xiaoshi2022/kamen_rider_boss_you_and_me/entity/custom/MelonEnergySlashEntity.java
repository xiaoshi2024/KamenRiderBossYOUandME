package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ParticleTypesRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
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

/**
 * 蜜瓜能量剑气实体类
 * 实现橙色剑气的飞行、命中检测和伤害效果
 */
public class MelonEnergySlashEntity extends AbstractArrow implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_CRITICAL_PROJECTILE = SynchedEntityData.defineId(MelonEnergySlashEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUUID;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 100; // 剑气飞行最大时间（5秒）

    private double damage;

    public MelonEnergySlashEntity(EntityType<? extends MelonEnergySlashEntity> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED; // 剑气不可拾取
    }

    public MelonEnergySlashEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.MELON_ENERGY_SLASH.get(), shooter, level);
        this.ownerUUID = shooter.getUUID();
        this.pickup = Pickup.DISALLOWED;
    }

    public MelonEnergySlashEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.MELON_ENERGY_SLASH.get(), x, y, z, level);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CRITICAL_PROJECTILE, false);
    }

    @Override
    public void tick() {
        super.tick();
        
        // 增加生命周期计数
        this.lifeTicks++;
        
        // 如果剑气飞行时间过长，就自动消失
        if (this.lifeTicks > MAX_LIFE_TICKS) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        
        // 在剑气飞行路径上生成蜜瓜粒子效果
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // 生成蜜瓜粒子
            spawnMelonParticles(serverLevel, this.position(), this.getDeltaMovement());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        
        Entity entity = result.getEntity();
        
        // 检查是否为生物实体
        if (entity instanceof LivingEntity livingEntity) {
            // 应用伤害效果
            applySlashDamage(livingEntity);
            
            // 发射蜜瓜爆炸音效
            playMelonExplosionSound();
            
            // 生成蜜瓜爆炸效果
            spawnMelonExplosionEffect();
        }
        
        // 剑气命中后消失
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // 击中方块后生成爆炸效果
        playMelonExplosionSound();
        spawnMelonExplosionEffect();
        this.remove(RemovalReason.DISCARDED);
    }


    @Override
    protected ItemStack getPickupItem() {
        // 剑气不可拾取，返回空物品堆
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    /* ====== 自定义方法 ====== */
    
    /**
     * 应用剑气伤害
     */
    private void applySlashDamage(LivingEntity target) {
        // 直接对目标造成伤害，模拟剑气切割效果
        // 获取基础伤害
        double baseDamage = this.getBaseDamage();
        
        // 创建伤害源
        DamageSource damageSource = this.damageSources().thrown(this, this.getOwner());
        
        // 对目标造成伤害
        target.hurt(damageSource, (float) baseDamage);
        
        // 模拟被击中的击退效果
        target.knockback(1.0F, this.getX() - target.getX(), this.getZ() - target.getZ());
    }

    /**
     * 播放蜜瓜爆炸音效
     */
    private void playMelonExplosionSound() {
        // 播放爆炸音效
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.1f, false, Level.ExplosionInteraction.NONE);
    }

    /**
     * 生成蜜瓜爆炸效果
     */
    private void spawnMelonExplosionEffect() {
        // 生成大量蜜瓜粒子
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
                
                serverLevel.sendParticles(
                        ParticleTypesRegistry.MLONSLICE.get(),
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.1D
                );
            }
        }
    }

    /**
     * 在剑气飞行路径上生成蜜瓜粒子
     */
    private void spawnMelonParticles(ServerLevel serverLevel, Vec3 position, Vec3 motion) {
        // 在剑气当前位置生成粒子
        serverLevel.sendParticles(
                ParticleTypesRegistry.MLONSLICE.get(),
                position.x(),
                position.y(),
                position.z(),
                3, // 每次生成3个粒子
                motion.x() * 0.2,
                motion.y() * 0.2,
                motion.z() * 0.2,
                0.05D
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 添加动画控制器，用于播放剑气的动画效果
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    
    /**
     * 动画状态谓词，用于控制动画播放
     */
    private PlayState predicate(AnimationState<MelonEnergySlashEntity> event) {
        // 播放空闲动画
        event.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }
        this.lifeTicks = compound.getInt("LifeTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
        compound.putInt("LifeTicks", this.lifeTicks);
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }
    
    /**
     * 获取剑气的基础伤害值
     */
    @Override
    public double getBaseDamage() {
        return this.damage > 0 ? this.damage : 10.0; // 默认伤害值为10
    }
    
    @Override
    public boolean isCustomNameVisible() {
        return false; // 隐藏实体ID显示
    }
    
    // 确保实体ID不显示
    @Override
    public boolean shouldShowName() {
        return false;
    }

}