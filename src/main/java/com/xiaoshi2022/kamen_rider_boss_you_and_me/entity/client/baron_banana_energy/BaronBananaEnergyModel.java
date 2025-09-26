package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.baron_banana_energy;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BaronBananaEnergyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class BaronBananaEnergyModel extends GeoModel<BaronBananaEnergyEntity> {

    @Override
    public ResourceLocation getModelResource(BaronBananaEnergyEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/baron_banana_energy.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaronBananaEnergyEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/baron_banana_energy.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaronBananaEnergyEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/baron_banana_energy.animation.json");
    }
}