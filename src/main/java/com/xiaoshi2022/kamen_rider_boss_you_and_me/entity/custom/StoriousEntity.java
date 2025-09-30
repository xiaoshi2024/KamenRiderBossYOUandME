package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class StoriousEntity extends Villager implements GeoEntity, VillagerLike<VillagerEntityMCA>, MenuProvider, CompassionateEntity<BreedableRelationship>, CrossbowAttackMob {
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("attack");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    //mca
    private final VillagerEntityMCA villagerComponent;
    private final VillagerBrain<VillagerEntityMCA> mcaBrain;
    private final VillagerCommandHandler interactions;

    public StoriousEntity(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
        this.villagerComponent = new VillagerEntityMCA((EntityType<VillagerEntityMCA>) entityType, level, Gender.MALE);
        this.mcaBrain = new VillagerBrain<>(this.villagerComponent);
        this.interactions = new VillagerCommandHandler(this.villagerComponent);
        // 设置默认名称为“Storious”
        this.setCustomName(Component.literal("Storious"));
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
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // 只在非和平模式下添加攻击目标
        if (this.level().getDifficulty() != Difficulty.PEACEFUL) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.36D, false) {
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
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new FloatGoal(this));
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
        if (isTargetCreativePlayer()) {
            this.setTarget(null);
            return;
        }

        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            if (this.distanceTo(target) < 2.0D) {
                float damage = (float)this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
                target.hurt(this.damageSources().mobAttack(this), damage);
            }
        }
    }


    // 添加属性创建方法
    public static AttributeSupplier.Builder createAttributes() {
        return Villager.createAttributes()
                .add(ModAttributes.CUSTOM_ATTACK_DAMAGE.get(), 25.0D)  // 攻击力从6.0D提高到25.0D
                .add(Attributes.MAX_HEALTH, 120.0D);
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
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof Player && ((Player)target).isCreative())) {
            float damage = (float)this.getAttributeValue(ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
            return target.hurt(this.damageSources().mobAttack(this), damage);
        }
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "run", 5, this::idleAnimController));
        controllers.add(new AnimationController<>(this, "attack", 5, this::attackAnimController));
    }

    protected <E extends StoriousEntity> PlayState idleAnimController(final AnimationState<E> event) {
        if (event.isMoving())
            return event.setAndContinue(RUN);
        else
            return event.setAndContinue(IDLE); // 当不移动时播放 idle 动画
    }

    protected <E extends StoriousEntity> PlayState attackAnimController(final AnimationState<E> event) {
        if (this.swinging) {
            return event.setAndContinue(ATTACK);
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
        return this.villagerComponent.createMenu(windowId, playerInventory, player);
    }

    public void setTradeOffers(MerchantOffers offers) {
        this.villagerComponent.setOffers(offers); // 使用 VillagerEntityMCA 的交易逻辑
    }
}