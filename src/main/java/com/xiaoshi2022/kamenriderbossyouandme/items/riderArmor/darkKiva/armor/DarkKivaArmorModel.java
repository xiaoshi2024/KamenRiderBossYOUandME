package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.darkKiva.armor;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.darkKiva.DarkKiva;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DarkKivaArmorModel extends GeoModel<DarkKiva> {
    @Override
    public ResourceLocation getAnimationResource(DarkKiva object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/dark_kiva.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DarkKiva object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/dark_kiva.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarkKiva object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/dark_kiva.png");
    }
}