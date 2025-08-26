package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.kivat;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KivatBatTwoNdModel extends GeoModel<KivatBatTwoNd> {

    /* .geo.json 路径（assets/modid/geo/xxx.geo.json） */
    @Override
    public ResourceLocation getModelResource(KivatBatTwoNd object) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/kivat_bat_two_nd.geo.json");
    }

    /* 贴图路径（assets/modid/textures/entity/xxx.png） */
    @Override
    public ResourceLocation getTextureResource(KivatBatTwoNd object) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/kivat_bat_two_nd.png");
    }

    /* 动画文件路径（assets/modid/animations/xxx.animation.json） */
    @Override
    public ResourceLocation getAnimationResource(KivatBatTwoNd object) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/kivat_bat_two_nd.animation.json");
    }
}