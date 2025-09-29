package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.dark_kiva_seal_barrier;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkKivaSealBarrierEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DarkKivaSealBarrierModel extends GeoModel<DarkKivaSealBarrierEntity> {
    @Override
    public ResourceLocation getModelResource(DarkKivaSealBarrierEntity object) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/dark_kiva_seal_barrier.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarkKivaSealBarrierEntity object) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/dark_kiva_seal_barrier.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DarkKivaSealBarrierEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/dark_kiva_seal_barrier.animation.json");
    }
}