package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.knecromghost;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.KnecromghostEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class KnecromghostModel  extends GeoModel<KnecromghostEntity> {

    @Override
    public ResourceLocation getModelResource(KnecromghostEntity animatable) {
        return new ResourceLocation(MODID, "geo/entity/krnecrom_ghost.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KnecromghostEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/krnecrom_ghost.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KnecromghostEntity animatable) {
        return new ResourceLocation(MODID, "animations/entity/krnecrom_ghost.animation.json");
    }
}