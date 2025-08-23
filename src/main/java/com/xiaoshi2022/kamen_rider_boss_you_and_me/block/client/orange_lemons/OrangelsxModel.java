package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.orange_lemons;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.OrangelsxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OrangelsxModel extends GeoModel<OrangelsxEntity> {
    @Override
    public ResourceLocation getModelResource(OrangelsxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/block/orangelsx.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(OrangelsxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/block/orangelsx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(OrangelsxEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/block/orangelsx.animation.json");
    }
}
