package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.two_sidriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Two_sidriver_batModel extends GeoModel<Two_sidriver> {
    @Override
    public ResourceLocation getModelResource(Two_sidriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/two_sidriver_bat.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Two_sidriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/two_sidriver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Two_sidriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/two_sidriver_bat.animation.json");
    }
}