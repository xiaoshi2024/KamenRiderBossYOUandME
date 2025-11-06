package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.napoleon_eyecon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class NapoleonEyeconModel  <T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public NapoleonEyeconModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }
    @Override
    public NapoleonEyeconModel<T> withAltModel(ResourceLocation altPath) {
        return (NapoleonEyeconModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"geo/item/nb_eye.geo.json"));
    }

    @Override
    public NapoleonEyeconModel<T> withAltAnimations(ResourceLocation altPath) {
        return (NapoleonEyeconModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"animations/item/nb_eye.animation.json"));
    }

    @Override
    public NapoleonEyeconModel<T> withAltTexture(ResourceLocation altPath) {
        return (NapoleonEyeconModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"textures/item/nb_eye.png"));
    }
}