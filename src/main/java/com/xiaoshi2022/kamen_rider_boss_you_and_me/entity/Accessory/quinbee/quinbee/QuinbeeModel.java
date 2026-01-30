package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.quinbee.quinbee;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.quinbee.QuinbeeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class QuinbeeModel extends GeoModel<QuinbeeItem> {
    @Override
    public ResourceLocation getModelResource(QuinbeeItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/quinbee.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(QuinbeeItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/quinbee.png");
    }

    @Override
    public ResourceLocation getAnimationResource(QuinbeeItem animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/quinbee.animation.json");
    }
}