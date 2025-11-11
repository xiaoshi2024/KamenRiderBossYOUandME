package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.renderer.misc;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.misc.DimensionalBarrier;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DimensionalBarrierModel extends GeoModel<DimensionalBarrier> {
    @Override
    public ResourceLocation getModelResource(DimensionalBarrier animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/entity/dimensional_br.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DimensionalBarrier animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/dimensional_br.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DimensionalBarrier animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/entity/dimensional_br.animation.json");
    }
}