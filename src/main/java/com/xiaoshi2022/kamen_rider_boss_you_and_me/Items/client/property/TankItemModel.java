package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.property;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.TankItem;

public class TankItemModel extends GeoModel<TankItem> {
    @Override
    public ResourceLocation getModelResource(TankItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:geo/item/tank.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TankItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:textures/item/tank.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TankItem animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:animations/item/tank.animation.json");
    }
}