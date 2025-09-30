package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.melon_energy_slash;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.MelonEnergySlashEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

/**
 * 蜜瓜能量斩击实体的模型类
 * 指定实体的geo模型、纹理和动画文件
 */
public class MelonEnergySlashModel extends GeoModel<MelonEnergySlashEntity> {

    @Override
    public ResourceLocation getModelResource(MelonEnergySlashEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/melon_energy_slash.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MelonEnergySlashEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/melon_energy_slash.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MelonEnergySlashEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/melon_energy_slash.animation.json");
    }
}