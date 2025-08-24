package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.dragonfruit;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class dragonfruitModel  <T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public dragonfruitModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }
    @Override
    public dragonfruitModel<T> withAltModel(ResourceLocation altPath) {
        return (dragonfruitModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"geo/item/dragonfruit.geo.json"));
    }

    /**
     * Changes the constructor-defined animations path for this model to an alternate.<br>
     * This is useful if your animatable shares an animations path with another animatable that differs in path to the model and texture for this model
     */
    @Override
    public dragonfruitModel<T> withAltAnimations(ResourceLocation altPath) {
        return (dragonfruitModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"animations/item/dragonfruit.animation.json"));
    }

    /**
     * Changes the constructor-defined texture path for this model to an alternate.<br>
     * This is useful if your animatable shares a texture path with another animatable that differs in path to the model and animations for this model
     */
    @Override
    public dragonfruitModel<T> withAltTexture(ResourceLocation altPath) {
        return (dragonfruitModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"textures/item/dragonfruit.png"));
    }
}
