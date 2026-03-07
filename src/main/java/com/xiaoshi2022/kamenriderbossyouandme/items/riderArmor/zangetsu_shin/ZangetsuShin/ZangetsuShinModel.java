package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.zangetsu_shin.zangetsushin;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.zangetsu_shin.zangetsuShin;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZangetsuShinModel extends GeoModel<zangetsuShin> {
    @Override
    public ResourceLocation getModelResource(zangetsuShin object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/zangetsu_shin.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(zangetsuShin object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/zangetsu_shin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(zangetsuShin animatable) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/zangetsu_shin.animation.json");
    }
}