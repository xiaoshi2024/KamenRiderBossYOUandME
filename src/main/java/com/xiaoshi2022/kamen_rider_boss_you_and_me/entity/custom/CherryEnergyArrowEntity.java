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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * 樱桃能量箭矢实体类
 * 实现樱桃形能量箭矢的飞行、命中检测和束缚压碎效果
 */
public class CherryEnergyArrowEntity extends AbstractArrow implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_CRITICAL_PROJECTILE = SynchedEntityData.defineId(CherryEnergyArrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUUID;
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 200; // 箭矢飞行最大时间（10秒）

    private double damage;

    public CherryEnergyArrowEntity(EntityType<? extends CherryEnergyArrowEntity> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED; // 箭矢不可拾取
    }

    public CherryEnergyArrowEntity(Level level, LivingEntity shooter) {
        super(ModEntityTypes.CHERRY_ENERGY_ARROW.get(), shooter, level);
        this.ownerUUID = shooter.getUUID();
        this.pickup = Pickup.DISALLOWED;
    }

    public CherryEnergyArrowEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.CHERRY_ENERGY_ARROW.get(), x, y, z, level);
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
        
        // 如果箭矢飞行时间过长，就自动消失
        if (this.lifeTicks > MAX_LIFE_TICKS) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        
        // 在箭矢飞行路径上生成樱桃粒子效果
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // 生成樱桃粒子
            spawnCherryParticles(serverLevel, this.position(), this.getDeltaMovement());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        
        Entity entity = result.getEntity();
        
        // 检查是否为生物实体
        if (entity instanceof LivingEntity livingEntity) {
            // 应用束缚效果（缓慢、挖掘疲劳、反胃）
            applyBindingEffects(livingEntity);
            
            // 发射樱桃爆炸音效
            playCherryExplosionSound();
            
            // 创建樱桃爆炸效果实体（如果有的话）
            spawnCherryExplosionEffect();
            
            // 模拟压碎效果 - 对目标造成额外伤害
            applyCrushingDamage(livingEntity);
        }
        
        // 箭矢命中后消失
        this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.remove(RemovalReason.DISCARDED);
    }


    @Override
    protected ItemStack getPickupItem() {
        // 箭矢不可拾取，返回空物品堆
        return ItemStack.EMPTY;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    /* ====== 自定义方法 ====== */
    
    /**
     * 应用束缚效果
     */
    private void applyBindingEffects(LivingEntity target) {
        // 缓慢IV效果，使目标几乎无法移动
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 
                60, // 持续3秒
                3,  // 等级IV
                false, 
                false
        ));
        
        // 挖掘疲劳IV效果，使目标无法挖掘或攻击
        target.addEffect(new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN, 
                60, // 持续3秒
                3,  // 等级IV
                false, 
                false
        ));
        
        // 反胃效果，模拟被压碎的眩晕感
        target.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION, 
                60, // 持续3秒
                0,  // 等级I
                false, 
                false
        ));
    }

    /**
     * 播放樱桃爆炸音效
     */
    private void playCherryExplosionSound() {
        // 这里使用已有的音效或添加新的音效
        // 可以考虑使用ModSounds中的音效，如SONICARROW_SHOOT或其他爆炸音效
        // 由于没有专门的樱桃爆炸音效，可以暂时使用默认的爆炸音效
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 0.1f, false, Level.ExplosionInteraction.NONE);
    }

    /**
     * 生成樱桃爆炸效果实体
     */
    private void spawnCherryExplosionEffect() {
        // 如果有专门的樱桃爆炸效果实体，可以在这里生成
        // 例如：
        // CherryExplosionEffect explosion = new CherryExplosionEffect(this.level(), this.getX(), this.getY(), this.getZ());
        // this.level().addFreshEntity(explosion);
        
        // 生成大量樱桃粒子作为替代
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
                
                serverLevel.sendParticles(
                        ParticleTypesRegistry.CHERRYSLICE.get(),
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
     * 应用压碎伤害
     */
    private void applyCrushingDamage(LivingEntity target) {
        // 直接对目标造成伤害，模拟压碎效果
        // 获取基础伤害
        double baseDamage = this.getBaseDamage();
        
        // 额外的压碎伤害
        double crushDamage = baseDamage * 0.5; // 50% 额外伤害
        
        // 创建伤害源
        DamageSource damageSource = this.damageSources().thrown(this, this.getOwner());
        
        // 对目标造成伤害
        target.hurt(damageSource, (float) (baseDamage + crushDamage));
        
        // 模拟被压碎的击退效果
        target.knockback(1.5F, this.getX() - target.getX(), this.getZ() - target.getZ());
    }

    /**
     * 在箭矢飞行路径上生成樱桃粒子
     */
    private void spawnCherryParticles(ServerLevel serverLevel, Vec3 position, Vec3 motion) {
        // 在箭矢当前位置生成粒子
        serverLevel.sendParticles(
                ParticleTypesRegistry.CHERRYSLICE.get(),
                position.x(),
                position.y(),
                position.z(),
                1,
                motion.x() * 0.2,
                motion.y() * 0.2,
                motion.z() * 0.2,
                0.05D
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 添加动画控制器，用于播放樱桃箭矢的动画效果
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    
    /**
     * 动画状态谓词，用于控制动画播放
     */
    private PlayState predicate(AnimationState<CherryEnergyArrowEntity> event) {
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