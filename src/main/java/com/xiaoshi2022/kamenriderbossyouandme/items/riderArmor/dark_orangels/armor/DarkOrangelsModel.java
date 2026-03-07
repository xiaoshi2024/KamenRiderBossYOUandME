package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.dark_orangels.armor;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.dark_orangels.DarkOrangels;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DarkOrangelsModel extends GeoModel<DarkOrangels> {
    @Override
    public ResourceLocation getAnimationResource(DarkOrangels object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/dark_orangels.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DarkOrangels object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/dark_orangels.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarkOrangels object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/dark_orangels.png");
    }
}