package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.Giifs;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.GiifuSleepingStateBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GiifuSleepingStateBlockModel extends GeoModel<GiifuSleepingStateBlockEntity> {
    @Override
    public ResourceLocation getModelResource(GiifuSleepingStateBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/block/giifu_sleeping_state.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GiifuSleepingStateBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/block/giifu_sleeping_state.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GiifuSleepingStateBlockEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/block/giifu_sleeping_state.animation.json");
    }

}