package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight.noxknight;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight.NoxKnight;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NoxKnightModel extends GeoModel<NoxKnight> {
    @Override
    public ResourceLocation getModelResource(NoxKnight object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/nox_knight.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NoxKnight object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/nox_knight.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NoxKnight animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/nox_knight.animation.json");
    }
}