package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.SakuraHurricaneLockseed;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.SakuraHurricaneLockseed;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SakuraHurricaneLockseedModel extends GeoModel<SakuraHurricaneLockseed> {
    @Override
    public ResourceLocation getModelResource(SakuraHurricaneLockseed animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/sakura_lockseed.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SakuraHurricaneLockseed animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/sakura_lockseed.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SakuraHurricaneLockseed animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/sakura_lockseed.animation.json");
    }
}