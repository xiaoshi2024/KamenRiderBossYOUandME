package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blood;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blood.bloods.BloodArmorRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class Blood extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    public Blood(Holder<ArmorMaterial> armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BloodArmorRenderer renderer;

            @Override
            public <T extends LivingEntity> net.minecraft.client.model.HumanoidModel<?> getGeoArmorRenderer(T livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, net.minecraft.client.model.HumanoidModel<T> original) {
                if (this.renderer == null)
                    this.renderer = new BloodArmorRenderer();

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

    private PlayState predicate(AnimationState event) {
        if (this.animationprocedure.equals("empty")) {
            this.animationprocedure = "idle";
            event.getController().setAnimation(IDLE);
            return PlayState.CONTINUE;
        } else if (this.animationprocedure.equals("idle")) {
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<Blood> event) {
        if (!this.animationprocedure.equals("empty") &&
                !this.animationprocedure.equals("idle")) {
            event.getController().setAnimation(
                    RawAnimation.begin().thenPlay(this.animationprocedure)
            );

            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                this.animationprocedure = "empty";
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 5, this::mainPredicate));
    }

    private <E extends GeoItem> PlayState mainPredicate(AnimationState<E> event) {
        event.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }
}