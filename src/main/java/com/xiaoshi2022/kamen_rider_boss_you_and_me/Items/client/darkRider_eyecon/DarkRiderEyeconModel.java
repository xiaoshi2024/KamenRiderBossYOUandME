package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.darkRider_eyecon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class DarkRiderEyeconModel  <T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public DarkRiderEyeconModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }
    @Override
    public DarkRiderEyeconModel<T> withAltModel(ResourceLocation altPath) {
        return (DarkRiderEyeconModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"geo/item/dark_rider_eye.geo.json"));
    }

    /**
     * Changes the constructor-defined animations path for this model to an alternate.<br>
     * This is useful if your animatable shares an animations path with another animatable that differs in path to the model and texture for this model
     */
    @Override
    public DarkRiderEyeconModel<T> withAltAnimations(ResourceLocation altPath) {
        return (DarkRiderEyeconModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"animations/item/dark_rider_eye.animation.json"));
    }

    /**
     * Changes the constructor-defined texture path for this model to an alternate.<br>
     * This is useful if your animatable shares a texture path with another animatable that differs in path to the model and animations for this model
     */
    @Override
    public DarkRiderEyeconModel<T> withAltTexture(ResourceLocation altPath) {
        return (DarkRiderEyeconModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"textures/item/dark_rider_eye.png"));
    }
}