package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gifftarian;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GifftarianModel extends GeoModel<Gifftarian> {

    @Override
    public ResourceLocation getModelResource(Gifftarian animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/gifftarian.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Gifftarian animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/gifftarian.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Gifftarian animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/gifftarian.animation.json");
    }
}