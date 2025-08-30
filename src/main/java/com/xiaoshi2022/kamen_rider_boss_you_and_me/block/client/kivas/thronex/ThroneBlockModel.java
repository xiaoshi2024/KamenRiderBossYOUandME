package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas.thronex;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas.ThroneBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ThroneBlockModel  extends GeoModel<ThroneBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ThroneBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/block/throneblock.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ThroneBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/block/throneblock.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ThroneBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/block/throneblock.animation.json");
    }
}