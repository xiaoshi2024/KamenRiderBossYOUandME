package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.gamma_eyecon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class GammaEyeconModel <T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    /**
     * Create a new instance of this model class.<br>
     * The asset path should be the truncated relative path from the base folder.<br>
     * E.G.
     * <pre>{@code
     * 	new ResourceLocation("myMod", "armor/obsidian")
     * }</pre>
     */
    public GammaEyeconModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }

    /**
     * Changes the constructor-defined model path for this model to an alternate.<br>
     * This is useful if your animatable shares a model path with another animatable that differs in path to the texture and animations for this model
     */
    @Override
    public GammaEyeconModel<T> withAltModel(ResourceLocation altPath) {
        return (GammaEyeconModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"geo/item/gamma_eyecon.geo.json"));
    }

    /**
     * Changes the constructor-defined animations path for this model to an alternate.<br>
     * This is useful if your animatable shares an animations path with another animatable that differs in path to the model and texture for this model
     */
    @Override
    public GammaEyeconModel<T> withAltAnimations(ResourceLocation altPath) {
        return (GammaEyeconModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"animations/item/gamma_eyecon.animation.json"));
    }

    /**
     * Changes the constructor-defined texture path for this model to an alternate.<br>
     * This is useful if your animatable shares a texture path with another animatable that differs in path to the model and animations for this model
     */
    @Override
    public GammaEyeconModel<T> withAltTexture(ResourceLocation altPath) {
        return (GammaEyeconModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"textures/item/gamma_eyecon.png"));
    }
}
