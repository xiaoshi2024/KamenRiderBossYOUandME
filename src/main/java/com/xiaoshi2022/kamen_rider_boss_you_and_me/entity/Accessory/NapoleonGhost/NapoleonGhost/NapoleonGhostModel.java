package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.NapoleonGhost.NapoleonGhost;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.NapoleonGhost.NapoleonGhostItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NapoleonGhostModel extends GeoModel<NapoleonGhostItem> {

    @Override
    public ResourceLocation getModelResource(NapoleonGhostItem animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/napoleon_ghost.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NapoleonGhostItem animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/napoleon_ghost.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NapoleonGhostItem animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/napoleon_ghost.animation.json");
    }
}