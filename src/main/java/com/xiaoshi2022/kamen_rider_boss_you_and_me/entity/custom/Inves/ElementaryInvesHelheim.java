package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.AttractGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.FlyAndAttackGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.PickUpHelheimFruitGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.helheimtems.FactionLeader;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Arrays;

public class ElementaryInvesHelheim extends Monster implements GeoEntity {
    protected static final RawAnimation GOMAD = RawAnimation.begin().thenPlay("gomad");
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("Attack");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
    public static final TagKey<Item> HELHEIM_FOOD_TAG =
            ItemTags.create(new ResourceLocation("kamen_rider_weapon_craft", "kamen_rider_helheim_food"));
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private String originalFactionId;
    private FactionLeader factionLeader;
    private static final String[] VARIANTS = {"red", "blue", "green"};
    private boolean isFlying = false;
    public int flyingCooldown = 0;
    private int shiftPressTime = 0;
    private int jumpPressTime = 0;
    private int flightToggleCooldown = 0; // 专门用于飞行切换的冷却

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
        this.setNoGravity(flying);

        if (!flying) {
            this.setNoGravity(false);
            // 清除垂直速度
            this.setDeltaMovement(this.getDeltaMovement().x * 0.8, this.getDeltaMovement().y * 0.5, this.getDeltaMovement().z * 0.8);
        } else {
            // 飞行时给予一点初始上升动力
            this.setDeltaMovement(this.getDeltaMovement().x, 0.2, this.getDeltaMovement().z);
        }
    }

    private String variant = "blue";

    public void setFactionLeader(FactionLeader leader) {
        this.factionLeader = leader;
        if (leader != null) {
            leader.addMinion(this);
            this.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false
            ));
        }
    }

    private static final double PROTECT_RANGE = 20.0;
    public LivingEntity master;
    private int loyaltyTime = 0;

    public void setMaster(LivingEntity master) {
        if (this.master == null && this.factionLeader != null) {
            this.originalFactionId = this.factionLeader.getFactionId();
        }

        this.master = master;
        if (master instanceof FactionLeader) {
            this.factionLeader = (FactionLeader) master;
        }
        this.loyaltyTime = 1200;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.master != null) {
            if (target == this.master || this.master.isAlliedTo(target)) {
                return false;
            }

            if (target instanceof ElementaryInvesHelheim targetInves && targetInves.getMaster() != null) {
                return !this.master.equals(targetInves.getMaster());
            }

            if (target instanceof Player targetPlayer) {
                KRBVariables.PlayerVariables targetVars = targetPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                if (targetVars.isOverlord && !this.master.equals(targetPlayer)) {
                    return true;
                }
            }

            return true;
        }

        if (target instanceof Player) {
            Player player = (Player) target;
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            if (variables.isOverlord) {
                return false;
            }
        }

        String myFaction = this.factionLeader != null ? this.factionLeader.getFactionId() : null;
        String targetFaction = target instanceof FactionLeader ?
                ((FactionLeader) target).getFactionId() : null;

        if (myFaction != null && targetFaction != null) {
            return !myFaction.equals(targetFaction);
        }

        return super.canAttack(target);
    }

    public ElementaryInvesHelheim(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setVariant(VARIANTS[random.nextInt(VARIANTS.length)]);
        this.factionLeader = null;
        this.master = null;
        this.isFlying = false;
        this.flyingCooldown = 0;
        this.loyaltyTime = 0;
    }

    public void setVariant(String variant) {
        if (Arrays.asList(VARIANTS).contains(variant)) {
            this.variant = variant;
        }
    }

    public String getVariant() {
        return variant;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("OriginalFactionId", Tag.TAG_STRING)) {
            this.originalFactionId = tag.getString("OriginalFactionId");
        }

        if (tag.contains("CurrentFactionId", Tag.TAG_STRING)) {
            String factionId = tag.getString("CurrentFactionId");
            this.factionLeader = findFactionLeaderById(factionId);
        }

        if (tag.contains("IsFlying", Tag.TAG_BYTE)) {
            this.isFlying = tag.getBoolean("IsFlying");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        if (this.originalFactionId != null) {
            tag.putString("OriginalFactionId", this.originalFactionId);
        }

        if (this.factionLeader != null) {
            tag.putString("CurrentFactionId", this.factionLeader.getFactionId());
        }

        tag.putBoolean("IsFlying", this.isFlying);
    }

    public boolean canBeRiddenInWater(Entity rider) {
        return true;
    }

    public boolean canBeRidden(Entity rider) {
        if (rider instanceof Player player && this.master == player) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            return variables.isOverlord;
        }
        return false;
    }

    @Override
    public void rideTick() {
        super.rideTick();

        Entity rider = this.getFirstPassenger();
        if (rider instanceof Player player) {
            // 下马检测
            if (player.isShiftKeyDown()) {
                if (this.shiftPressTime++ > 20) {
                    player.stopRiding();
                    player.displayClientMessage(Component.literal("已从异域者身上下来"), true);
                    this.shiftPressTime = 0;
                    this.jumpPressTime = 0;
                    return;
                }
            } else {
                this.shiftPressTime = 0;
            }

            // 飞行切换检测 - 使用更简单直接的方法
            if (flightToggleCooldown > 0) {
                flightToggleCooldown--;
            }

            // 检测空格键按下（飞行切换）
            if (isJumpKeyPressed(player) && flightToggleCooldown == 0 && this.flyingCooldown <= 0) {
                flightToggleCooldown = 10; // 防止重复触发

                this.setFlying(!this.isFlying());
                this.flyingCooldown = 20;

                if (this.isFlying()) {
                    player.displayClientMessage(Component.literal("飞行模式已启动！WASD移动，空格上升，Shift下降"), true);
                } else {
                    player.displayClientMessage(Component.literal("飞行模式已关闭"), true);
                }
            }

            // 冷却处理
            if (this.jumpPressTime > 0) this.jumpPressTime--;
            if (this.flyingCooldown > 0) this.flyingCooldown--;
        }
    }

    // 简化的跳跃键检测
    private boolean isJumpKeyPressed(Player player) {
        // 直接检查玩家是否按下了跳跃键
        // 在 Minecraft 中，当玩家按下空格时，zza 会有特定的表现
        return player.zza > 0 && !player.onGround();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && this.canBeRidden(passenger);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (!this.getPassengers().isEmpty() && this.getFirstPassenger() instanceof Player player) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            if (variables.isOverlord) {
                return source.is(DamageTypes.IN_WALL) || super.isInvulnerableTo(source);
            }
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(DamageSource source, float damageAmount) {
        if (!this.getPassengers().isEmpty() && this.getFirstPassenger() instanceof Player player) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            if (variables.isOverlord) {
                damageAmount *= 0.5F;
            }
        }
        return super.hurt(source, damageAmount);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive() && this.isVehicle() && this.getFirstPassenger() instanceof Player player) {
            // 同步旋转
            this.setYRot(player.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(player.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());

            // 完全停止AI
            this.getNavigation().stop();
            this.setTarget(null);
            this.setAggressive(false);

            // 获取移动输入
            float forward = player.zza; // 前后移动 (W/S)
            float strafe = player.xxa;  // 左右移动 (A/D)

            // 计算移动方向 - 简化计算
            double moveX = 0;
            double moveZ = 0;

            if (forward != 0 || strafe != 0) {
                float yRotRad = this.getYRot() * ((float)Math.PI / 180F);

                // 前后移动
                moveX = -Math.sin(yRotRad) * forward;
                moveZ = Math.cos(yRotRad) * forward;

                // 左右移动 - 修正方向
                moveX += Math.cos(yRotRad) * strafe;
                moveZ += Math.sin(yRotRad) * strafe;

                // 标准化方向向量
                double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
                if (length > 0) {
                    moveX /= length;
                    moveZ /= length;
                }
            }

            // 飞行状态处理
            if (this.isFlying()) {
                this.setNoGravity(true);

                // 垂直移动控制
                double moveY = 0;
                if (isJumpKeyPressed(player)) {
                    moveY = 0.15; // 上升
                } else if (player.isShiftKeyDown()) {
                    moveY = -0.15; // 下降
                }

                // 应用飞行移动 - 更平滑的速度
                double speed = 0.25D;
                Vec3 currentMovement = this.getDeltaMovement();
                Vec3 newMovement = new Vec3(
                        moveX * speed + currentMovement.x * 0.7,
                        moveY + currentMovement.y * 0.8,
                        moveZ * speed + currentMovement.z * 0.7
                );

                this.setDeltaMovement(newMovement);

            } else {
                // 地面移动 - 更流畅
                this.setNoGravity(false);
                double speed = 0.2D;
                double currentY = this.getDeltaMovement().y;

                Vec3 newMovement = new Vec3(
                        moveX * speed,
                        currentY,
                        moveZ * speed
                );
                this.setDeltaMovement(newMovement);
            }

            // 应用移动 - 关键步骤
            this.move(MoverType.SELF, this.getDeltaMovement());

            // 更新动画
            this.calculateEntityAnimation(false);
            return;
        }

        // 没有乘客时使用默认移动
        super.travel(travelVector);
    }

    @Override
    public void tick() {
        super.tick();

        // 有乘客时完全禁用AI
        if (this.isVehicle()) {
            this.getNavigation().stop();
            this.setTarget(null);
            this.setAggressive(false);

            // 清除所有运行的目标
            this.goalSelector.getRunningGoals().forEach(goal -> goal.stop());
            this.targetSelector.getRunningGoals().forEach(goal -> goal.stop());
        }

        if (getMaster() != null && !getMaster().isAlive()) {
            this.discard();
            return;
        }

        if (flyingCooldown > 0) {
            flyingCooldown--;
        }

        // 自动关闭飞行
        if (isFlying && this.getTarget() == null && !this.isVehicle()) {
            this.setFlying(false);
            flyingCooldown = 100;
        }

        if (!this.level().isClientSide && this.tickCount % 20 == 0) {
            updateMasterControl();
        }
    }

    public boolean isVehicle() {
        return !this.getPassengers().isEmpty();
    }

    private void updateMasterControl() {
        if (master != null) {
            if (loyaltyTime > 0) loyaltyTime--;

            if (!master.isAlive() || loyaltyTime <= 0) {
                releaseControl();
                return;
            }

            LivingEntity masterTarget = master.getLastHurtMob();
            if (masterTarget != null && masterTarget != this.getTarget()) {
                this.setTarget(masterTarget);
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            }

            LivingEntity masterAttacker = master.getLastHurtByMob();
            if (masterAttacker != null && masterAttacker != this.getTarget()) {
                this.setTarget(masterAttacker);
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            }

            if (this.distanceToSqr(master) > PROTECT_RANGE * PROTECT_RANGE) {
                this.getNavigation().moveTo(master, 1.2);
            }

            if (master instanceof LordBaronEntity &&
                    master.getLastHurtByMob() != null &&
                    !this.isFlying()) {
                this.setFlying(true);
            }
        }
    }

    private void releaseControl() {
        if (this.originalFactionId != null) {
            this.factionLeader = findFactionLeaderById(this.originalFactionId);
        }
        this.master = null;
        this.loyaltyTime = 0;
    }

    private FactionLeader findFactionLeaderById(String factionId) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity instanceof FactionLeader leader &&
                    leader.getFactionId().equals(factionId)) {
                return leader;
            }
        }
        return null;
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean recentlyHit) {
        super.dropFromLootTable(damageSource, recentlyHit);

        if (!this.level().isClientSide) {
            ItemStack invesMeat = new ItemStack(ModItems.INVES_MEAT.get());
            this.spawnAtLocation(invesMeat, 0.0F);
        }
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(0, new ProtectMasterGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));

        this.targetSelector.addGoal(3, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return master == null && super.canUse();
            }
            @Override
            public boolean canContinueToUse() {
                return master == null && super.canContinueToUse();
            }
        });

        this.targetSelector.addGoal(4, new CustomAttackGoal<>(
                this,
                Player.class,
                20,
                true,
                false,
                target -> master == null && CustomAttackGoal.isValidTarget(target)
        ));

        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(
                this,
                Villager.class,
                30,
                true,
                false,
                target -> master == null && !(target instanceof GiifuDemosEntity || target instanceof StoriousEntity)
        ));

        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.34D, true));
        this.goalSelector.addGoal(2, new FlyAndAttackGoal(this));
        this.goalSelector.addGoal(3, new AttractGoal(this, HELHEIM_FOOD_TAG.location(), 0.45D));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.45D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new FloatGoal(this));
        this.goalSelector.addGoal(8, new PickUpHelheimFruitGoal(this));
    }

    @Nullable
    public LivingEntity getMaster() {
        return this.master;
    }

    static class OwnerHurtByTargetGoal extends TargetGoal {
        private final ElementaryInvesHelheim inves;
        private LivingEntity attacker;
        private int timestamp;

        public OwnerHurtByTargetGoal(ElementaryInvesHelheim inves) {
            super(inves, false);
            this.inves = inves;
        }

        @Override
        public boolean canUse() {
            if (inves.master == null || inves.loyaltyTime <= 0) {
                return false;
            }
            this.attacker = inves.master.getLastHurtByMob();
            int lastHurtTimestamp = inves.master.getLastHurtByMobTimestamp();
            return lastHurtTimestamp != this.timestamp
                    && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.inves.setTarget(this.attacker);
            this.timestamp = this.inves.master.getLastHurtByMobTimestamp();
            super.start();
        }
    }

    static class OwnerHurtTargetGoal extends TargetGoal {
        private final ElementaryInvesHelheim inves;
        private LivingEntity target;
        private int timestamp;

        public OwnerHurtTargetGoal(ElementaryInvesHelheim inves) {
            super(inves, false);
            this.inves = inves;
        }

        @Override
        public boolean canUse() {
            if (inves.master == null || inves.loyaltyTime <= 0) {
                return false;
            }
            this.target = inves.master.getLastHurtMob();
            int lastHurtTimestamp = inves.master.getLastHurtMobTimestamp();
            return lastHurtTimestamp != this.timestamp
                    && this.canAttack(this.target, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.inves.setTarget(this.target);
            this.timestamp = this.inves.master.getLastHurtMobTimestamp();
            super.start();
        }
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        if (master != null && (type == EntityType.PLAYER || type == ModEntityTypes.LORD_BARON.get())) {
            return false;
        }
        return super.canAttackType(type);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (this.master != null) {
            if (entity == this.master || this.master.isAlliedTo(entity)) {
                return true;
            }

            if (entity instanceof ElementaryInvesHelheim otherInves && otherInves.getMaster() != null) {
                return this.master.equals(otherInves.getMaster());
            }

            if (entity instanceof Player otherPlayer) {
                KRBVariables.PlayerVariables otherVars = otherPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                if (otherVars.isOverlord && !this.master.equals(otherPlayer)) {
                    return false;
                }
            }

            return false;
        }

        if (this.factionLeader != null && entity instanceof FactionLeader) {
            return this.factionLeader.getFactionId().equals(((FactionLeader) entity).getFactionId());
        }

        return super.isAlliedTo(entity);
    }

    static class ProtectMasterGoal extends Goal {
        private final ElementaryInvesHelheim inves;
        private LivingEntity target;
        private int cooldown = 0;

        ProtectMasterGoal(ElementaryInvesHelheim inves) {
            this.inves = inves;
        }

        @Override
        public boolean canUse() {
            if (inves.master == null || inves.loyaltyTime <= 0) return false;
            if (cooldown-- > 0) return false;
            cooldown = 10;

            this.target = inves.master.getLastHurtByMob();
            if (target == null) {
                this.target = inves.master.getLastHurtMob();
            }

            return target != null &&
                    target.isAlive() &&
                    inves.distanceToSqr(target) <= 400.0;
        }

        @Override
        public void start() {
            inves.setTarget(target);
            inves.setFlying(true);
            inves.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, true));
        }
    }

    private static class CustomAttackGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public CustomAttackGoal(Mob mob, Class<T> targetClass, int chance, boolean checkSight, boolean onlyNearby, java.util.function.Predicate<LivingEntity> targetPredicate) {
            super(mob, targetClass, chance, checkSight, onlyNearby, targetPredicate);
        }

        private static boolean isValidTarget(LivingEntity target) {
            if (target instanceof GiifuDemosEntity || target instanceof StoriousEntity) {
                return false;
            }
            return target instanceof Player || target instanceof Villager;
        }
    }

    public static AttributeSupplier createAttributes() {
        return GiifuDemosEntity.createAttributes()
                .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 31.0D)
                .add(Attributes.MAX_HEALTH, 96.D)
                .add(Attributes.ARMOR, 15.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 25.0D)
                .build();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "run", 5, this::idleAnimController));
        controllers.add(new AnimationController<>(this, "Attack", 5, this::attackAnimController));
        controllers.add(new AnimationController<>(this, "fly", 5, this::flyAnimController));
    }

    protected <E extends ElementaryInvesHelheim> PlayState flyAnimController(final AnimationState<E> event) {
        if (this.isFlying()) {
            return event.setAndContinue(FLY);
        }
        return PlayState.STOP;
    }

    protected <E extends ElementaryInvesHelheim> PlayState idleAnimController(final AnimationState<E> event) {
        if (event.isMoving()) {
            return event.setAndContinue(FLY);
        } else {
            return event.setAndContinue(IDLE);
        }
    }

    protected <E extends ElementaryInvesHelheim> PlayState attackAnimController(final AnimationState<E> event) {
        if (this.swinging) {
            if (getRandom().nextInt(100) < 40) {
                return event.setAndContinue(GOMAD);
            } else if (getRandom().nextInt(100) < 70) {
                return event.setAndContinue(ATTACK);
            }
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public Component getDisplayName() {
        if (this.factionLeader != null) {
            String symbol = switch (factionLeader.getFactionId().hashCode() % 4) {
                case 0 -> "♛";
                case 1 -> "♕";
                case 2 -> "⚔";
                default -> "⚜";
            };

            return Component.literal(symbol + " ")
                    .append(super.getDisplayName())
                    .withStyle(ChatFormatting.GOLD);
        }
        return super.getDisplayName();
    }
}