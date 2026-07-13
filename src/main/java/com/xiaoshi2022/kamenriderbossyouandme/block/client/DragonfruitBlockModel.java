package com.xiaoshi2022.kamenriderbossyouandme.block.client;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DragonfruitBlockModel extends GeoModel<DragonfruitBlockEntity> {
    @Override
    public ResourceLocation getModelResource(DragonfruitBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/block/dragonfruitx.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonfruitBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/block/dragonfruitx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DragonfruitBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/block/dragonfruitx.animation.json");
    }
}