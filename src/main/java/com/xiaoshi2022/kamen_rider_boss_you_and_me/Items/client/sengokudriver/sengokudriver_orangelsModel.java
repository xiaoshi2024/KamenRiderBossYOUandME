package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class sengokudriver_orangelsModel extends GeoModel<sengokudrivers_epmty> {
    public sengokudriver_orangelsModel(ResourceLocation sengokudriver_orangels) {
        super();
    }

    @Override
    public ResourceLocation getModelResource(sengokudrivers_epmty animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/sengokudriver_orangels.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(sengokudrivers_epmty animatable){
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/sengokudriver_orangels.png");
    }

    @Override
    public ResourceLocation getAnimationResource(sengokudrivers_epmty animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/sengokudriver_orangels.animation.json");
    }
}