package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.builds;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blackbuild.BlackBuild;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackBuildModel extends GeoModel<BlackBuild> {
    @Override
    public ResourceLocation getAnimationResource(BlackBuild object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/black_build.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BlackBuild object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/black_build.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackBuild object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/black_build.png");
    }
}