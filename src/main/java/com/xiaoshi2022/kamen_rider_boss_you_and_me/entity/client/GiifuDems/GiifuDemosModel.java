package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.GiifuDems;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GiifuDemosModel extends GeoModel<GiifuDemosEntity> {

    @Override
    public ResourceLocation getModelResource(GiifuDemosEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/giifudemos.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GiifuDemosEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/giifudemos.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GiifuDemosEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/giifudemos.animation.json");
    }
}
