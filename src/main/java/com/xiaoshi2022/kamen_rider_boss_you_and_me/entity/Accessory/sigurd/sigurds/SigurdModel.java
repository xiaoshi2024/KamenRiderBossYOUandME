package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.sigurds;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SigurdModel extends GeoModel<Sigurd> {
    @Override
    public ResourceLocation getAnimationResource(Sigurd object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/sigurd.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Sigurd object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/sigurd.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Sigurd object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/sigurd.png");
    }
}