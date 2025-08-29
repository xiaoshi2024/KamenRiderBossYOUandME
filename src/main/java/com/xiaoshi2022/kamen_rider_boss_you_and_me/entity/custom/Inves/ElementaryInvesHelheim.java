package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.AttractGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.FlyAndAttackGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals.PickUpHelheimFruitGoal;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.helheimtems.FactionLeader;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class ElementaryInvesHelheim extends Monster implements GeoEntity {
    protected static final RawAnimation GOMAD = RawAnimation.begin().thenPlay("gomad");
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("Attack");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly"); // 添加飞行动画
    public static final TagKey<Item> HELHEIM_FOOD_TAG =
            ItemTags.create(new ResourceLocation("kamen_rider_weapon_craft", "kamen_rider_helheim_food"));
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private String originalFactionId; // 新增字段保存原始阵营ID

    private FactionLeader factionLeader;

    private static final String[] VARIANTS = {"red", "blue", "green"};
    private boolean isFlying = false; // 添加飞行状态
    public int flyingCooldown = 0; // 飞行冷却
    // 添加飞行状态getter/setter
    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    private String variant = "blue"; // 默认变种

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
    private int loyaltyTime = 0; // 忠诚度计时器

    public void setMaster(LivingEntity master) {
        // 如果是第一次被控制，保存原始阵营ID
        if (this.master == null && this.factionLeader != null) {
            this.originalFactionId = this.factionLeader.getFactionId(); // 直接保存String
        }

        this.master = master;
        if (master instanceof FactionLeader) {
            this.factionLeader = (FactionLeader) master;
        }
        this.loyaltyTime = 1200;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        // 1. 优先检查主人关系
        if (this.master != null) {
            // 不攻击主人及主人的盟友
            if (target == this.master || this.master.isAlliedTo(target)) {
                return false;
            }
            // 攻击主人的敌人
            return true;
        }

        // 2. 检查阵营关系
        String myFaction = this.factionLeader != null ? this.factionLeader.getFactionId() : null;
        String targetFaction = target instanceof FactionLeader ?
                ((FactionLeader) target).getFactionId() : null;

        // 双方都有阵营且阵营不同时可以攻击
        if (myFaction != null && targetFaction != null) {
            return !myFaction.equals(targetFaction);
        }

        // 3. 默认行为
        return super.canAttack(target);
    }


    public ElementaryInvesHelheim(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        // 随机选择一个变种
        this.setVariant(VARIANTS[random.nextInt(VARIANTS.length)]);
        // 初始化 factionLeader 为 null 或默认值
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

        // 读取原始阵营ID
        if (tag.contains("OriginalFactionId", Tag.TAG_STRING)) {
            this.originalFactionId = tag.getString("OriginalFactionId");
        }

        // 读取当前阵营
        if (tag.contains("CurrentFactionId", Tag.TAG_STRING)) {
            String factionId = tag.getString("CurrentFactionId");
            this.factionLeader = findFactionLeaderById(factionId);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // 保存原始阵营ID
        if (this.originalFactionId != null) {
            tag.putString("OriginalFactionId", this.originalFactionId.toString());
        }

        // 保存当前阵营
        if (this.factionLeader != null) {
            tag.putString("CurrentFactionId", this.factionLeader.getFactionId());
        }
    }

    @Override
    public final boolean hurt(DamageSource source, float damageAmount) { // 重写 hurt 方法
        // 调用父类的 hurt 方法
        return super.hurt(source, damageAmount);
    }

    @Override
    public void tick() {
        super.tick();

        // 如果主人已死亡，立即消失
        if (getMaster() != null && !getMaster().isAlive()) {
            this.discard();
            return;
        }

        // 飞行冷却处理
        if (flyingCooldown > 0) {
            flyingCooldown--;
        }

        // 失去目标时降落
        if (isFlying && this.getTarget() == null) {
            this.setFlying(false);
            flyingCooldown = 100;
        }

        // 星爵巴隆控制逻辑
        if (!this.level().isClientSide && this.tickCount % 20 == 0) {
            updateMasterControl();
        }
    }

    private void updateMasterControl() {
        if (master != null) {
            // 减少忠诚时间
            if (loyaltyTime > 0) loyaltyTime--;

            // 检查主人是否有效
            if (!master.isAlive() || loyaltyTime <= 0) {
                releaseControl();
                return;
            }

            // 优先攻击主人正在攻击的目标
            LivingEntity masterTarget = master.getLastHurtMob();
            if (masterTarget != null && masterTarget != this.getTarget()) {
                this.setTarget(masterTarget);
                this.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST, 200, 1));
            }

            // 优先攻击主人的攻击者
            LivingEntity masterAttacker = master.getLastHurtByMob();
            if (masterAttacker != null && masterAttacker != this.getTarget()) {
                this.setTarget(masterAttacker);
                this.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST, 200, 1));
            }

            // 如果离主人太远，回到主人身边
            if (this.distanceToSqr(master) > PROTECT_RANGE * PROTECT_RANGE) {
                this.getNavigation().moveTo(master, 1.2);
            }

            // 如果主人是星爵巴隆且正在被攻击，起飞保护
            if (master instanceof LordBaronEntity &&
                    master.getLastHurtByMob() != null &&
                    !this.isFlying()) {
                this.setFlying(true);
            }
        }
    }

    // 脱离主人时恢复原阵营
    private void releaseControl() {
        if (this.originalFactionId != null) {
            this.factionLeader = findFactionLeaderById(this.originalFactionId.toString());
        }
        this.master = null;
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

        // 添加掉落逻辑
        if (!this.level().isClientSide) {
            // 获取掉落位置
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            // 创建掉落物品
            ItemStack invesMeat = new ItemStack(ModItems.INVES_MEAT.get());
            this.spawnAtLocation(invesMeat, 0.0F);
        }
    }

    @Override
    protected void registerGoals() {
        // ===== 目标选择器 (targetSelector) =====
        // 优先级0 - 保护主人（最高优先级）
        this.targetSelector.addGoal(0, new ProtectMasterGoal(this));

        // 新增优先级1 - 攻击主人攻击的目标
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));

        // 优先级3 - 被攻击时反击（仅当无主人时生效）
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

        // 优先级2 - 攻击玩家（仅当无主人时生效）
        this.targetSelector.addGoal(2, new CustomAttackGoal<>(
                this,
                Player.class,
                20,
                true,
                false,
                target -> master == null && CustomAttackGoal.isValidTarget(target)
        ));

        // 优先级3 - 攻击村民（仅当无主人时生效）
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this,
                Villager.class,
                30,
                true,
                false,
                target -> master == null && !(target instanceof GiifuDemosEntity || target instanceof StoriousEntity)
        ));

        // ===== 行为选择器 (goalSelector) =====
        // 优先级1 - 近战攻击
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.34D, true));

        // 优先级2 - 飞行攻击
        this.goalSelector.addGoal(2, new FlyAndAttackGoal(this));

        // 优先级3 - 被食物吸引
        this.goalSelector.addGoal(3, new AttractGoal(
                this,
                HELHEIM_FOOD_TAG.location(),
                0.45D
        ));

        // 优先级4 - 随机漫步
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.45D));

        // 优先级5 - 看向玩家
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // 优先级6 - 随机环顾
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // 优先级7 - 水中漂浮
        this.goalSelector.addGoal(7, new FloatGoal(this));

        // 优先级 这里放 8
        this.goalSelector.addGoal(8, new PickUpHelheimFruitGoal(this));
    }

    // 在ElementaryInvesHelheim类中
    @Nullable
    public LivingEntity getMaster() {
        return this.master;
    }

    // 当主人被攻击时，攻击攻击者
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

    // 当主人攻击目标时，也攻击该目标
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
        // 被控制时不攻击玩家
        if (master != null && (type == EntityType.PLAYER || type == ModEntityTypes.LORD_BARON.get())) {
            return false;
        }
        return super.canAttackType(type);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        // 优先检查主人关系
        if (this.master != null) {
            return entity == this.master || this.master.isAlliedTo(entity);
        }

        // 其次检查阵营关系
        if (this.factionLeader != null && entity instanceof FactionLeader) {
            return this.factionLeader.getFactionId().equals(((FactionLeader) entity).getFactionId());
        }

        return super.isAlliedTo(entity);
    }

    // 新增保护主人的目标
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

            // 每10ticks检查一次（减少性能消耗）
            if (cooldown-- > 0) return false;
            cooldown = 10;

            // 获取主人正在攻击的目标 或 攻击主人的敌人
            this.target = inves.master.getLastHurtByMob();
            if (target == null) {
                this.target = inves.master.getLastHurtMob();
            }

            return target != null &&
                    target.isAlive() &&
                    inves.distanceToSqr(target) <= 400.0; // 20格范围内
        }

        @Override
        public void start() {
            inves.setTarget(target);
            inves.setFlying(true);
            inves.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    200, 1, false, true
            ));
        }
    }

    // 自定义攻击目标逻辑
    private static class CustomAttackGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public CustomAttackGoal(Mob mob, Class<T> targetClass, int chance, boolean checkSight, boolean onlyNearby, java.util.function.Predicate<LivingEntity> targetPredicate) {
            super(mob, targetClass, chance, checkSight, onlyNearby, targetPredicate);
        }

        // 静态方法，用于目标验证
        private static boolean isValidTarget(LivingEntity target) {
            // 如果目标是 GiifuDemosEntity 或 StoriousEntity，则不攻击
            if (target instanceof GiifuDemosEntity || target instanceof StoriousEntity) {
                return false;
            }

            // 如果目标是普通村民或玩家，则攻击
            return target instanceof Player || target instanceof Villager;
        }
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 保持原有的两个控制器
        controllers.add(new AnimationController<>(this, "run", 5, this::idleAnimController));
        controllers.add(new AnimationController<>(this, "Attack", 5, this::attackAnimController));
        controllers.add(new AnimationController<>(this, "fly", 5, this::flyAnimController)); // 新增飞行控制器
    }

    protected <E extends ElementaryInvesHelheim> PlayState flyAnimController(final AnimationState<E> event) {
        if (this.isFlying()) {
            return event.setAndContinue(FLY); // 飞行时播放 fly 动画
        }
        return PlayState.STOP; // 不飞行时停止
    }

    protected <E extends ElementaryInvesHelheim> PlayState idleAnimController(final AnimationState<E> event) {
        if (event.isMoving()) {
            return event.setAndContinue(FLY);
        } else {
            return event.setAndContinue(IDLE);
        }
    }

    protected <E extends ElementaryInvesHelheim> PlayState attackAnimController(final AnimationState<E> event) {
        // 攻击动画逻辑保持不变
        if (this.swinging) {
            if (getRandom().nextInt(100) < 40) { // 40% 概率触发凶暴
                return event.setAndContinue(GOMAD);
            } else if (getRandom().nextInt(100) < 70) { // 70% 概率触发普通攻击
                return event.setAndContinue(ATTACK);
            }
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // ===== 阵营标记渲染 =====
    @Override
    public Component getDisplayName() {
        if (this.factionLeader != null) {
            // 生成阵营符号
            String symbol = switch (factionLeader.getFactionId().hashCode() % 4) {
                case 0 -> "♛";
                case 1 -> "♕";
                case 2 -> "⚔";
                default -> "⚜";
            };

            // 返回带符号的名称
            return Component.literal(symbol + " ")
                    .append(super.getDisplayName())
                    .withStyle(ChatFormatting.GOLD);
        }
        return super.getDisplayName();
    }
}