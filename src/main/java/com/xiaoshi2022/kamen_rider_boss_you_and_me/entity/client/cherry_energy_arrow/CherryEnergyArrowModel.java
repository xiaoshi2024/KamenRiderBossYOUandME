package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.cherry_energy_arrow;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.CherryEnergyArrowEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

/**
 * 樱桃能量箭矢实体的模型类
 * 指定实体的geo模型、纹理和动画文件
 */
public class CherryEnergyArrowModel extends GeoModel<CherryEnergyArrowEntity> {

    @Override
    public ResourceLocation getModelResource(CherryEnergyArrowEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/cherry_energy_arrow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CherryEnergyArrowEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/cherry_energy_arrow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CherryEnergyArrowEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/cherry_energy_arrow.animation.json");
    }
}