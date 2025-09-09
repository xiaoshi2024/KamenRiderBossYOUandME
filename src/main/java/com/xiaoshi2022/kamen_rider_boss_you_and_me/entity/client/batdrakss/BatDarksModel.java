package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.batdrakss;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatDarksEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class BatDarksModel extends GeoModel<BatDarksEntity> {

    @Override
    public ResourceLocation getModelResource(BatDarksEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/batdarks.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BatDarksEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/batdarks.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BatDarksEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/batdarks.animation.json");
    }
}