package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.baron_lemon_energy;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BaronLemonEnergyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

/**
 * 巴隆柠檬能量实体的模型类
 * 指定实体的geo模型、纹理和动画文件
 */
public class BaronLemonEnergyModel extends GeoModel<BaronLemonEnergyEntity> {

    @Override
    public ResourceLocation getModelResource(BaronLemonEnergyEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/baron_lemon_energy.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaronLemonEnergyEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/baron_lemon_energy.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaronLemonEnergyEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/baron_lemon_energy.animation.json");
    }
}