package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Zi_os;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Zi_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Another_Zi_oModel extends GeoModel<Another_Zi_o> {

    @Override
    public ResourceLocation getModelResource(Another_Zi_o animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/another_zi_o.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Another_Zi_o animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/another_zi_o.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Another_Zi_o animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/another_zi_o.animation.json");
    }
}
