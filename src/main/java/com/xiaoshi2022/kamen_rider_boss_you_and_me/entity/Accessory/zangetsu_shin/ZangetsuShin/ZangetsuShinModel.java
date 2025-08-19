package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ZangetsuShinModel extends GeoModel<ZangetsuShinItem> {
    @Override
    public ResourceLocation getAnimationResource(ZangetsuShinItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/zangetsu_shin.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(ZangetsuShinItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/zangetsu_shin.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZangetsuShinItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/zangetsu_shin.png");
    }
}