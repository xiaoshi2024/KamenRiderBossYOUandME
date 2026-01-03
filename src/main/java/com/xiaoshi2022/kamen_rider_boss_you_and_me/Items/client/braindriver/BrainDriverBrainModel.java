package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.braindriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BrainDriver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrainDriverBrainModel extends GeoModel<BrainDriver> {

    public BrainDriverBrainModel(ResourceLocation modelResource) {
    }

    @Override
    public ResourceLocation getModelResource(BrainDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me","geo/item/brain_driver.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BrainDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/brain_driver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BrainDriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/brain_driver.animation.json");
    }
}