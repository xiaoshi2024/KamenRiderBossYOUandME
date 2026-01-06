package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.BrainRoidmude;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Roidmude.BrainRoidmudeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrainRoidmudeModel extends GeoModel<BrainRoidmudeEntity> {
    @Override
    public ResourceLocation getModelResource(BrainRoidmudeEntity object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/entity/brain_monster.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BrainRoidmudeEntity object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/brain_monster.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BrainRoidmudeEntity animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/entity/brain_monster.animation.json");
    }
}
