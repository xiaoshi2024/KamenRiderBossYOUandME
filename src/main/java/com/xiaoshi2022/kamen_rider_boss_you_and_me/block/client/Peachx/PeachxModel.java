package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Peachx;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.PeachxEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class PeachxModel extends GeoModel<PeachxEntity> {
    @Override
    public ResourceLocation getModelResource(PeachxEntity animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/block/peachx.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PeachxEntity animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/block/peachx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PeachxEntity animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/block/peachx.animation.json");
    }
}