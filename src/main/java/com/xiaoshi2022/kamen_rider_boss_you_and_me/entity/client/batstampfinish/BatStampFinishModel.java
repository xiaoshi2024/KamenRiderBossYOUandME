package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.batstampfinish;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatStampFinishEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class BatStampFinishModel extends GeoModel<BatStampFinishEntity> {

    @Override
    public ResourceLocation getModelResource(BatStampFinishEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/batstampfinish.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BatStampFinishEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/batstampfinish.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BatStampFinishEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/batstampfinish.animation.json");
    }
}