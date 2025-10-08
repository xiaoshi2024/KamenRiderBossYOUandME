package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

public class EntitySakuraHurricane extends Animal implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation LEFT = RawAnimation.begin().thenPlayAndHold("right");
    private static final RawAnimation RIGHT = RawAnimation.begin().thenPlayAndHold("left");
    private static final RawAnimation JUMP = RawAnimation.begin().thenPlay("jump"); // 添加跳跃动画
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private float speedBoostFactor = 1.0F;
    private int speedBoostTime = 0;
    private boolean isTurningLeft = false;
    private boolean isTurningRight = false;

    // 跳跃相关变量
    private boolean isJumping = false;
    private float jumpPower = 0.0F;
    private int jumpCooldown = 0;
    private boolean isInAir = false;

    private int jumpDelay = 0;

    // 动画相关变量
    public float animationSpeed;
    public float animationSpeedOld;
    public float animationPosition;

    // 摩托车特有变量
    private float wheelRotation;
    private float prevWheelRotation;
    private int fuel = 0;
    private static final int MAX_FUEL = 1200; // 60秒燃料

    public EntitySakuraHurricane(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0F);
        this.noCulling = true; // 防止渲染裁剪
    }

    public static AttributeSupplier createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D) // 摩托车更耐用
                .add(Attributes.MOVEMENT_SPEED, 0.5D) // 提高基础速度从0.35D到0.5D
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D) // 完全抗击退
                .add(Attributes.FOLLOW_RANGE, 0.0D) // 不需要跟随范围
                .add(Attributes.JUMP_STRENGTH, 1.0D) // 添加跳跃强度属性
                .build();
    }

    @Override
    protected void registerGoals() {
        // 完全移除AI目标，摩托车不需要AI
        this.goalSelector.addGoal(0, new FloatGoal(this)); // 只保留浮水
    }

    // 音效 - 使用更合适的音效
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isVehicle() ? SoundEvents.MINECART_INSIDE : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        if (this.isVehicle() && this.isMoving()) {
            this.playSound(SoundEvents.MINECART_RIDING, 0.5F, 1.0F);
        }
    }

    // 交互逻辑 - 摩托车专用
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            return InteractionResult.PASS;
        }

        // 空手骑乘
        if (itemstack.isEmpty()) {
            if (!this.isVehicle()) {
                player.startRiding(this);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDER_DRAGON_FLAP, SoundSource.NEUTRAL, 1.0F, 0.8F);
                return InteractionResult.SUCCESS;
            }
        }
        // 使用燃料物品
        else if (itemstack.getItem() == Items.COAL || itemstack.getItem() == Items.CHARCOAL) {
            if (this.fuel < MAX_FUEL) {
                this.fuel += 200; // 每个煤炭增加200ticks燃料
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.5F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    // 骑乘相关方法
    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (this.hasPassenger(passenger)) {
            // 摩托车专用骑手位置
            double offset = this.getPassengersRidingOffset() + passenger.getMyRidingOffset();
            Vec3 seatPosition = new Vec3(0.0D, offset, -0.2D) // 稍微靠后坐
                    .yRot(-this.getYRot() * ((float)Math.PI / 180F));

            callback.accept(passenger,
                    this.getX() + seatPosition.x,
                    this.getY() + seatPosition.y,
                    this.getZ() + seatPosition.z);

            // 同步骑手旋转
            passenger.setYRot(passenger.getYRot() + this.yRotO - this.getYRot());
            passenger.setYHeadRot(passenger.getYHeadRot() + this.yRotO - this.getYRot());
        }
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        this.setYRot(passenger.getYRot());
        this.yRotO = this.getYRot();
        this.yBodyRot = this.getYRot();
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.7D;
    }

    // 核心移动控制 - 摩托车专用
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            LivingEntity rider = this.getControllingPassenger();

            if (this.isVehicle() && rider != null) {
                // 完全同步骑手旋转
                this.setYRot(rider.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(rider.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;

                // 获取输入
                float strafe = rider.xxa * 0.5F;
                float forward = rider.zza;

                // 更新转向状态
                isTurningLeft = strafe < -0.1F;
                isTurningRight = strafe > 0.1F;

                // 检查燃料
                boolean hasFuel = this.fuel > 0;

                // 计算速度（有燃料时正常速度，无燃料时减速）
                float baseSpeed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
                float currentSpeed = baseSpeed * this.speedBoostFactor * (hasFuel ? 1.5F : 0.5F); // 有燃料时速度提高到1.5倍

                this.setSpeed(currentSpeed);

                // 消耗燃料 - 降低消耗率
                if (hasFuel && (forward != 0 || strafe != 0) && this.tickCount % 2 == 0) { // 每两刻消耗一次燃料
                    this.fuel--;
                }

                // 处理跳跃输入
                handleJumpInput(rider);

                // 应用移动
                super.travel(new Vec3(strafe, travelVector.y, forward));

                // 更新轮子旋转（动画用）
                this.prevWheelRotation = this.wheelRotation;
                if (this.isMoving() && hasFuel) {
                    this.wheelRotation += this.animationSpeed * 10.0F;
                }

                // 更新动画状态
                this.updateAnimationState();

                // 播放引擎音效
                if (!this.level().isClientSide && this.tickCount % 40 == 0 && this.isMoving() && hasFuel) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.2F, 2.0F);
                }

                return;
            }
        }

        // 非骑乘状态 - 摩托车不会自己移动
        if (this.isVehicle()) {
            // 还有骑手但没有控制（可能是客户端不同步）
            super.travel(Vec3.ZERO);
        } else {
            // 完全静止
            this.setDeltaMovement(Vec3.ZERO);
        }

        this.updateAnimationState();
    }

    // 处理跳跃输入的核心方法
    private void handleJumpInput(LivingEntity rider) {
        if (this.jumpCooldown > 0) {
            this.jumpCooldown--;
        }

        if (this.jumpDelay > 0) {
            this.jumpDelay--;
        }

        // 检测玩家跳跃输入 - 修复版本
        if (rider instanceof Player player && this.jumpCooldown == 0 && this.jumpDelay == 0) {
            // 使用反射来访问受保护的 jumping 字段
            boolean wantsToJump = false;
            try {
                Field jumpingField = LivingEntity.class.getDeclaredField("jumping");
                jumpingField.setAccessible(true);
                wantsToJump = jumpingField.getBoolean(player);
            } catch (Exception e) {
                // 反射失败时使用更安全的备用方案：只检查垂直速度，不检查是否在地面
                // 避免玩家刚骑上摩托车时位置变化导致的误触
                wantsToJump = player.getDeltaMovement().y > 0.5; // 使用更高的阈值
            }

            if (wantsToJump && this.onGround()) {
                if (!this.isJumping) {
                    // 开始蓄力
                    this.isJumping = true;
                    this.jumpPower = 0.0F;
                    // 播放蓄力音效
                    this.playSound(SoundEvents.NOTE_BLOCK_HAT.get(), 0.3F, 1.5F);
                } else {
                    // 蓄力过程
                    if (this.jumpPower < 1.0F) {
                        this.jumpPower += 0.1F;
                    }

                    // 自动跳跃当蓄力完成
                    if (this.jumpPower >= 1.0F) {
                        this.executeJump();
                        this.isJumping = false;
                        this.jumpCooldown = 10;
                        this.jumpDelay = 5;
                    }
                }
            } else if (this.isJumping) {
                // 执行跳跃
                this.executeJump();
                this.isJumping = false;
                this.jumpCooldown = 10;
                this.jumpDelay = 5;
            }
        }

        // 空中姿态控制
        if (this.isInAir && rider instanceof Player player) {
            controlAirborneAttitude(player);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 当摩托车被攻击时的逻辑
        if (!this.level().isClientSide && !this.isInvulnerableTo(source)) {
            // 检查是否有骑手
            Entity rider = this.getControllingPassenger();
            
            // 只有在没有骑手的情况下，才会变回锁种
            if (rider == null) {
                // 生成锁种物品
                ItemStack lockseedStack = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.SAKURAHURRICANE_LOCKSEED.get());
                
                // 创建物品实体并设置位置
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                        this.level(), this.getX(), this.getY(), this.getZ(), lockseedStack);
                
                // 添加物品实体到世界
                this.level().addFreshEntity(itemEntity);
                
                // 播放锁种关闭音效
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds.LOCKOFF.get(), 
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                
                // 移除摩托车实体
                this.remove(Entity.RemovalReason.DISCARDED);
                
                // 返回true表示伤害已处理
                return true;
            }
            // 有骑手时，正常处理伤害
            else {
                return super.hurt(source, amount);
            }
        }
        
        // 默认行为
        return super.hurt(source, amount);
    }

    // 执行跳跃
    private void executeJump() {
        if (!this.onGround()) {
            return; // 只有在地面上才能起跳
        }

        // 计算跳跃速度（参考Minecraft马的跳跃机制）
        float jumpStrength = (float) this.getAttributeValue(Attributes.JUMP_STRENGTH);
        double jumpVelocity = (double)(this.jumpPower * jumpStrength * 0.6F); // 调整跳跃系数

        // 获取当前移动方向来增加跳跃距离
        Vec3 currentMotion = this.getDeltaMovement();
        double horizontalBoost = this.jumpPower * 0.3; // 水平跳跃助推

        // 应用跳跃速度（保持水平动量）
        this.setDeltaMovement(
                currentMotion.x + (this.getLookAngle().x * horizontalBoost),
                jumpVelocity,
                currentMotion.z + (this.getLookAngle().z * horizontalBoost)
        );

        // 设置空中状态
        this.isInAir = true;

        // 播放跳跃音效
        this.playSound(SoundEvents.HORSE_JUMP, 0.4F, 1.0F);

        // 消耗少量燃料进行跳跃
        if (this.fuel > 0) {
            this.fuel -= 10; // 跳跃消耗10点燃料
        }

        // 重置跳跃力量
        this.jumpPower = 0.0F;
    }


    // 空中姿态控制 - 模拟摩托车特技:cite[7]
    private void controlAirborneAttitude(Player player) {
        // 获取玩家输入来控制空中姿态
        float pitchInput = -player.xxa * 0.5F; // 左右倾斜控制前后旋转

        // 应用空中旋转
        if (Math.abs(pitchInput) > 0.1F) {
            this.setXRot(this.getXRot() + pitchInput * 2.0F);
        }

        // 限制旋转角度
        if (this.getXRot() < -45.0F) this.setXRot(-45.0F);
        if (this.getXRot() > 45.0F) this.setXRot(45.0F);
    }

    private void updateAnimationState() {
        this.animationSpeedOld = this.animationSpeed;
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        float movement = (float) Math.sqrt(dx * dx + dz * dz) * 4.0F;

        if (movement > 1.0F) {
            movement = 1.0F;
        }

        this.animationSpeed += (movement - this.animationSpeed) * 0.4F;
        this.animationPosition += this.animationSpeed;

        // 检测是否落地 - 修复地面检测方法
        if (this.isInAir && this.onGround() && !this.level().isClientSide) {
            this.isInAir = false;
            // 重置跳跃相关状态
            this.isJumping = false;
            this.jumpPower = 0.0F;

            // 播放落地音效
            this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.5F, 1.0F);
        }
    }

    @Override
    public boolean onGround() {
        return super.onGround(); // 使用父类的 onGround() 方法
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // 处理加速效果
        if (this.speedBoostTime > 0) {
            --this.speedBoostTime;
            if (this.speedBoostTime == 0) {
                this.speedBoostFactor = 1.0F;
            }
        }

        // 客户端轮子旋转更新
        if (this.level().isClientSide) {
            this.prevWheelRotation = this.wheelRotation;
            if (this.isMoving() && this.fuel > 0) {
                this.wheelRotation += this.animationSpeed * 10.0F;
            }
        }

        // 在空中时应用轻微的向前动力（模拟摩托车跳跃时的惯性）
        if (this.isInAir && this.isVehicle()) {
            Vec3 currentMotion = this.getDeltaMovement();
            Vec3 lookVector = this.getLookAngle().multiply(0.1, 0, 0.1);
            this.setDeltaMovement(
                    currentMotion.x + lookVector.x,
                    currentMotion.y,
                    currentMotion.z + lookVector.z
            );
        }
        
        // 限制水平速度，防止移动错误警告
        Vec3 motion = this.getDeltaMovement();
        double horizontalSpeed = motion.horizontalDistance();
        double maxHorizontalSpeed = 0.9; // 设置一个合理的最大水平速度，高于创飞所需的阈值但低于移动错误的阈值
        
        if (horizontalSpeed > maxHorizontalSpeed) {
            // 保持方向，按比例降低速度
            double speedRatio = maxHorizontalSpeed / horizontalSpeed;
            this.setDeltaMovement(
                    motion.x * speedRatio,
                    motion.y,
                    motion.z * speedRatio
            );
        }
    }

    // 动画控制器
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> event) {
        // 骑乘状态下的动画
        if (this.isVehicle()) {
            if (this.isInAir) {
                event.getController().setAnimation(JUMP);
                return PlayState.CONTINUE;
            } else if (isTurningLeft) {
                event.getController().setAnimation(LEFT);
                return PlayState.CONTINUE;
            } else if (isTurningRight) {
                event.getController().setAnimation(RIGHT);
                return PlayState.CONTINUE;
            } else if (this.isMoving()) {
                event.getController().setAnimation(RUN);
                return PlayState.CONTINUE;
            }
        }

        event.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    private boolean isMoving() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 0.001;
    }

    // 加速方法
    public void setSpeedBoost(float factor, int duration) {
        this.speedBoostFactor = factor;
        this.speedBoostTime = duration;
    }

    // 燃料相关方法
    public int getFuel() {
        return this.fuel;
    }

    public int getMaxFuel() {
        return MAX_FUEL;
    }

    public float getFuelLevel() {
        return (float) this.fuel / MAX_FUEL;
    }

    // 跳跃相关方法
    public float getJumpPower() {
        return this.jumpPower;
    }

    public boolean isChargingJump() {
        return this.isJumping;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null; // 不可繁殖
    }

    // 物理和碰撞设置 - 摩托车特性
    @Override
    public boolean isPushable() {
        return !this.isVehicle(); // 骑乘时不可推动
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        // 允许与所有实体碰撞（包括生物和玩家）
        return true;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.95F; // 水中减速较少
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    // 当被骑乘时的tick更新
    @Override
    public void rideTick() {
        super.rideTick();
        
        // 获取骑乘者
        Entity rider = this.getFirstPassenger();
        if (rider instanceof Player player) {
            // 检查玩家是否已死亡或不在实体上
            if (!player.isAlive()) {
                // 玩家被打下或死亡，在服务器端处理
                if (!this.level().isClientSide) {
                    // 让玩家停止骑乘
                    player.stopRiding();
                    
                    // 播放锁种关闭音效
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds.LOCKOFF.get(), 
                            SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 移除摩托车实体
                    this.remove(Entity.RemovalReason.DISCARDED);
                    
                    // 原地生成锁种物品
                    this.level().addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                            this.level(), 
                            this.getX(), 
                            this.getY(), 
                            this.getZ(), 
                            new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.SAKURAHURRICANE_LOCKSEED.get())));
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 确保摩托车在非骑乘时不会移动
        if (!this.isVehicle() && !this.level().isClientSide) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.5, 0.5));
        }
        
        // 检查是否达到了可以创飞敌人的速度
        checkForEnemyCollision();
    }
    
    /**
     * 检查摩托车是否达到足够速度来创飞敌人
     */
    private void checkForEnemyCollision() {
        // 只在服务端执行碰撞检测，避免客户端不同步
        if (this.level().isClientSide) {
            return;
        }
        
        // 检查是否有骑手并且正在移动
        if (!this.isVehicle()) {
            return;
        }
        
        // 获取当前水平速度
        double horizontalSpeed = this.getDeltaMovement().horizontalDistance();
        
        // 设置创飞敌人的最小速度阈值（降低阈值以确保能够创飞）
        double minRammingSpeed = 0.5; // 从0.8降低到0.5
        
        // 只有当速度超过阈值时才执行碰撞检测
        if (horizontalSpeed < minRammingSpeed) {
            return;
        }
        
        // 获取摩托车前方的一个区域进行检测（增大范围）
        double range = 2.0; // 从1.5增加到2.0
        List<Entity> nearbyEntities = this.level().getEntities(this, this.getBoundingBox().inflate(range), 
                entity -> {
                    // 不检测自己和骑手
                    if (entity == this || entity == this.getControllingPassenger()) {
                        return false;
                    }
                    
                    // 只检测可以被伤害的实体
                    return entity instanceof LivingEntity;
                });
        
        // 对检测到的敌人应用伤害和击退效果
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                // 根据速度计算伤害（速度越快伤害越高）
                float damage = (float) (horizontalSpeed * 5.0);
                
                // 创建伤害源
                DamageSource damageSource = this.damageSources().mobAttack(this);
                
                // 应用伤害
                boolean damaged = livingEntity.hurt(damageSource, damage);
                
                if (damaged) {
                    // 根据摩托车的移动方向计算击退方向
                    Vec3 direction = this.getLookAngle().normalize();
                    double knockbackStrength = horizontalSpeed * 2.0;
                    
                    // 应用击退效果
                    livingEntity.push(direction.x * knockbackStrength, 0.5, direction.z * knockbackStrength);
                    
                    // 播放撞击音效
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.IRON_GOLEM_ATTACK, SoundSource.NEUTRAL, 0.6F, 1.0F);
                }
            }
        }
    }

    // 轮子旋转获取方法（用于渲染）
    public float getWheelRotation(float partialTicks) {
        return this.prevWheelRotation + (this.wheelRotation - this.prevWheelRotation) * partialTicks;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true; // 忽略压力板等
    }
}