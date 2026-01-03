package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.brain.brains;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.brain.Brain;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrainModel extends GeoModel<Brain> {
    @Override
    public ResourceLocation getModelResource(Brain object) {
        // 这里需要指向实际的模型文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/brain_rider.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Brain object) {
        // 这里需要指向实际的纹理文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/brain_rider.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Brain animatable) {
        // 这里需要指向实际的动画文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/brain_rider.animation.json");
    }
}