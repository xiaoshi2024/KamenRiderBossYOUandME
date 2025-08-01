package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class sengokudrivers_epmtyModel extends GeoModel<sengokudrivers_epmty> {
    public sengokudrivers_epmtyModel(ResourceLocation sengokudrivers_epmty_epmty) {
        super();
    }

    @Override
    public ResourceLocation getModelResource(sengokudrivers_epmty animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/sengokudrivers_epmty.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(sengokudrivers_epmty animatable){
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/sengokudrivers.png");
    }

    @Override
    public ResourceLocation getAnimationResource(sengokudrivers_epmty animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/sengokudrivers_epmty.animation.json");
    }
}