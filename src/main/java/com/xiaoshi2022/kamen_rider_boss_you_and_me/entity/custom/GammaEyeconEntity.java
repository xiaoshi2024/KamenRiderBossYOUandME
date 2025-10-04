package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.GammaEyecon;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;

public class GammaEyeconEntity extends AmbientCreature implements GeoEntity {
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    // 控制变量
    private boolean isFloating = true;
    private boolean isFlying = false; // 添加飞行状态标志
    private int floatTime = 0;
    private static final int FLOAT_DURATION = 100; // 漂浮持续时间
    private int moveCooldown = 0;
    private static final int MOVE_COOLDOWN_MIN = 20; // 最小移动冷却时间（刻）
    private static final int MOVE_COOLDOWN_MAX = 60; // 最大移动冷却时间（刻）
    
    // 几率配置
    private static final int SUMMON_GAMMA_CHANCE = 30; // 30% 几率召唤眼魔
    private static final int BECOME_ITEM_CHANCE = 70; // 70% 几率变成物品
    
    public GammaEyeconEntity(EntityType<? extends AmbientCreature> type, Level world) {
        super(type, world);
        this.noPhysics = false; // 允许物理碰撞
        this.setInvulnerable(false); // 不是无敌的
        this.isFlying = true; // 初始状态为飞行
        this.setNoGravity(true); // 初始取消重力
    }
    
    // 获取飞行状态
    public boolean isFlying() {
        return isFlying;
    }
    
