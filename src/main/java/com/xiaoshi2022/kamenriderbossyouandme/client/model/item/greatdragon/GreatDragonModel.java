package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.greatdragon;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.GreatDragon;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GreatDragonModel extends GeoModel<GreatDragon> {

    private final GreatDragon.Mode mode;

    public GreatDragonModel() {
        this(GreatDragon.Mode.EMPTY);
    }

    public GreatDragonModel(GreatDragon.Mode mode) {
        this.mode = mode;
    }

    @Override
    public ResourceLocation getModelResource(GreatDragon object) {
        return switch (mode) {
            case EMPTY -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/great_dragon_empty.geo.json");
            default -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/great_dragon.geo.json");
        };
    }

    @Override
    public ResourceLocation getTextureResource(GreatDragon object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/great_dragon.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GreatDragon animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/great_dragon.animation.json");
    }
}