package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SengokuDriverModel extends GeoModel<sengokudrivers_epmty> {
    public SengokuDriverModel(ResourceLocation sengokudrivers_epmty) {
        super();
    }

    @Override
    // 重写getModelResource方法，用于获取模型资源
    public ResourceLocation getModelResource(sengokudrivers_epmty animatable) {
        // 返回模型资源的位置
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/sengokudrivers.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(sengokudrivers_epmty animatable){
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/sengokudrivers.png");
    }

    @Override
    public ResourceLocation getAnimationResource(sengokudrivers_epmty animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/sengokudrivers.animation.json");
    }
}
