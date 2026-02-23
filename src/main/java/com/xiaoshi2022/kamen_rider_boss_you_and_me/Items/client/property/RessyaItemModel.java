package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.property;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.RessyaItem;

public class RessyaItemModel extends GeoModel<RessyaItem> {
    @Override
    public ResourceLocation getModelResource(RessyaItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:geo/item/ressya.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RessyaItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:textures/item/ressya.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RessyaItem animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:animations/item/ressya.animation.json");
    }
}
