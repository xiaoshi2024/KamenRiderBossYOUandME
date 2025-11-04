package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.ghostdriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GhostDriverModel extends GeoModel<GhostDriver> {

    @Override
    public ResourceLocation getModelResource(GhostDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/ghostdriver.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GhostDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/ghostdriver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GhostDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/ghostdriver.animation.json");
    }
}