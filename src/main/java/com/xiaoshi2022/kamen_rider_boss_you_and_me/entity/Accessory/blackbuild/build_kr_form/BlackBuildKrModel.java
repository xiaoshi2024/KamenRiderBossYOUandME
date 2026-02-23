package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.blackbuild.build_kr_form;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.blackbuild.BlackBuildKr;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BlackBuildKrModel extends GeoModel<BlackBuildKr> {
    @Override
    public ResourceLocation getModelResource(BlackBuildKr object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/black_build_kr.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlackBuildKr object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/black_build_kr.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlackBuildKr animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/black_build.animation.json");
    }
}
