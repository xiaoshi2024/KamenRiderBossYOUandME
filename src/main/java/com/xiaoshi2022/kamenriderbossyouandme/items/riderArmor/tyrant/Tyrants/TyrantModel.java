package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant.Tyrants;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.tyrant.Tyrant;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TyrantModel extends GeoModel<Tyrant> {
    @Override
    public ResourceLocation getModelResource(Tyrant object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/tyrant.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Tyrant object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/tyrant.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Tyrant animatable) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/tyrant.animation.json");
    }
}