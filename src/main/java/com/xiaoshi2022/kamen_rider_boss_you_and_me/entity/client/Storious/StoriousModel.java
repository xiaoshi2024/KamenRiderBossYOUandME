package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Storious;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class StoriousModel extends GeoModel<StoriousEntity> {

    @Override
    public ResourceLocation getModelResource(StoriousEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/storious.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(StoriousEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/storious.png");
    }

    @Override
    public ResourceLocation getAnimationResource(StoriousEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/storious.animation.json");
    }
}
