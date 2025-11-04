package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.ghostdriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GhostDriverDarkModel extends GeoModel<GhostDriver> {

    public GhostDriverDarkModel(ResourceLocation modelResource) {
    }

    @Override
    public ResourceLocation getModelResource(GhostDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me","geo/item/ghost_driver_dark.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GhostDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/ghost_driver_dark.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GhostDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/ghost_driver_dark.animation.json");
    }
}