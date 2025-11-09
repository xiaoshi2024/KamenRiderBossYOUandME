package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Decades;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Decade;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;


public class Another_DecadeModel extends GeoModel<Another_Decade> {

    @Override
    public ResourceLocation getModelResource(Another_Decade animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/ai_decade_entity.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Another_Decade animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/ai_decade_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Another_Decade animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/ai_decade_entity.animation.json");
    }
}
