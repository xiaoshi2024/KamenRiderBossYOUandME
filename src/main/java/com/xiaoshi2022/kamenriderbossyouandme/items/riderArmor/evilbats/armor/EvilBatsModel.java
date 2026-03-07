package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.evilbats.armor;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.evilbats.EvilBats;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EvilBatsModel extends GeoModel<EvilBats> {
    @Override
    public ResourceLocation getAnimationResource(EvilBats object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/evilbats.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(EvilBats object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/evilbats.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EvilBats object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/evilbats.png");
    }
}