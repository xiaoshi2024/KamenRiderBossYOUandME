package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Genesisdriver_cherryModel extends GeoModel<Genesis_driver> {
    public Genesisdriver_cherryModel(ResourceLocation Genesis_driver) {
        super();
    }
    @Override
    public ResourceLocation getModelResource(Genesis_driver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/genesis_driver_cherry.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Genesis_driver animatable){
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/genesis_driver_cherry.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Genesis_driver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/genesis_driver_cherry.animation.json");
    }
}
