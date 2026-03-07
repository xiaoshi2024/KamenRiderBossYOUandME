package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.marika.marikas;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.marika.Marika;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MarikaModel extends GeoModel<Marika> {
    @Override
    public ResourceLocation getAnimationResource(Marika object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/marika.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Marika object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/marika.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Marika object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/marika.png");
    }
}