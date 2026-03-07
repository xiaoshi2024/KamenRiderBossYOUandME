package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.build_kr_form;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.BlackBuildKr;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackBuildKrModel extends GeoModel<BlackBuildKr> {
    @Override
    public ResourceLocation getAnimationResource(BlackBuildKr object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/black_build_kr.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BlackBuildKr object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/black_build_kr.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackBuildKr object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/black_build_kr.png");
    }
}