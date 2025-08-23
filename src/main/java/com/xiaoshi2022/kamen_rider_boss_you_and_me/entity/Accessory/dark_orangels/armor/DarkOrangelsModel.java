package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.armor;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DarkOrangelsModel extends GeoModel<Dark_orangels> {
    @Override
    public ResourceLocation getModelResource(Dark_orangels object) {
        // 这里需要指向实际的模型文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/dark_orangels.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Dark_orangels object) {
        // 这里需要指向实际的纹理文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/dark_orangels.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Dark_orangels animatable) {
        // 这里需要指向实际的动画文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/dark_orangels.animation.json");
    }
}