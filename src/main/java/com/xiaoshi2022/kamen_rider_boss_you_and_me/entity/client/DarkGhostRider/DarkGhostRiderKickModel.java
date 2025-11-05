package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.DarkGhostRider;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkGhostRiderKickEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class DarkGhostRiderKickModel extends GeoModel<DarkGhostRiderKickEntity> {

    @Override
    public ResourceLocation getModelResource(DarkGhostRiderKickEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/ghost_eyes_u.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarkGhostRiderKickEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/ghost_eyes_u.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DarkGhostRiderKickEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/ghost_eyes_u.animation.json");
    }
}