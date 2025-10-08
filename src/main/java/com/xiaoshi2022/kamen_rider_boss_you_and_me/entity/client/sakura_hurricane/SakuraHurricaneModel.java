package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.sakura_hurricane;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EntitySakuraHurricane;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SakuraHurricaneModel extends GeoModel<EntitySakuraHurricane> {

    @Override
    public ResourceLocation getModelResource(EntitySakuraHurricane animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/sakura_hurricane.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EntitySakuraHurricane animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/sakura_hurricane.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EntitySakuraHurricane animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/sakura_hurricane.animation.json");
    }
}