package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Bananas;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.BananasEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BananasModel extends GeoModel<BananasEntity> {
    @Override
    public ResourceLocation getModelResource(BananasEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/block/bananas.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BananasEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/block/bananas.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BananasEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/block/bananas.animation.json");
    }
}