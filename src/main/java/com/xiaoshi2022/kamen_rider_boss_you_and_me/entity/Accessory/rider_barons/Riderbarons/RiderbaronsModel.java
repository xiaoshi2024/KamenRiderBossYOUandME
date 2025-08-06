package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.Riderbarons;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RiderbaronsModel extends GeoModel<rider_baronsItem> {
    @Override
    public ResourceLocation getAnimationResource(rider_baronsItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/rider_barons.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(rider_baronsItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/rider_barons.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(rider_baronsItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/rider_barons.png");
    }
}

