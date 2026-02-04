package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.hazardtrigger;

import net.minecraft.resources.ResourceLocation;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.HazardTrigger;
import software.bernie.geckolib.model.GeoModel;

public class HazardTriggerModel extends GeoModel<HazardTrigger> {

    @Override
    public ResourceLocation getModelResource(HazardTrigger object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/hazard_trigger.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HazardTrigger object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/hazard_trigger.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HazardTrigger animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/hazard_trigger.animation.json");
    }
}
