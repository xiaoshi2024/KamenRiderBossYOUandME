package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Lemonx;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.LemonxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LemoxModel extends GeoModel<LemonxEntity> {
    @Override
    public ResourceLocation getModelResource(LemonxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/block/lemonx.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LemonxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/block/lemonx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LemonxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/block/lemonx.animation.json");
    }
}