package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects.ModEffects;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.PlayerBloodlineHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.Locale;

public class KivatBatTwoNd extends TamableAnimal implements GeoEntity {

    /* ===== 动画 ===== */
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation SLEEP = RawAnimation.begin().thenPlay("sleep");
    private static final RawAnimation SAY = RawAnimation.begin().thenLoop("say");
    private static final RawAnimation HOVER = RawAnimation.begin().thenPlayAndHold("hover");

    /* ===== 数据 Key ===== */
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RESTING = SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.BOOLEAN);

    /* ===== 飞行常量 ===== */
    public static final int TICKS_PER_FLAP = Mth.ceil(2.4166098F);
    private static final double FOLLOW_SPEED = 1.2D;
    private static final double ATTACK_SPEED = 1.2D;
    private static final double MIN_FOLLOW = 3F;
    private static final double MAX_FOLLOW = 10F;
    private static final EntityDataAccessor<Boolean> DATA_CAN_HANG =
            SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.BOOLEAN);

    private BlockPos targetPosition;

    private static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.INT);

    /**
     * 彩蛋：小丑蝙蝠呼叫
     */
    public static boolean isClownBatCall(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String processedText = text.toLowerCase(Locale.ROOT)
                .replaceAll("[@，,\\s]", "")   // 去掉 @ 中英文逗号空格
                .trim();

        // 更精确的匹配
        return processedText.contains("那边的小丑蝙蝠过来") ||
                processedText.contains("小丑蝙蝠过来") ||
                processedText.contains("过来小丑蝙蝠");
    }

    /**
     * 彩蛋：强制飞向玩家
     */
    public void temptToPlayer(Player player) {
        // 多重安全检查
        if (level().isClientSide || player == null) return;
        if (!this.isAlive() || this.isRemoved()) return;
        if (!player.isAlive() || player.isRemoved()) return;
        if (level() != player.level()) return; // 确保在同一个世界

        // 确保导航系统有效
        if (this.navigation == null || !(this.navigation instanceof FlyingPathNavigation)) {
            this.navigation = createNavigation(this.level());
        }

        this.navigation.stop();

        try {
            this.navigation.moveTo(player, 1.6D);
        } catch (Exception e) {
            // 捕获可能的异常，防止崩溃
            kamen_rider_boss_you_and_me.LOGGER.error("Kivat导航错误: " + e.getMessage());
        }

        this.setOrderedToSit(false);
        this.setResting(false);
        this.setSleeping(false);
        this.setFlying(true);
    }

    public static Component reply(String input) {
        input = input.toLowerCase(Locale.ROOT);

        Component.translatable("dialog.kivat.clownbat");

        if (input.contains("hello") || input.contains("你好") || input.contains("hi")) {
            return Component.translatable("dialog.kivat.hello");
        }
        if (input.contains("food") || input.contains("饿") || input.contains("meal")) {
            return Component.translatable("dialog.kivat.food");
        }
        if (input.contains("sleep") || input.contains("睡觉") || input.contains("rest")) {
            return Component.translatable("dialog.kivat.sleep");
        }
        if (input.matches(".*(?:血|blood|fang).*")) {
            return Component.translatable("dialog.kivat.blood");
        }
        return Component.translatable("dialog.kivat.unknown");
    }

    public enum Mode {NORMAL, SAY, SLEEP}

    /* ===== 运行时字段 ===== */
    private int flapCooldown = 0;                // 翅膀拍打计时

    /* ------------------------------------------------------------ */
    /*  构造 & 属性                                                  */
    /* ------------------------------------------------------------ */
    public KivatBatTwoNd(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = createNavigation(level); // 使用专门的创建方法
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
        if (!level.isClientSide) setResting(true);
    }

    // 专门的导航创建方法
    public FlyingPathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.FOLLOW_RANGE, 16.0D) // 追踪范围16格
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D) // 攻击击退
                .add(Attributes.ATTACK_SPEED, 4.0D); // 攻击速度（每秒攻击次数）
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        // 确保导航系统正确初始化
        if (this.navigation == null || !(this.navigation instanceof FlyingPathNavigation)) {
            this.navigation = createNavigation(this.level());
        }
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    /* ------------------------------------------------------------ */
    /*  数据同步                                                     */
    /* ------------------------------------------------------------ */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_FLYING,   false);
        entityData.define(DATA_RESTING,  true);
        entityData.define(DATA_SLEEPING, false);
        entityData.define(DATA_MODE, Mode.NORMAL.ordinal());
        entityData.define(DATA_CAN_HANG, false);
    }

    /* ------------------------------------------------------------ */
    /*  AI 注册                                                     */
    /* ------------------------------------------------------------ */
    @Override
    protected void registerGoals() {
        /* 通用 */
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new BatSleepOnSpotGoal());
        goalSelector.addGoal(3, new BatAutoSleepGoal());
        goalSelector.addGoal(4, new BatRestGoal());
        goalSelector.addGoal(5, new BatWanderGoal());
        goalSelector.addGoal(6, new BatFollowOwnerGoal(this, FOLLOW_SPEED, (float) MIN_FOLLOW, (float) MAX_FOLLOW, false));
        goalSelector.addGoal(7, new SimpleAttackGoal());
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8F));

        /* 目标选择器 - 修复后的版本 */
        // 最高优先级：主人被攻击时
        targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
        // 第二优先级：攻击主人的目标
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        // 第三优先级：自己被攻击时
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
        // 第四优先级：攻击非主人的玩家（野生状态下）
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                living -> !isTame() && living != this.getOwner()));
        // 最低优先级：攻击其他生物（非玩家且非主人）
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                living -> !(living instanceof Player) && living != this.getOwner()));
    }

    private class BatAutoSleepGoal extends Goal {
        private int cooldown = 0;
        private int sleepTicks = 0;
        private BlockPos chosenSleepPos;

        BatAutoSleepGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // 只对野生有效，且不在冷却中
            if (isTame() || --cooldown > 0 || isSleeping() || isOrderedToSit()) {
                return false;
            }

            // 战斗状态不睡觉
            if (getTarget() != null || hurtTime > 0) {
                return false;
            }

            // 只在夜晚睡觉（13000-23000 ticks）
            long time = level().getDayTime() % 24000;
            boolean isNight = time > 13000 && time < 23000;
            if (!isNight) {
                return false;
            }

            // 随机触发（约每10秒检测一次）
            if (random.nextFloat() > 0.05F) {
                return false;
            }

            // 寻找安全的睡觉位置
            chosenSleepPos = findSafeSleepPosition();
            return chosenSleepPos != null;
        }

        @Override
        public void start() {
            if (chosenSleepPos != null) {
                // 移动到睡觉位置
                safeNavigationMoveTo(
                        chosenSleepPos.getX() + 0.5,
                        chosenSleepPos.getY(),
                        chosenSleepPos.getZ() + 0.5,
                        1.2D
                );
            }
            sleepTicks = 0;
        }

        @Override
        public void tick() {
            sleepTicks++;

            // 如果已经到达睡觉位置，开始睡觉
            if (chosenSleepPos != null && distanceToSqr(Vec3.atCenterOf(chosenSleepPos)) < 2.0) {
                setMode(Mode.SLEEP);
            }

            // 自动唤醒条件
            if (shouldWakeUp()) {
                stop();
            }
        }

        private boolean shouldWakeUp() {
            // 天亮唤醒
            long time = level().getDayTime() % 24000;
            boolean isDay = time < 13000 || time >= 23000;

            // 被惊扰唤醒
            boolean isDisturbed = getTarget() != null || hurtTime > 0;

            // 睡够时间（3-8分钟）
            boolean sleptEnough = sleepTicks > (3600 + random.nextInt(3600));

            return isDay || isDisturbed || sleptEnough;
        }

        @Override
        public boolean canContinueToUse() {
            return !isTame() && !shouldWakeUp() && chosenSleepPos != null;
        }

        @Override
        public void stop() {
            if (isSleeping()) {
                setMode(Mode.NORMAL); // 切换到正常模式唤醒
            }
            chosenSleepPos = null;
            cooldown = 600 + random.nextInt(600); // 30-60秒冷却
        }

        @Nullable
        private BlockPos findSafeSleepPosition() {
            BlockPos currentPos = blockPosition();

            // 优先在当前位置附近寻找
            for (int i = 0; i < 10; i++) {
                BlockPos checkPos = currentPos.offset(
                        random.nextInt(7) - 3,
                        random.nextInt(5) - 2,
                        random.nextInt(7) - 3
                );

                if (!level().isInWorldBounds(checkPos)) continue;

                // 检查位置安全性
                if (isSleepPositionSafe(checkPos)) {
                    return checkPos;
                }
            }

            return null;
        }

        private boolean isSleepPositionSafe(BlockPos pos) {
            // 检查光照（不能太亮）
            if (level().getMaxLocalRawBrightness(pos) > 4) {
                return false;
            }

            // 检查附近是否有怪物（8格内）
            AABB area = new AABB(pos).inflate(8);
            if (!level().getEntitiesOfClass(Monster.class, area).isEmpty()) {
                return false;
            }

            // 检查是否可到达
            if (navigation != null) {
                return navigation.createPath(pos, 0) != null;
            }

            return true;
        }
    }

    private class SimpleAttackGoal extends Goal {
        private int attackCooldown = 0;

        SimpleAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = KivatBatTwoNd.this.getTarget();
            boolean result = target != null && target.isAlive();
            return result;
        }

        @Override
        public void tick() {
            LivingEntity target = KivatBatTwoNd.this.getTarget();
            if (target == null || !target.isAlive()) return;

            KivatBatTwoNd.this.getNavigation().moveTo(target, 1.6D);

            double distance = KivatBatTwoNd.this.distanceTo(target);
            if (--attackCooldown <= 0 && distance <= getAttackRange()) {
                KivatBatTwoNd.this.doHurtTarget(target);
                attackCooldown = 20;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }
    }

    private class BatFollowOwnerGoal extends FollowOwnerGoal {
        public BatFollowOwnerGoal(TamableAnimal entity, double speed, float min, float max, boolean teleport) {
            super(entity, speed, min, max, teleport);
        }

        @Override
        public boolean canUse() {
            return super.canUse()         // 主人存在
                    && !isSleeping()       // 没睡觉
                    && !isOrderedToSit()  // 没坐下
                    ;
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !isSleeping() && !isOrderedToSit();
        }
    }

    private class BatSleepOnSpotGoal extends Goal {
        public BatSleepOnSpotGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return isSleeping() && !isOrderedToSit();   // 睡觉且没有坐下
        }

        @Override
        public void start() {
            // 立即停止所有移动
            getNavigation().stop();
            setDeltaMovement(Vec3.ZERO);
            setYHeadRot(getYRot());

            // 确保找到合适的睡觉位置
            BlockPos sleepPos = findSleepPos();
            if (sleepPos != null && level().isInWorldBounds(sleepPos)) {
                setPos(sleepPos.getX() + 0.5, sleepPos.getY(), sleepPos.getZ() + 0.5);
            }
        }

        @Override
        public void tick() {
            // 完全静止
            setDeltaMovement(Vec3.ZERO);
            setYHeadRot(getYRot());

            // 关闭重力（如果是倒挂）
            setNoGravity(entityData.get(DATA_CAN_HANG));
        }

        @Override
        public boolean canContinueToUse() {
            return isSleeping() && !isOrderedToSit();
        }

        @Override
        public void stop() {
            setNoGravity(false); // 恢复重力
        }
    }

    /* ------------------------------------------------------------ */
    /*  每 tick 逻辑                                                 */
    /* ------------------------------------------------------------ */

    // 获取攻击范围
    public double getAttackRange() {
        return 2.5; // 2.5格攻击距离
    }

    @Override
    public void tick() {
        super.tick();

        // 添加实体有效性检查
        if (!this.isAlive() || this.isRemoved()) {
            return;
        }

        /* —— 睡觉时特殊处理 —— */
        if (isSleeping()) {
            setDeltaMovement(Vec3.ZERO);
            if (navigation != null) {
                navigation.stop();
            }
            return; // 跳过其他逻辑
        }

        // 安全检查：确保当前坐标有效
        BlockPos currentPos = blockPosition();
        if (!level().isInWorldBounds(currentPos)) {
            // 如果坐标无效，移动到安全位置
            int safeY = Math.max(level().getSeaLevel(), level().getMinBuildHeight() + 10);
            setPos(getX(), safeY, getZ());
            return;
        }

        /* —— 原有的翅膀拍打逻辑 —— */
        if (isFlapping()) flapCooldown = 5;

        /* —— 坐下时强制落地 —— */
        if (isOrderedToSit()) {
            // 1. 找脚下地面
            BlockPos ground = findGroundBelow(blockPosition());
            // 2. 直接瞬移到地面（+0.1 避免卡进方块）
            if (ground != null && Math.abs(getY() - (ground.getY() + 0.1)) > 0.1) {
                setPosRaw(getX(), ground.getY() + 0.1, getZ());
            }
            // 3. 静止
            setDeltaMovement(Vec3.ZERO);
            setYRot(yHeadRot);          // 保持朝向
            if (navigation != null) {
                navigation.stop();          // 停止导航
            }
            return;                     // 其余飞行逻辑全部跳过
        }

        // 如果有攻击目标，确保处于飞行状态
        if (getTarget() != null && !isFlying()) {
            setFlying(true);
            setResting(false);
            setSleeping(false);
            setOrderedToSit(false);
        }

        /* —— 睡觉时特殊处理 —— */
        if (isSleeping()) {
            setDeltaMovement(Vec3.ZERO);
            // 保持睡觉位置
            BlockPos sleepPos = findSleepPos();
            if (sleepPos != null && distanceToSqr(Vec3.atCenterOf(sleepPos)) > 1.0) {
                setPosRaw(sleepPos.getX() + 0.5, sleepPos.getY(), sleepPos.getZ() + 0.5);
            }
            if (navigation != null) {
                navigation.stop();
            }
            return;
        }

        /* —— 原有休息/飞行逻辑 —— */
        if (isResting()) {
            setDeltaMovement(Vec3.ZERO);
            setPosRaw(getX(), Mth.floor(getY()) + 1.0 - getBbHeight(), getZ());
            if (navigation != null) {
                navigation.stop();
            }
        } else {
            // 飞行状态，允许正常移动
            setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
    }

    // 检查是否正在飞行
    public boolean isFlying() {
        return entityData.get(DATA_FLYING);
    }

    // 设置飞行状态
    public void setFlying(boolean flying) {
        entityData.set(DATA_FLYING, flying);
        if (flying) {
            setResting(false);
            setOrderedToSit(false);
        }
    }

    // 改进的寻找地面方法
    @Nullable
    private BlockPos findGroundBelow(BlockPos from) {
        // 安全检查：确保起始坐标有效
        if (!level().isInWorldBounds(from)) {
            return null;
        }

        int minY = Math.max(level().getMinBuildHeight(), from.getY() - 50); // 限制搜索深度
        for (int y = from.getY(); y >= minY; y--) {
            BlockPos pos = new BlockPos(from.getX(), y, from.getZ());

            // 安全检查：确保坐标有效
            if (!level().isInWorldBounds(pos)) {
                return null;
            }

            BlockState state = level().getBlockState(pos);
            if (!state.isAir() && state.isSolidRender(level(), pos)) {
                BlockPos result = pos.above();
                // 确保结果坐标有效
                if (level().isInWorldBounds(result)) {
                    return result; // 返回方块顶部一格
                }
                return pos; // 如果上方无效，返回当前方块
            }
        }
        return null;
    }

    /* 主 AI 逻辑 */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // 添加实体有效性检查
        if (!this.isAlive() || this.isRemoved()) {
            return;
        }

        // 添加导航系统检查
        if (this.navigation == null || !(this.navigation instanceof FlyingPathNavigation)) {
            this.navigation = createNavigation(this.level());
            return;
            }

        // 检查实体是否有效
        if (!this.isAlive() || this.isRemoved()) {
            return;
        }

        boolean wasFlying = entityData.get(DATA_FLYING);
        boolean shouldFly = !isResting() && !isOrderedToSit() && !isSleeping();
        if (wasFlying != shouldFly) {
            entityData.set(DATA_FLYING, shouldFly);
        }

        if (shouldFly) {
            if (!this.level().isInWorldBounds(this.blockPosition())) {
                this.setPos(this.getX(), this.level().getSeaLevel() + 2, this.getZ());
            }

            if (targetPosition != null && (navigation.isDone() || navigation.getPath() == null)) {
                targetPosition = getOwner() == null ? randomNearby() : randomAround(getOwner());

                if (targetPosition != null && this.level().isInWorldBounds(targetPosition)) {
                    // 添加路径查找前的安全检查
                    if (this.isAlive() && !this.isRemoved()) {
                        safeNavigationMoveTo(
                                targetPosition.getX() + 0.5,
                                targetPosition.getY() + 0.5,
                                targetPosition.getZ() + 0.5,
                                1.0D
                        );
                    }
                }
            }
        }
    }

    // 安全导航方法
    private boolean safeNavigationMoveTo(double x, double y, double z, double speed) {
        if (this.navigation == null || !(this.navigation instanceof FlyingPathNavigation)) {
            this.navigation = createNavigation(this.level());
            return false;
        }

        // 添加更多安全检查
        if (!this.isAlive() || this.isRemoved() || this.navigation == null) {
            return false;
        }

        try {
            return this.navigation.moveTo(x, y, z, speed);
        } catch (Exception e) {
            kamen_rider_boss_you_and_me.LOGGER.error("安全导航错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 为睡觉模式找一个落脚点：
     * 1. 优先找头顶可倒挂方块 → 返回方块底面坐标
     * 2. 找不到 → 返回地面坐标
     */
    @Nullable
    private BlockPos findSleepPos() {
        BlockPos currentPos = blockPosition();

        // 安全检查
        if (!level().isInWorldBounds(currentPos)) {
            return currentPos;
        }

        // 1. 优先查找可倒挂的位置（3x3x3范围内）
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = currentPos.offset(x, y, z);
                    BlockPos abovePos = checkPos.above();

                    if (level().isInWorldBounds(abovePos) && canHangBelow(abovePos, level())) {
                        entityData.set(DATA_CAN_HANG, true);
                        return checkPos;
                    }
                }
            }
        }

        // 2. 找不到可倒挂位置，返回地面位置
        entityData.set(DATA_CAN_HANG, false);
        BlockPos ground = findGroundBelow(currentPos);
        return ground != null ? ground : currentPos;
    }

    /* ------------------------------------------------------------ */
    /*  工具：位置 & 移动                                             */
    /* ------------------------------------------------------------ */
    private BlockPos randomAround(LivingEntity owner) {
        double a = random.nextDouble() * Math.PI * 2;
        double r = 2 + random.nextDouble() * 4;
        return BlockPos.containing(
                owner.getX() + Math.cos(a) * r,
                owner.getY() + 1.5 + random.nextDouble(),
                owner.getZ() + Math.sin(a) * r
        );
    }

    private BlockPos randomNearby() {
        return BlockPos.containing(
                getX() + random.nextInt(7) - 3.5,
                getY() + random.nextInt(6) - 2,
                getZ() + random.nextInt(7) - 3.5
        );
    }

    private static boolean canHangBelow(BlockPos pos, Level level) {
        // 安全检查：确保坐标有效
        if (!level.isInWorldBounds(pos)) {
            return false;
        }

        BlockPos abovePos = pos.above();
        // 安全检查：确保上方坐标有效
        if (!level.isInWorldBounds(abovePos)) {
            return false;
        }

        BlockState above = level.getBlockState(abovePos);
        return above.is(BlockTags.LOGS) || above.is(BlockTags.LEAVES) || above.isRedstoneConductor(level, abovePos);
    }

    public boolean isFlapping() {
        return !isResting() && tickCount % TICKS_PER_FLAP == 0;
    }

    public boolean isResting()   { return entityData.get(DATA_RESTING); }
    public void    setResting(boolean resting) {
        entityData.set(DATA_RESTING, resting);
    }

    public boolean isSleeping()  { return entityData.get(DATA_SLEEPING); }
    public void    setSleeping(boolean sleeping) {
        entityData.set(DATA_SLEEPING, sleeping);
    }

    /* ------------------------------------------------------------ */
    /*  交互 & 驯服                                                  */
    /* ------------------------------------------------------------ */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        // 吃食物回血逻辑（防 NPE）
        if (item.isEdible() && this.getHealth() < this.getMaxHealth()) {
            var props = item.getFoodProperties(player);
            if (props != null) {                       // ← 关键：判空
                if (!player.getAbilities().instabuild) {
                    item.shrink(1);
                }
                this.heal(props.getNutrition() * 2.0F);
                this.playSound(this.getEatingSound(item), 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }

        // 若玩家没有纯度，先给
        if (PlayerBloodlineHelper.getFangPurity(player) == 0.0F)
            PlayerBloodlineHelper.setFangPurity(player, 0.8F);

        if (item.getItem() == ModItems.BLOODLINE_FANG.get() && !isTame()) {
            if (!player.getAbilities().instabuild) item.shrink(1);
            if (PlayerBloodlineHelper.getFangPurity(player) >= 0.5F) {
                tame(player);
                level().broadcastEntityEvent(this, (byte) 7);
                return InteractionResult.SUCCESS;
            } else {
                level().broadcastEntityEvent(this, (byte) 6);
                return InteractionResult.FAIL;
            }
        }

        // Shift+右键切换动画
        // Shift+右键切换动画
        if (isTame() && isOwnedBy(player) && player.isShiftKeyDown()) {
            if (!level().isClientSide) {
                int next = (getMode().ordinal() + 1) % Mode.values().length;

                // 驯服后的Kivat可以切换到SLEEP模式
                Mode newMode = Mode.values()[next];
                setMode(newMode);

                player.displayClientMessage(
                        Component.translatable("dialog.kivat.mode_change." + newMode.name().toLowerCase()),
                        true
                );
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public Mode getMode() {
        return Mode.values()[entityData.get(DATA_MODE)];
    }

    public void setMode(Mode mode) {
        // 野生Kivat不能手动切换到SLEEP模式（只能自动），但允许从BatAutoSleepGoal调用
        if (mode == Mode.SLEEP && !isTame()) {
            // 获取调用栈，检查是否从BatAutoSleepGoal调用的
            boolean fromAutoSleep = false;
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().contains("BatAutoSleepGoal")) {
                    fromAutoSleep = true;
                    break;
                }
            }

            if (!fromAutoSleep) {
                return; // 野生Kivat只能自动睡觉，不能手动切换
            }
        }

        // 清除所有状态
        setResting(false);
        setOrderedToSit(false);
        setSleeping(false);
        setFlying(false);

        switch (mode) {
            case SLEEP:
                setSleeping(true);
                setNoGravity(entityData.get(DATA_CAN_HANG)); // 只有倒挂时才关闭重力

                // 确保找到合适的睡觉位置
                BlockPos sleepPos = findSleepPos();
                if (sleepPos != null && level().isInWorldBounds(sleepPos)) {
                    setPos(sleepPos.getX() + 0.5, sleepPos.getY(), sleepPos.getZ() + 0.5);
                }
                break;

            case NORMAL:
                if (isTame()) {
                    setOrderedToSit(true); // 驯服后默认坐下
                } else {
                    setResting(true); // 野生时默认休息
                }
                break;

            case SAY:
                setFlying(true); // 说话模式保持飞行
                break;
        }

        entityData.set(DATA_MODE, mode.ordinal());
        speaking = (mode == Mode.SAY);
    }

    @Override
    public void tame(Player player) {
        super.tame(player);
        setOrderedToSit(false);   // ← 关键：不要默认坐下
        setResting(false);
        if (!level().isClientSide && player instanceof ServerPlayer sp) {
            kamen_rider_boss_you_and_me.TAMED_KIVAT_TRIGGER.trigger(sp);
            float purity = PlayerBloodlineHelper.getFangPurity(player);
            int amplifier = Math.min(4, (int) (purity * 5));
            player.addEffect(new MobEffectInstance(
                    ModEffects.FANG_BLOODLINE.get(),
                    20 * 60 * 5,
                    amplifier,
                    false, false, true));
        }
    }

    /* ------------------------------------------------------------ */
    /*  动画 & Geckolib                                              */
    /* ------------------------------------------------------------ */
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean speaking = false;
    public  boolean isSpeaking() { return speaking; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar reg) {
        reg.add(new AnimationController<>(this, "main", 5, this::predicate));
    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> state) {
        if (isSleeping()) {
            // 真正睡觉：头顶有方块 → HOVER（倒挂）
            return entityData.get(DATA_CAN_HANG)
                    ? state.setAndContinue(HOVER)
                    : state.setAndContinue(SLEEP);   // 地面休息
        }
        if (isResting())  return state.setAndContinue(HOVER);
        if (isSpeaking()) return state.setAndContinue(SAY);
        return state.setAndContinue(FLY);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /* ------------------------------------------------------------ */
    /*  NBT 持久化                                                   */
    /* ------------------------------------------------------------ */
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Resting", isResting());
        tag.putBoolean("Sleeping", isSleeping());
        tag.putBoolean("CanHang", entityData.get(DATA_CAN_HANG));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setResting(tag.getBoolean("Resting"));
        setSleeping(tag.getBoolean("Sleeping"));
        entityData.set(DATA_CAN_HANG, tag.getBoolean("CanHang"));

        // ✅ 安全地重新初始化导航器
        if (level() != null && !level().isClientSide) {
            this.navigation = createNavigation(level());
        }
    }

    /* ------------------------------------------------------------ */
    /*  其它重写                                                     */
    /* ------------------------------------------------------------ */
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity entity) {}
    @Override protected void pushEntities() {}

    @Override
    public boolean hurt(DamageSource src, float amt) {
        if (src.getEntity() instanceof Player p && this.isOwnedBy(p)) {
            ItemStack stack = new ItemStack(ModItems.KIVAT_BAT_TWO_ND_ITEM.get());
            CompoundTag tag = new CompoundTag();

            this.save(tag);                    // 实体数据
            tag.putUUID("OwnerUUID", getOwnerUUID());
            tag.putBoolean("FromKivatEntity", true);

            // 去掉坐标、旋转、速度等与位置相关的字段
            tag.remove("Pos");
            tag.remove("Rotation");
            tag.remove("Motion");

            stack.setTag(tag);

            if (!p.getInventory().add(stack)) {
                p.drop(stack, false);
            }

            this.discard();
            return true;
        }

        // ✅ 被攻击后反击逻辑（野生状态下）
        if (!this.level().isClientSide && src.getEntity() instanceof LivingEntity attacker) {
            // 如果是驯服状态，且攻击者不是主人，才设置目标
            if (isTame()) {
                if (getOwner() != null && !attacker.equals(getOwner())) {
                    this.setTarget(attacker);
                }
            } else {
                // 野生状态下直接反击
                this.setTarget(attacker);
            }

            // 强制触发 Goal 重新评估
            this.goalSelector.tick();
            this.targetSelector.tick();
        }

        return super.hurt(src, amt);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 11) {
            // 触发 snap 动画
            this.setSleeping(false);
            this.setResting(false);
            // 这里可以播放额外粒子或声音
        }
        super.handleEntityEvent(id);
    }

    /* ------------------------------------------------------------ */
    /*  内部 Goal：休息                                              */
    /* ------------------------------------------------------------ */
    private class BatRestGoal extends Goal {
        private int cooldown = 0;
        BatRestGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            if (isResting() || isOrderedToSit()) return false;
            if (--cooldown > 0) return false;
            return random.nextFloat() < 0.1F && canHangBelow(blockPosition(), level());
        }

        @Override public void start() {
            setResting(true);
            cooldown = 200 + random.nextInt(200);
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;   // 不计算摔落伤害
    }

    /* ------------------------------------------------------------ */
    /*  内部 Goal：漫游                                              */
    /* ------------------------------------------------------------ */
    private class BatWanderGoal extends Goal {
        private int cooldown = 0;

        BatWanderGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !isResting() && !isOrderedToSit() && !isSleeping() && --cooldown <= 0;
        }

        @Override
        public void start() {
            cooldown = 40 + random.nextInt(40);
            targetPosition = getOwner() == null ? randomNearby() : randomAround(getOwner());

            // 实际移动到目标位置
            if (targetPosition != null && level().isInWorldBounds(targetPosition)) {
                safeNavigationMoveTo(
                        targetPosition.getX() + 0.5,
                        targetPosition.getY() + 0.5,
                        targetPosition.getZ() + 0.5,
                        1.0D
                );
            }
        }

        @Override
        public boolean canContinueToUse() {
            return navigation != null && !navigation.isDone() && !isResting() && !isOrderedToSit() && !isSleeping();
        }
    }
}