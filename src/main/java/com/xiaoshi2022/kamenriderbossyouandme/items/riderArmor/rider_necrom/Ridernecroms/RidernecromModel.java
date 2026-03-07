package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.rider_necrom.Ridernecroms;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.rider_necrom.RiderNecrom;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RidernecromModel extends GeoModel<RiderNecrom> {
    @Override
    public ResourceLocation getModelResource(RiderNecrom object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/rider_necrom.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RiderNecrom object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/rider_necrom.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RiderNecrom animatable) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/rider_necrom.animation.json");
    }
}