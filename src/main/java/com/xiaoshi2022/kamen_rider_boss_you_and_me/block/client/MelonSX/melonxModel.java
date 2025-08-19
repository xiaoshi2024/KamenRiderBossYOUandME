package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.MelonSX;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.melonxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class melonxModel extends GeoModel<melonxEntity> {
    @Override
    public ResourceLocation getModelResource(melonxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/block/melonsx.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(melonxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/block/melonsx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(melonxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/block/melonsx.animation.json");
    }
}