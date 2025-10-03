package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.Tyrant.TyrantArmorRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.init.ArmorAnimationFactory;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class TyrantItem extends ArmorItem implements GeoItem , KamenBossArmor , ArmorAnimationFactory.AnimatableAccessor {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	public String animationprocedure = "empty";

	public TyrantItem(Type type, Properties properties) {
		super(new ArmorMaterial() {
			@Override
			public int getDurabilityForType(Type type) {
				return new int[]{13, 15, 16, 11}[type.getSlot().getIndex()] * 25;
			}

			// 防御：提升至38
			@Override
			public int getDefenseForType(Type type) {
				return new int[]{9, 9, 7, 3}[type.getSlot().getIndex()];
			}
			// 盔甲韧性：添加韧性值4.5
			@Override
			public float getToughness() {
				return 4.5f;
			}
			// 击退抗性：增加15%
			@Override
			public float getKnockbackResistance() {
				return 0.15f;
			}

			@Override
			public int getEnchantmentValue() {
				return 9;
			}

			@Override
			public SoundEvent getEquipSound() {
				return SoundEvents.EMPTY;
			}

			@Override
			public Ingredient getRepairIngredient() {
				return Ingredient.of();
			}

			@Override
			public String getName() {
				return "tyrant";
			}

		}, type, properties);
		// 注册为同步可动画对象，确保多人游戏中材质变化能正确同步
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private GeoArmorRenderer<?> renderer;

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
				if (this.renderer == null)
					this.renderer = new TyrantArmorRenderer();
				this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
				return this.renderer;
			}
		});
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
	}


	private PlayState predicate(AnimationState event) {
		if (this.animationprocedure.equals("empty")) {
			event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
			Entity entity = (Entity) event.getData(DataTickets.ENTITY);
			if (entity instanceof ArmorStand) {
				return PlayState.CONTINUE;
			}
			return PlayState.CONTINUE;
		}
		return PlayState.STOP;
	}

	private PlayState procedurePredicate(AnimationState event) {
		if (!this.animationprocedure.equals("empty") && event.getController().getAnimationState() == AnimationController.State.STOPPED) {
			event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
			if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
				this.animationprocedure = "empty";
				event.getController().forceAnimationReset();
			}
			Entity entity = (Entity) event.getData(DataTickets.ENTITY);
			if (entity instanceof ArmorStand) {
				return PlayState.CONTINUE;
			}
			return PlayState.CONTINUE;
		}
		return PlayState.STOP;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar data) {
		data.add(new AnimationController(this, "controller", 5, this::predicate));
		data.add(new AnimationController(this, "procedureController", 5, this::procedurePredicate));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	@Override
	public void setAnimationProcedure(String procedure) {
		this.animationprocedure = procedure;
	}

	@Override
	public void tick(Player player) {
		// 添加抗性效果
		this.applyResistanceEffect(player);

		// 添加力量效果
		this.applyStrengthEffect(player);
	}

	// 不再提供力量效果，避免与原版药水冲突
	@Override
	public int getStrengthLevel() {
		return 3; // 不使用力量效果
	}

	// 覆写getResistanceLevel方法，设置自定义抗性等级
	@Override
	public int getResistanceLevel() {
		return 3; //级别
	}

	// 重写applyResistanceEffect方法，确保不会移除玩家已有的抗性效果
	@Override
	public void applyResistanceEffect(Player player) {
		if (!player.level().isClientSide()) {
			int resistanceLevel = this.getResistanceLevel();
			if (resistanceLevel > 0) {
				int targetAmp = resistanceLevel - 1;
				MobEffectInstance existing = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
				
				// 只有在玩家没有抗性效果，或者现有效果等级低于我们提供的等级时，才添加新效果
				if (existing == null || existing.getAmplifier() < targetAmp) {
					player.addEffect(new MobEffectInstance(
							MobEffects.DAMAGE_RESISTANCE,
							400,
							targetAmp,
							false,
							false // 不显示粒子效果，避免视觉混乱
					));
				}
			}
		}
	}
}