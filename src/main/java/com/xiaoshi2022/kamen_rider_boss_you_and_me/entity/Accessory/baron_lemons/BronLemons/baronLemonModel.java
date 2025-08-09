package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.BronLemons;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class baronLemonModel extends GeoModel<baron_lemonItem> {
    @Override
    public ResourceLocation getAnimationResource(baron_lemonItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/baron_lemon.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(baron_lemonItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/baron_lemon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(baron_lemonItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/baron_lemon.png");
    }
}