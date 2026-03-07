package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.sigurd.sigurds;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.sigurd.Sigurd;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SigurdModel extends GeoModel<Sigurd> {
    @Override
    public ResourceLocation getAnimationResource(Sigurd object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/sigurd.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Sigurd object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/sigurd.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Sigurd object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/sigurd.png");
    }
}