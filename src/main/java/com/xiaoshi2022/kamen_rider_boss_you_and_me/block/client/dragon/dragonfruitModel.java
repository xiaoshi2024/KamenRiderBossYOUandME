package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.dragon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.DragonfruitBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class dragonfruitModel extends GeoModel<DragonfruitBlockEntity> {
    @Override
    public ResourceLocation getModelResource(DragonfruitBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/block/dragonfruitx.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonfruitBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/block/dragonfruitx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DragonfruitBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/block/dragonfruitx.animation.json");
    }
}