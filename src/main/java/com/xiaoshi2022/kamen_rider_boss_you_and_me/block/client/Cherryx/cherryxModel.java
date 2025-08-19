package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Cherryx;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.cherryxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class cherryxModel extends GeoModel<cherryxEntity> {
    @Override
    public ResourceLocation getModelResource(cherryxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/block/cherryx.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(cherryxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/block/cherryx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(cherryxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/block/cherryx.animation.json");
    }
}
