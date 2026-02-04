package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.blackbuild.builds;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.blackbuild.BlackBuild;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackBuildModel extends GeoModel<BlackBuild> {
    @Override
    public ResourceLocation getModelResource(BlackBuild object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/black_build.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackBuild object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/black_build.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlackBuild animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/black_build.animation.json");
    }
}