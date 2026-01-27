package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
// 移除直接的MCA类导入，改为使用软依赖方式
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GiifuDemosEntity extends Villager implements GeoEntity, MenuProvider, CrossbowAttackMob {
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation SWEEP = RawAnimation.begin().thenLoop("sweep");
    protected static final RawAnimation MUTATION = RawAnimation.begin().thenPlayAndHold("mutation");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // 德莫斯相关
    private boolean hasPlayedMutation = false; // 标志字段

    // 存储原始村民数据的NBT标签键
    public static final String ORIGINAL_VILLAGER_TAG = "OriginalVillagerData";
    
    public GiifuDemosEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
        // 设置默认名称为“基夫德莫斯”
        this.setCustomName(Component.literal("基夫德莫斯"));
        this.setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1)); // 设置村民数据
    }

    // 在类中添加属性构建方法
    public static AttributeSupplier.Builder createAttributes() {
        return Villager.createAttributes()
                .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 20.0D)  // 攻击力从5.0D提高到20.0D
                .add(Attributes.MAX_HEALTH, 100.0D);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // 只在非和平模式下添加攻击目标
        if (this.level().getDifficulty() != Difficulty.PEACEFUL) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.34D, false) {
                @Override
                public boolean canUse() {
                    return super.canUse() && !isTargetCreativePlayer();
                }

                @Override
                public boolean canContinueToUse() {
                    return super.canContinueToUse() && !isTargetCreativePlayer();
                }
            });
        }
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2)); // 当受到火焰伤害时逃跑
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.45D)); // 随机漫步
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this)); // 随机环顾四周
        this.goalSelector.addGoal(7, new FloatGoal(this)); // 如果在水里，尝试浮在水面上
    }

    private boolean isTargetCreativePlayer() {
        LivingEntity target = this.getTarget();
        return target instanceof Player && ((Player)target).isCreative();
    }


    @Override
    public boolean canAttackType(EntityType<?> type) {
        return true; // 允许攻击所有实体类型
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // 只在非和平模式下执行攻击逻辑
        if (this.level().getDifficulty() != Difficulty.PEACEFUL &&
                !(this.getTarget() instanceof Player && ((Player)this.getTarget()).isCreative())) {
            LivingEntity target = this.getTarget();
            if (target != null && this.distanceTo(target) < 2.0D) {
                float damage = (float)this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
                target.hurt(this.damageSources().mobAttack(this), damage);
            }
        }
    }

    // 替换环境音效
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.WITHER_SKELETON_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean hurt(DamageSource source, float damageAmount) {
        // 在和平模式下不设置攻击目标
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && source.getDirectEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getDirectEntity();
            // 不把创造模式玩家设为目标
            if (!(attacker instanceof Player && ((Player)attacker).isCreative())) {
                this.setTarget(attacker);
            }
        }
        return super.hurt(source, damageAmount);
    }


    @Override
    public Component getDisplayName() {
        return this.getCustomName() != null ? this.getCustomName() : Component.literal("基夫德莫斯");
    }

    @Override
    public void handleEntityEvent(byte p_35391_) {
        super.handleEntityEvent(p_35391_);
    }

    @Override
    public void setCustomName(@Nullable Component p_20053_) {
        super.setCustomName(p_20053_);
    }

    @Override
    public void setAge(int p_146763_) {
        super.setAge(p_146763_);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float)this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
        return target.hurt(this.damageSources().mobAttack(this), damage);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk", 5, this::idleAnimController));
        controllers.add(new AnimationController<>(this, "sweep", 5, this::attackAnimController));
        controllers.add(new AnimationController<>(this, "mutation", 5, this::mutationAnimController));
    }

    protected <E extends GiifuDemosEntity> PlayState mutationAnimController(AnimationState<E> event) {
        if (!this.hasPlayedMutation) {
            event.getController().setAnimation(MUTATION);
            MUTATION.thenPlay("mutation");
            this.hasPlayedMutation = true; // 设置标志为 true
        }
        return PlayState.CONTINUE;
    }

    protected <E extends GiifuDemosEntity> PlayState idleAnimController(final AnimationState<E> event) {
        if (event.isMoving())
            return event.setAndContinue(WALK);
        else
            return event.setAndContinue(IDLE); // 当不移动时播放 idle 动画
    }

    protected <E extends GiifuDemosEntity> PlayState attackAnimController(AnimationState<E> event) {
        if (this.swinging && !this.level().isClientSide()) { // 仅服务端执行
            this.hurtNearbyEntities(); // 自定义方法处理伤害
        }
        return this.swinging ? event.setAndContinue(SWEEP) : PlayState.STOP;
    }

    public void hurtNearbyEntities() {
        if (this.level().isClientSide()) return;

        // 获取攻击伤害属性，如果不存在则使用默认值
        double damage = this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());

        // 扫描周围实体
        for (LivingEntity entity : this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(2.0D),
                e -> e != this && !(e instanceof Player && ((Player)e).isCreative()))
        ) {
            entity.hurt(this.damageSources().mobAttack(this), (float)damage);
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public void performCrossbowAttack(LivingEntity target, float pullProgress) {
        // 如果需要弩攻击逻辑，可以在这里实现
    }

    @Override
    public void setChargingCrossbow(boolean charging) {
        // 如果需要弩攻击逻辑，可以在这里实现
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity p_32328_, ItemStack p_32329_, Projectile p_32330_, float p_32331_) {

    }

    @Override
    public void onCrossbowAttackPerformed() {

    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        // 如果需要远程攻击逻辑，可以在这里实现
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return null;
    }

    public void setTradeOffers(MerchantOffers offers) {
        // 移除MCA相关的交易逻辑
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