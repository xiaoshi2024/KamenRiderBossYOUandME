package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.PeachEnergy;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.peach_energy;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PeachEnergyModel extends GeoModel<peach_energy> {
    @Override
    public ResourceLocation getModelResource(peach_energy animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/peach_energy.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(peach_energy animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/peach_energy.png");
    }

    @Override
    public ResourceLocation getAnimationResource(peach_energy animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/peach_energy.animation.json");
    }
}