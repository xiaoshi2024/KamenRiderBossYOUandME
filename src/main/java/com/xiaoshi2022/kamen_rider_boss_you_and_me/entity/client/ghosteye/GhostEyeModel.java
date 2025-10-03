package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.ghosteye;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GhostEyeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class GhostEyeModel extends GeoModel<GhostEyeEntity> {

    @Override
    public ResourceLocation getModelResource(GhostEyeEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/necrom_eyes.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GhostEyeEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/necrom_eyes.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GhostEyeEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/necrom_eyes.animation.json");
    }
}