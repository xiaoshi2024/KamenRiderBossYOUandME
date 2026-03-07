package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.duke.dukes;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.duke.Duke;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DukeModel extends GeoModel<Duke> {
    @Override
    public ResourceLocation getAnimationResource(Duke object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/duke.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Duke object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/duke.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Duke object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/duke.png");
    }
}