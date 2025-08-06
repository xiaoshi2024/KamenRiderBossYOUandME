package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.lord_baron;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LordBaronModel extends GeoModel<LordBaronEntity> {

    @Override
    public ResourceLocation getModelResource(LordBaronEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/lord_baron.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LordBaronEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/lord_baron.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LordBaronEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/lord_baron.animation.json");
    }
}