    // 设置飞行状态
    public void setFlying(boolean flying) {
        this.isFlying = flying;
        this.setNoGravity(flying); // 飞行时取消重力
        
        if (!flying) {
            this.setNoGravity(false);
            // 清除垂直速度
            this.setDeltaMovement(this.getDeltaMovement().x * 0.8, this.getDeltaMovement().y * 0.5, this.getDeltaMovement().z * 0.8);
        } else {
            // 飞行时给予一点初始上升动力
            this.setDeltaMovement(this.getDeltaMovement().x, 0.2, this.getDeltaMovement().z);
        }
    }
    


    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D) // 较低的血量，容易被攻击
                .add(Attributes.MOVEMENT_SPEED, 0.1D) // 缓慢移动
                .add(Attributes.FLYING_SPEED, 0.2D) // 缓慢飞行
                .add(Attributes.FOLLOW_RANGE, 32.0D) // 添加跟踪范围属性
                .build();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GammaEyeconEntity> event) {
        // 播放飞行动画
        event.getController().setAnimation(FLY);
        return PlayState.CONTINUE;
    }

    // 注册AI目标
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CustomRandomFlyingGoal(this));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }
    
    // 重写导航系统，使用飞行导航代替地面导航
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(false);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }
    
    // 自定义随机飞行目标类
    class CustomRandomFlyingGoal extends Goal {
        private final GammaEyeconEntity entity;
        private final Random random = new Random();
        private double targetX, targetY, targetZ;
        private int executionChance = 20;
        private int cooldown = 0;
        
        public CustomRandomFlyingGoal(GammaEyeconEntity entity) {
            this.entity = entity;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }
        
        @Override
        public boolean canUse() {
            // 只有在冷却时间结束且实体处于飞行状态时才执行
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            
            // 随机决定是否执行此目标
            if (random.nextInt(executionChance) != 0) {
                return false;
            }
            
            // 计算新的随机目标位置
            double speed = entity.getAttributeValue(Attributes.FLYING_SPEED);
            targetX = entity.getX() + (random.nextDouble() - 0.5) * 10.0;
            targetY = entity.getY() + (random.nextDouble() - 0.5) * 5.0;
            targetZ = entity.getZ() + (random.nextDouble() - 0.5) * 10.0;
            
            // 设置冷却时间
            cooldown = random.nextInt(40) + 20; // 20-60刻的冷却时间
            
            return true;
        }
        
        @Override
        public boolean canContinueToUse() {
            // 持续向目标移动
            return entity.distanceToSqr(targetX, targetY, targetZ) > 1.0;
        }
        
        @Override
        public void start() {
            // 确保实体处于飞行状态
            if (!entity.isFlying()) {
                entity.setFlying(true);
            }
        }
        
        @Override
        public void tick() {
            // 计算到目标的方向
            double dx = targetX - entity.getX();
            double dy = targetY - entity.getY();
            double dz = targetZ - entity.getZ();
            
            // 归一化方向向量
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 0) {
                dx /= distance;
                dy /= distance;
                dz /= distance;
                
                // 应用移动
                double speed = entity.getAttributeValue(Attributes.FLYING_SPEED) * 0.5;
                entity.setDeltaMovement(
                    entity.getDeltaMovement().x + dx * speed,
                    entity.getDeltaMovement().y + dy * speed,
                    entity.getDeltaMovement().z + dz * speed
                );
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        // 确保实体一直处于飞行状态
        if (!this.isFlying()) {
            this.setFlying(true);
        }
        
        // 自由漫步逻辑 - 增强原版AI的飞行效果
        if (this.floatTime % 10 == 0) { // 更频繁的小幅度移动
            Random random = new Random();
            double speed = this.getAttributeValue(Attributes.FLYING_SPEED);
            
            // 小幅度的随机调整以增加自然感
            double xAdjustment = (random.nextDouble() - 0.5) * speed * 0.2;
            double yAdjustment = (random.nextDouble() - 0.5) * speed * 0.4;
            double zAdjustment = (random.nextDouble() - 0.5) * speed * 0.2;
            
            // 应用调整但保持总体方向
            this.setDeltaMovement(
                    this.getDeltaMovement().x + xAdjustment,
                    this.getDeltaMovement().y + yAdjustment,
                    this.getDeltaMovement().z + zAdjustment
            );
        }
        
        // 限制最大速度，使移动更自然
        double currentSpeed = Math.sqrt(
                this.getDeltaMovement().x * this.getDeltaMovement().x +
                this.getDeltaMovement().y * this.getDeltaMovement().y +
                this.getDeltaMovement().z * this.getDeltaMovement().z
        );
        double maxSpeed = this.getAttributeValue(Attributes.FLYING_SPEED) * 1.5;
        
        if (currentSpeed > maxSpeed) {
            double scale = maxSpeed / currentSpeed;
            this.setDeltaMovement(
                    this.getDeltaMovement().x * scale,
                    this.getDeltaMovement().y * scale,
                    this.getDeltaMovement().z * scale
            );
        }
        
        // 更新漂浮时间
        this.floatTime++;
        if (this.floatTime >= FLOAT_DURATION) {
            this.floatTime = 0;
        }
        
        // 应用较小的阻力，让飞行更流畅
        this.setDeltaMovement(
                this.getDeltaMovement().x * 0.98D,
                this.getDeltaMovement().y * 0.98D,
                this.getDeltaMovement().z * 0.98D
        );
    }

    // 实体受到攻击时的处理逻辑
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 只在服务端处理
        if (!this.level().isClientSide && source.getEntity() instanceof Player player) {
            // 随机决定结果
            Random random = new Random();
            int chance = random.nextInt(100);
            
            if (chance < SUMMON_GAMMA_CHANCE) {
                // 召唤眼魔
                summonGamma(player);
            } else if (chance < BECOME_ITEM_CHANCE) {
                // 变成物品到玩家手中
                becomeItem(player);
            }
            
            // 无论哪种情况，实体都会消失
            this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
            this.discard();
            return true;
        }
        return false;
    }
    
    // 召唤眼魔
    private void summonGamma(Player player) {
        if (this.level() instanceof ServerLevel serverLevel) {
            // 检查是否有Gamma实体类型
            if (ModEntityTypes.GAMMA_S.get() != null) {
                // 创建眼魔实体
                Gamma_s_Entity gamma = ModEntityTypes.GAMMA_S.get().create(serverLevel);
                if (gamma != null) {
                    // 设置眼魔的位置在玩家附近
                    double spawnX = player.getX() + (player.getRandom().nextDouble() - 0.5) * 4;
                    double spawnY = player.getY() + 1;
                    double spawnZ = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 4;
                    
                    gamma.setPos(spawnX, spawnY, spawnZ);
                    // 不设置目标，让眼魔不会主动攻击玩家
                    
                    // 添加实体到世界
                    serverLevel.addFreshEntity(gamma);
                    
                    // 播放召唤音效
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EVOKER_PREPARE_SUMMON, player.getSoundSource(), 1.0F, 1.0F);
                }
            }
        }
    }
    
    // 变成物品到玩家手中
    private void becomeItem(Player player) {
        // 创建眼魔眼魂物品 - 使用ModItems中注册的实例
        ItemStack eyeconStack = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.GAMMA_EYECON.get(), 1);
        
        // 尝试添加到玩家物品栏
        if (!player.getInventory().add(eyeconStack)) {
            // 如果物品栏满了，就掉落在地上
            player.drop(eyeconStack, false);
        }
        
        // 播放获取物品音效
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_PICKUP, player.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(Entity entity) {
        super.doPush(entity);
    }

    @Override
    protected void pushEntities() {
        super.pushEntities();
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false; // 不会受到坠落伤害
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
        // 不需要实现
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT; // 默认使用右手
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    // 保存实体数据
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        // 保存飞行状态
        compound.putBoolean("IsFlying", this.isFlying);

        compound.putBoolean("IsFloating", this.isFloating);
        compound.putInt("FloatTime", this.floatTime);
        compound.putInt("MoveCooldown", this.moveCooldown);
    }

    // 加载实体数据
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        // 读取飞行状态
        if (compound.contains("IsFlying")) {
            this.isFlying = compound.getBoolean("IsFlying");
            this.setNoGravity(this.isFlying);
        }

        if (compound.contains("IsFloating")) {
            this.isFloating = compound.getBoolean("IsFloating");
        }
        if (compound.contains("FloatTime")) {
            this.floatTime = compound.getInt("FloatTime");
        }
        if (compound.contains("MoveCooldown")) {
            this.moveCooldown = compound.getInt("MoveCooldown");
        }
    }
    
    // 重写此方法以隐藏实体ID显示
    @Override
    public boolean shouldShowName() {
        return false;
    }
    
    // 确保即使有自定义名称也不显示
    @Override
    public boolean isCustomNameVisible() {
        return false;
    }
}