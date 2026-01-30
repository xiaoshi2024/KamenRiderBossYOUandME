package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.queenbeestamp;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.QueenBeeStamp;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class QueenBeeStampModel extends GeoModel<QueenBeeStamp> {
    @Override
    public ResourceLocation getModelResource(QueenBeeStamp object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/quinbee_stamp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(QueenBeeStamp object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/quinbee_stamp.png");
    }

    @Override
    public ResourceLocation getAnimationResource(QueenBeeStamp animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/quinbee_stamp.animation.json");
    }
}
