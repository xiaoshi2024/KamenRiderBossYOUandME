package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.blackbuild;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.blackbuild.builds.BlackBuildArmorRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.init.ArmorAnimationFactory;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class BlackBuild extends ArmorItem implements GeoItem, KamenBossArmor, ArmorAnimationFactory.AnimatableAccessor {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public BlackBuild(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{528, 600, 640, 448}[type.getSlot().getIndex()];
            }

            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{6, 10, 9, 8}[type.getSlot().getIndex()];
            }

            @Override
            public int getEnchantmentValue() {
                return 20;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_NETHERITE;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.DIAMOND, Items.GOLD_INGOT);
            }

            @Override
            public String getName() {
                return "netherite";
            }

            @Override
            public float getToughness() {
                return 4.0f;
            }

            @Override
            public float getKnockbackResistance() {
                return 0.25f;
            }
        }, type, properties);
    }

    @Override
    public void tick(Player player) {
        this.applyResistanceEffect(player);
        this.applyStrengthEffect(player);
        this.applySpeedEffect(player);
        this.applyNightVisionEffect(player);
        this.applyRegenerationEffect(player);
    }

    @Override
    public int getResistanceLevel() {
        return 2;
    }
    
    @Override
    public int getStrengthLevel() {
        return 2;
    }
    
    private void applySpeedEffect(Player player) {
        if (!player.level().isClientSide()) {
            MobEffectInstance existing = player.getEffect(MobEffects.MOVEMENT_SPEED);
            if (existing == null || existing.getAmplifier() < 1) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        400,
                        1,
                        false,
                        false
                ));
            }
        }
    }
    
    private void applyNightVisionEffect(Player player) {
        if (!player.level().isClientSide()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    800,
                    0,
                    false,
                    false
            ));
        }
    }
    
    private void applyRegenerationEffect(Player player) {
        if (!player.level().isClientSide()) {
            if (player.getHealth() < player.getMaxHealth() * 0.7) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        200,
                        1,
                        false,
                        false
                ));
            }
        }
    }

    @Override
    public void applyResistanceEffect(Player player) {
        if (!player.level().isClientSide()) {
            int resistanceLevel = this.getResistanceLevel();
            if (resistanceLevel > 0) {
                int targetAmp = resistanceLevel - 1;
                MobEffectInstance existing = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
                if (existing == null || existing.getAmplifier() < targetAmp) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            400,
                            targetAmp,
                            false,
                            false
                    ));
                }
            }
        }
    }

    public static boolean isArmorEquipped(ServerPlayer player, Item armorItem) {
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (armorStack.getItem() == armorItem) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFullArmorEquipped(ServerPlayer player) {
        return player.getInventory().armor.get(3).getItem() instanceof BlackBuild &&
               player.getInventory().armor.get(2).getItem() instanceof BlackBuild &&
               player.getInventory().armor.get(1).getItem() instanceof BlackBuild;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new BlackBuildArmorRenderer();
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    public void triggeridle(Player player, String animationName) {
        if (player.level().isClientSide) {
            AnimatableManager<?> manager = this.getAnimatableInstanceCache().getManagerForId(player.getId());
            if (manager != null) {
                manager.tryTriggerAnimation("controller", animationName);
            }
        }
    }

    private <E extends GeoItem> PlayState mainPredicate(AnimationState<E> event) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "main_controller", 5, this::mainPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }
}