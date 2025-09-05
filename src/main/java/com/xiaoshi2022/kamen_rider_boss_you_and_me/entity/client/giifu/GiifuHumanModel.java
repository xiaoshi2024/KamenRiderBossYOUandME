package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.GiifuHumanEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GiifuHumanModel extends GeoModel<GiifuHumanEntity> {

    @Override
    public ResourceLocation getModelResource(GiifuHumanEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/giifurex.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GiifuHumanEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/giifurex.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GiifuHumanEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/giifurex.animation.json");
    }
}