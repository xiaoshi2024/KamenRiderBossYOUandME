package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gamma_eyecon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GammaEyeconEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class GammaEyeconModel extends GeoModel<GammaEyeconEntity> {

    @Override
    public ResourceLocation getModelResource(GammaEyeconEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/gamma_eyecons.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GammaEyeconEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/gamma_eyecons.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GammaEyeconEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/gamma_eyecons.animation.json");
    }
}