package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.DarkRiderGhost;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.DarkRiderGhost.DarkRiderGhosts.DarkRiderGhostArmorRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class DarkRiderGhost extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    public DarkRiderGhost(Holder<ArmorMaterial> armorMaterial, Type type, Properties properties) {
        super(armorMaterial, type, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private final DarkRiderGhostArmorRenderer renderer = new DarkRiderGhostArmorRenderer();

            @Override
            public <T extends net.minecraft.world.entity.LivingEntity> net.minecraft.client.model.HumanoidModel<?> getGeoArmorRenderer(T livingEntity, ItemStack itemStack, net.minecraft.world.entity.EquipmentSlot equipmentSlot, net.minecraft.client.model.HumanoidModel<T> original) {
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            if (this.animationprocedure.equals("empty")) {
                this.animationprocedure = "idle";
                state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
                return PlayState.CONTINUE;
            } else if (this.animationprocedure.equals("idle")) {
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));
        controllers.add(new AnimationController<>(this, "procedureController", 5, state -> {
            if (!this.animationprocedure.equals("empty")) {
                state.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
                this.animationprocedure = "empty";
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void setAnimationProcedure(String procedure) {
        this.animationprocedure = procedure;
    }
}