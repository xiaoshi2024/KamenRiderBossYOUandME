package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.renderer.entity;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.AnotherDenlinerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class AnotherDenlinerModel extends GeoModel<AnotherDenlinerEntity> {

    @Override
    public ResourceLocation getModelResource(AnotherDenlinerEntity object) {
        return new ResourceLocation(MODID, "geo/entity/ai_den_liner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AnotherDenlinerEntity object) {
        return new ResourceLocation(MODID, "textures/entity/ai_den_liner.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AnotherDenlinerEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/ai_den_liner.animation.json");
    }
}