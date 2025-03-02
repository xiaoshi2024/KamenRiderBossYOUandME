package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.BreedableRelationship;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.entity.ai.Traits;
import forge.net.mca.entity.ai.brain.VillagerBrain;
import forge.net.mca.entity.ai.relationship.CompassionateEntity;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.entity.interaction.VillagerCommandHandler;
import forge.net.mca.util.network.datasync.CDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.npc.*;
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
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GiifuDemosEntity extends Villager implements GeoEntity, VillagerLike<VillagerEntityMCA>, MenuProvider, CompassionateEntity<BreedableRelationship>, CrossbowAttackMob {
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation SWEEP = RawAnimation.begin().thenLoop("sweep");
    protected static final RawAnimation MUTATION = RawAnimation.begin().thenPlayAndHold("mutation");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    //mca
    private final VillagerEntityMCA villagerComponent;
    private final VillagerBrain<VillagerEntityMCA> mcaBrain;
    private final VillagerCommandHandler interactions;


    // 德莫斯相关
    private boolean hasPlayedMutation = false; // 标志字段


    public GiifuDemosEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
        this.villagerComponent = new VillagerEntityMCA((EntityType<VillagerEntityMCA>) entityType, level, Gender.MALE);
        this.mcaBrain = new VillagerBrain<>(this.villagerComponent);
        this.interactions = new VillagerCommandHandler(this.villagerComponent);
        // 设置默认名称为“基夫德莫斯”
        this.setCustomName(Component.literal("基夫德莫斯"));
        this.setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1)); // 设置村民数据
    }

    // 使用 VillagerEntityMCA 的功能
    public VillagerCommandHandler getInteractions() {
        return villagerComponent.getInteractions();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F)); // 看向玩家
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false)); // 近战攻击目标
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2)); // 当受到火焰伤害时逃跑
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.45D)); // 随机漫步
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this)); // 随机环顾四周
        this.goalSelector.addGoal(7, new FloatGoal(this)); // 如果在水里，尝试浮在水面上
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        return true; // 允许攻击所有实体类型
    }

    @Override
    public void aiStep() {
        super.aiStep();
        LivingEntity target = this.getTarget();
        if (target != null && this.distanceTo(target) < 2.0D) {
            // 执行攻击逻辑
            target.hurt(asEntity().damageSources().generic(), 5.0F); // 示例：对目标造成 5 点伤害
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
    public final boolean hurt(DamageSource source, float damageAmount) {
        if (source.getDirectEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getDirectEntity();
            // 设置攻击目标
            this.setTarget(attacker);
        }
        // 调用父类的 hurt 方法
        return super.hurt(source, damageAmount);
    }

    @Override
    public Component getDisplayName() {
        return this.villagerComponent.getDisplayName(); // 使用 VillagerEntityMCA 的显示名称
    }

    @Override
    public void handleEntityEvent(byte p_35391_) {
        this.villagerComponent.handleEntityEvent(p_35391_); // 使用 VillagerEntityMCA 的实体事件处理
    }


    @Override
    public void setCustomName(@Nullable Component p_20053_) {
        this.villagerComponent.setCustomName(p_20053_); // 使用 VillagerEntityMCA 的自定义名称
    }

    @Override
    public void setAge(int p_146763_) {
        this.villagerComponent.setAge(p_146763_); // 使用 VillagerEntityMCA 的年龄设置
    }

    @Override
    public boolean doHurtTarget(Entity p_21372_) {
        return this.villagerComponent.doHurtTarget(p_21372_); // 使用 VillagerEntityMCA 的伤害目标
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

    protected <E extends GiifuDemosEntity> PlayState attackAnimController(final AnimationState<E> event) {
        if (this.swinging) {
            return event.setAndContinue(SWEEP);
        }
        return PlayState.STOP;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public Genetics getGenetics() {
        return this.villagerComponent.getGenetics();
    }

    @Override
    public Traits getTraits() {
        return this.villagerComponent.getTraits();
    }

    @Override
    public VillagerBrain<VillagerEntityMCA> getVillagerBrain() {
        return this.mcaBrain;
    }

    @Override
    public boolean isBurned() {
        return false;
    }

    @Override
    public float getInfectionProgress() {
        return 0; // 如果不需要感染逻辑，可以返回 0
    }

    @Override
    public void setInfectionProgress(float progress) {
        // 如果需要感染逻辑，可以在这里添加实现
    }

    @Override
    public BreedableRelationship getRelationships() {
        return null; // 如果不需要繁殖关系逻辑，可以返回 null
    }

    @Override
    public CDataManager<VillagerEntityMCA> getTypeDataManager() {
        return this.villagerComponent.getTypeDataManager();
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
        return this.villagerComponent.createMenu(windowId, playerInventory,player);
    }

    public void setTradeOffers(MerchantOffers offers) {
        this.villagerComponent.setOffers(offers); // 使用 VillagerEntityMCA 的交易逻辑
    }
}