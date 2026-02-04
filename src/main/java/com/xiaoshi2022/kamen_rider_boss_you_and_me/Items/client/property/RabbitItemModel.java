package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.property;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.RabbitItem;

public class RabbitItemModel extends GeoModel<RabbitItem> {
    @Override
    public ResourceLocation getModelResource(RabbitItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:geo/item/rabbit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RabbitItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:textures/item/rabbit.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RabbitItem animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:animations/item/rabbit.animation.json");
    }
}