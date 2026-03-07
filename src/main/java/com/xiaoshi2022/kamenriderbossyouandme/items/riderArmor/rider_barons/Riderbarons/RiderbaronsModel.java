package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.rider_barons.riderbarons;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.rider_barons.RiderBarons;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RiderbaronsModel extends GeoModel<RiderBarons> {
    @Override
    public ResourceLocation getModelResource(RiderBarons object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/rider_barons.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RiderBarons object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/rider_barons.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RiderBarons animatable) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/rider_barons.animation.json");
    }
}