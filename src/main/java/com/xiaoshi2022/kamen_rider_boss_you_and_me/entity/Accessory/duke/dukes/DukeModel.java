package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.dukes;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DukeModel extends GeoModel<Duke> {
    @Override
    public ResourceLocation getModelResource(Duke object) {
        // 这里需要指向实际的模型文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/duke.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Duke object) {
        // 这里需要指向实际的纹理文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/duke.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Duke animatable) {
        // 这里需要指向实际的动画文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/duke.animation.json");
    }

}