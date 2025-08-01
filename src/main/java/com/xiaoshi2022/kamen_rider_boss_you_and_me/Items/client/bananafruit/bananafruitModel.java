package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.bananafruit;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class bananafruitModel  <T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public bananafruitModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }
    @Override
    public bananafruitModel<T> withAltModel(ResourceLocation altPath) {
        return (bananafruitModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"geo/item/bananafruit.geo.json"));
    }

    /**
     * Changes the constructor-defined animations path for this model to an alternate.<br>
     * This is useful if your animatable shares an animations path with another animatable that differs in path to the model and texture for this model
     */
    @Override
    public bananafruitModel<T> withAltAnimations(ResourceLocation altPath) {
        return (bananafruitModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"animations/item/bananafruit.animation.json"));
    }

    /**
     * Changes the constructor-defined texture path for this model to an alternate.<br>
     * This is useful if your animatable shares a texture path with another animatable that differs in path to the model and animations for this model
     */
    @Override
    public bananafruitModel<T> withAltTexture(ResourceLocation altPath) {
        return (bananafruitModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"textures/item/bananafruit.png"));
    }
}
