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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.Locale;

public class KivatBatTwoNd extends TamableAnimal implements GeoEntity {

    /* ===== 动画 ===== */
    private static final RawAnimation FLY   = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation SLEEP = RawAnimation.begin().thenPlay("sleep");
    private static final RawAnimation SAY   = RawAnimation.begin().thenLoop("say");
    private static final RawAnimation HOVER = RawAnimation.begin().thenPlay("hover");

    /* ===== 数据 Key ===== */
    private static final EntityDataAccessor<Boolean> DATA_FLYING   = SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RESTING  = SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.BOOLEAN);

    /* ===== 飞行常量 ===== */
    public  static final int TICKS_PER_FLAP = Mth.ceil(2.4166098F);
    private static final double FOLLOW_SPEED  = 1.2D;
    private static final double ATTACK_SPEED  = 1.2D;
    private static final double MIN_FOLLOW    = 3F;
    private static final double MAX_FOLLOW    = 10F;

    private BlockPos targetPosition;

    private static final EntityDataAccessor<Integer> DATA_MODE =
            SynchedEntityData.defineId(KivatBatTwoNd.class, EntityDataSerializers.INT);

    /* ------------------------------------------------------------ */
    /*  彩蛋                                                         */
    /* ------------------------------------------------------------ */
    public static boolean isClownBatCall(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        String s = text.toLowerCase(Locale.ROOT).replaceAll("[@，,\\s]", "").trim();
        return s.contains("那边的小丑蝙蝠过来") || s.contains("小丑蝙蝠过来") || s.contains("过来小丑蝙蝠");
    }
    public void temptToPlayer(Player player) {
        if (level().isClientSide || player == null || !isAlive() || !player.isAlive() || level() != player.level()) return;
        if (!(navigation instanceof FlyingPathNavigation)) navigation = createNavigation(level());
        navigation.stop();
        navigation.moveTo(player, 1.6D);
        setOrderedToSit(false);
        setResting(false);
        setSleeping(false);
        setFlying(true);
    }
    public static Component reply(String input) {
        input = input.toLowerCase(Locale.ROOT);
        if (input.contains("hello") || input.contains("你好") || input.contains("hi"))
            return Component.translatable("dialog.kivat.hello");
        if (input.contains("food") || input.contains("饿") || input.contains("meal"))
            return Component.translatable("dialog.kivat.food");
        if (input.contains("sleep") || input.contains("睡觉") || input.contains("rest"))
            return Component.translatable("dialog.kivat.sleep");
        if (input.matches(".*(?:血|blood|fang).*"))
            return Component.translatable("dialog.kivat.blood");
        return Component.translatable("dialog.kivat.unknown");
    }



    public enum Mode { NORMAL, SAY, SLEEP }

    /* ===== 运行时字段 ===== */
    private int flapCooldown = 0;                // 翅膀拍打计时
    /* ------------------------------------------------------------ */
    /*  构造 & 属性                                                  */
    /* ------------------------------------------------------------ */
    public KivatBatTwoNd(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.navigation = new FlyingPathNavigation(this, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
        if (!level.isClientSide) setResting(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.MOVEMENT_SPEED, 0.3F);
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
    }

    /* ------------------------------------------------------------ */
    /*  AI 注册                                                     */
    /* ------------------------------------------------------------ */
    @Override
    protected void registerGoals() {
        /* 行为 Goal */
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new BatSleepOnSpotGoal());
        goalSelector.addGoal(3, new BatFollowOwnerGoal(this, FOLLOW_SPEED,
                (float) MIN_FOLLOW, (float) MAX_FOLLOW, false));
        goalSelector.addGoal(4, new MeleeAttackGoal(this, ATTACK_SPEED, true) {
            @Override protected double getAttackReachSqr(LivingEntity target) {
                return 3.0 * 3.0;
            }
        });
        goalSelector.addGoal(5, new BatRestGoal());
        goalSelector.addGoal(6, new BatWanderGoal());
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8F));

        /* 目标选择器（必须！） */
        targetSelector.addGoal(0, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    private class BatFollowOwnerGoal extends FollowOwnerGoal {
        public BatFollowOwnerGoal(TamableAnimal entity, double speed, float min, float max, boolean teleport) {
            super(entity, speed, min, max, teleport);
        }

        @Override
        public boolean canUse() {
            return super.canUse()         // 主人存在
                    && !isSleeping()       // 没睡觉
                    && !isOrderedToSit();  // 没坐下
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
            return isSleeping();   // 仅在睡觉模式下执行
        }

        @Override
        public void start() {
            // 立即停止所有移动
            getNavigation().stop();
            setDeltaMovement(Vec3.ZERO);
            setYHeadRot(getYRot());
        }

        @Override
        public void tick() {
            // 1. 关掉浮力
            setNoGravity(true);
            // 2. 完全静止
            setDeltaMovement(Vec3.ZERO);
        }

        @Override
        public boolean canContinueToUse() {
            return isSleeping();
        }
    }

    /* ------------------------------------------------------------ */
    /*  每 tick 逻辑                                                 */
    /* ------------------------------------------------------------ */
    @Override
    public void tick() {
        super.tick();

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
            navigation.stop();          // 停止导航
            return;                     // 其余飞行逻辑全部跳过
        }

        /* —— 睡觉时特殊处理 —— */
        if (isSleeping()) {
            setDeltaMovement(Vec3.ZERO);
            // 保持睡觉位置
            BlockPos sleepPos = findSleepPos();
            if (sleepPos != null && distanceToSqr(Vec3.atCenterOf(sleepPos)) > 1.0) {
                setPosRaw(sleepPos.getX() + 0.5, sleepPos.getY(), sleepPos.getZ() + 0.5);
            }
            navigation.stop();
            return;
        }

        /* —— 原有休息/飞行逻辑 —— */
        if (isResting()) {
            setDeltaMovement(Vec3.ZERO);
            setPosRaw(getX(), Mth.floor(getY()) + 1.0 - getBbHeight(), getZ());
            navigation.stop();
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
        for (int y = from.getY(); y >= level().getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(from.getX(), y, from.getZ());
            BlockState state = level().getBlockState(pos);
            if (!state.isAir() && state.isSolidRender(level(), pos)) {
                return pos.above(); // 返回方块顶部一格
            }
        }
        return null;
    }

    /* 主 AI 逻辑 */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // 处理飞行状态更新
        boolean wasFlying = entityData.get(DATA_FLYING);
        boolean shouldFly = !isResting() && !isOrderedToSit() && !isSleeping();

        if (wasFlying != shouldFly) {
            entityData.set(DATA_FLYING, shouldFly);
        }

        // 飞行时处理逻辑
        if (shouldFly) {
            // 确保导航系统在工作
            if (targetPosition != null && navigation.isDone()) {
                // 到达目标后寻找新目标
                targetPosition = getOwner() == null ? randomNearby() : randomAround(getOwner());
                if (targetPosition != null) {
                    navigation.moveTo(
                            targetPosition.getX() + 0.5,
                            targetPosition.getY() + 0.5,
                            targetPosition.getZ() + 0.5,
                            1.0D
                    );
                }
            }
        }
    }

    /**
     * 为睡觉模式找一个落脚点：
     * 1. 优先找头顶可倒挂方块 → 返回方块底面坐标
     * 2. 找不到 → 返回地面坐标
     */
    private BlockPos findSleepPos() {
        BlockPos eye = blockPosition();
        // 向上找 6 格内的“顶”
        for (int y = 0; y <= 6; y++) {
            BlockPos above = eye.above(y);
            if (canHangBelow(above, level())) {
                // 蝙蝠身体高度 0.5F，挂底面
                return above.below();
            }
        }
        // 找不到 → 直接落在地面
        return findGroundBelow(eye);
    }


    /* ------------------------------------------------------------ */
    /*  休息 & 飞行逻辑                                              */
    /* ------------------------------------------------------------ */
    private void handleRestLogic() {
        if (!canHangBelow(blockPosition(), level())) setResting(false);
    }

    private void handleFlightLogic() {

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
        BlockState above = level.getBlockState(pos.above());
        return above.is(BlockTags.LOGS) || above.is(BlockTags.LEAVES) || above.isRedstoneConductor(level, pos);
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
        if (isTame() && isOwnedBy(player) && player.isShiftKeyDown()) {
            if (!level().isClientSide) {
                int next = (getMode().ordinal() + 1) % Mode.values().length;
                setMode(Mode.values()[next]);
                player.displayClientMessage(
                        Component.literal("Kivat 状态: " + Mode.values()[next]), true);
            }
            return InteractionResult.SUCCESS;
        }

        /* ===== 新增：右键喂食回血 ===== */
        if (isTame() && isOwnedBy(player) && hand == InteractionHand.MAIN_HAND) {
            // 满血不处理
            if (getHealth() >= getMaxHealth()) {
                if (!level().isClientSide) {
                    player.displayClientMessage(Component.translatable("dialog.kivat.full"), true);
                }
                return InteractionResult.SUCCESS;
            }

            // 玩家饱食度不足
            if (player.getFoodData().getFoodLevel() < 3) {
                if (!level().isClientSide) {
                    player.displayClientMessage(Component.translatable("dialog.kivat.hungry"), true);
                }
                return InteractionResult.SUCCESS;
            }

            // 扣除饱食度并治疗
            if (!level().isClientSide) {
                player.getFoodData().eat(-3, 0);   // 扣 3 点饱食度
                heal(4.0F);                        // 回 4 点血
                level().broadcastEntityEvent(this, (byte) 7); // 爱心粒子
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public Mode getMode() {
        return Mode.values()[entityData.get(DATA_MODE)];
    }

    public void setMode(Mode mode) {
        boolean sleep = (mode == Mode.SLEEP);
        // 开关重力
        setNoGravity(sleep);
        if (!sleep) {
            setNoGravity(false);
        }
        if (mode == Mode.SLEEP) {
            BlockPos sleepPos = findSleepPos();
            if (sleepPos != null) {
                setPosRaw(sleepPos.getX() + 0.5, sleepPos.getY(), sleepPos.getZ() + 0.5);
            }
            // 关键：把睡觉标志设为 true，Goal 会接管
            setSleeping(true);          // 触发 BatSleepOnSpotGoal
            setOrderedToSit(false);     // 不再用 SitWhenOrderedToGoal
        } else {
            setSleeping(false);         // 退出睡觉，Goal 自动失效
            setOrderedToSit(mode == Mode.NORMAL && isTame()); // 需要时才用原坐下
        }

        entityData.set(DATA_MODE, mode.ordinal());
        setSleeping(mode == Mode.SLEEP);
        speaking = (mode == Mode.SAY);

        // 5 tick 内禁止再次切换（防误触）
        if (!level().isClientSide) {
            ((ServerLevel) level()).scheduleTick(blockPosition(), level().getBlockState(blockPosition()).getBlock(), 5);
        }
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
        if (isSleeping()) return state.setAndContinue(SLEEP);
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setResting(tag.getBoolean("Resting"));
        setSleeping(tag.getBoolean("Sleeping"));
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
            tag.putBoolean("FromKivatEntity", true); // 标记
            stack.setTag(tag);

            // 直接进背包；如果满则掉地上
            if (!p.getInventory().add(stack)) {
                p.drop(stack, false);
            }

            this.discard();
            return true;
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
            if (targetPosition != null) {
                navigation.moveTo(
                        targetPosition.getX() + 0.5,
                        targetPosition.getY() + 0.5,
                        targetPosition.getZ() + 0.5,
                        1.0D
                );
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !navigation.isDone() && !isResting() && !isOrderedToSit() && !isSleeping();
        }
    }
}