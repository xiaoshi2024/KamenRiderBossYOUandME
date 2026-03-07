package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.quinbee.quinbees;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.quinbee.Quinbee;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class QuinbeeModel extends GeoModel<Quinbee> {
    @Override
    public ResourceLocation getModelResource(Quinbee object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/quinbee.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Quinbee object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/quinbee.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Quinbee animatable) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/quinbee.animation.json");
    }
}