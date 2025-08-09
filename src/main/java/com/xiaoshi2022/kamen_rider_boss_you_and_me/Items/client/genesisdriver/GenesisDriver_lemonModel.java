package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenesisDriver_lemonModel extends GeoModel<Genesis_driver> {
    public GenesisDriver_lemonModel(ResourceLocation Genesis_driver) {
        super();
    }

    @Override
    // 重写getModelResource方法，用于获取模型资源
    public ResourceLocation getModelResource(Genesis_driver animatable) {
        // 返回模型资源的位置
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/genesis_driver_lemo.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Genesis_driver animatable){
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/genesis_driver_lemo.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Genesis_driver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/genesis_driver_lemon.animation.json");
    }
}