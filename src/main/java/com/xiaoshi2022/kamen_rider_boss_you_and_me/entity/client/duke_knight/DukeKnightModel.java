package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.duke_knight;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DukeKnightEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DukeKnightModel extends GeoModel<DukeKnightEntity> {

    @Override
    public ResourceLocation getModelResource(DukeKnightEntity animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/duke_knight.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DukeKnightEntity animatable) {
        // 确认纹理文件的实际位置
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/duke_knight.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DukeKnightEntity animatable) {
        // 确认动画文件的实际位置
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/duke_knight.animation.json");
    }
}