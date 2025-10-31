package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Another_zi_o_click;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class aiden_owcModel <T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public aiden_owcModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }

    @Override
    public aiden_owcModel<T> withAltModel(ResourceLocation altPath) {
        return (aiden_owcModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"geo/item/aiden_owc.geo.json"));
    }

    @Override
    public aiden_owcModel<T> withAltAnimations(ResourceLocation altPath) {
        return (aiden_owcModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"animations/item/aiden_owc.animation.json"));
    }

    @Override
    public aiden_owcModel<T> withAltTexture(ResourceLocation altPath) {
        return (aiden_owcModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"textures/item/aiden_owc.png"));
    }
}
