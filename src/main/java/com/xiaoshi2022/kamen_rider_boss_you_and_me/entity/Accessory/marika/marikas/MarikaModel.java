package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.marikas;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class MarikaModel extends GeoModel<Marika> {
    @Override
    public ResourceLocation getModelResource(Marika animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/marika.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Marika animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/marika.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Marika animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/marika.animation.json");
    }
}