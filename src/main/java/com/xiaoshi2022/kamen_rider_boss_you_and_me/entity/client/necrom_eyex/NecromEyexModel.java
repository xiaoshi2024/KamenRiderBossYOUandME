package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.necrom_eyex;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.NecromEyexEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class NecromEyexModel extends GeoModel<NecromEyexEntity> {

    @Override
    public ResourceLocation getModelResource(NecromEyexEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/necrom_eyex.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NecromEyexEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/necrom_eyex.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NecromEyexEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/necrom_eyex.animation.json");
    }
}