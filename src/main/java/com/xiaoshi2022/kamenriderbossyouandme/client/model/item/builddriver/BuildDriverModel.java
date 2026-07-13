package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.builddriver;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BuildDriverModel extends GeoModel<BuildDriver> {
    private final BuildDriver.BeltMode mode;
    private final boolean isTransforming;

    public BuildDriverModel() {
        this(BuildDriver.BeltMode.DEFAULT, false);
    }

    public BuildDriverModel(BuildDriver.BeltMode mode) {
        this(mode, false);
    }

    public BuildDriverModel(BuildDriver.BeltMode mode, boolean isTransforming) {
        this.mode = mode;
        this.isTransforming = isTransforming;
    }

    @Override
    public ResourceLocation getModelResource(BuildDriver object) {
        if ((isTransforming && mode == BuildDriver.BeltMode.HAZARD_RT) || mode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_mould.geo.json");
        }

        return switch (mode) {
            case RT -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_rt.geo.json");
            case R -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_r.geo.json");
            case T -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_t.geo.json");
            case HAZARD_EMPTY -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_hazard_empty.geo.json");
            case HAZARD_RT -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_hazard_rt.geo.json");
            case HAZARD_R -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_hazard_r.geo.json");
            case HAZARD_T -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_hazard_t.geo.json");
            case HAZARD_K -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_hazard_k.geo.json");
            case HAZARD_KR -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_hazard_kr.geo.json");
            case HAZARD_RESSYA -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver_hazard_ressya.geo.json");
            default -> ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/build_driver.geo.json");
        };
    }

    @Override
    public ResourceLocation getTextureResource(BuildDriver object) {
        if ((isTransforming && mode == BuildDriver.BeltMode.HAZARD_RT) || mode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/build_driver_mould.png");
        }

        if (mode == BuildDriver.BeltMode.HAZARD_K || mode == BuildDriver.BeltMode.HAZARD_KR || mode == BuildDriver.BeltMode.HAZARD_RESSYA) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/build_driver_hazard_kr.png");
        }

        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/build_driver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BuildDriver animatable) {
        if ((isTransforming && mode == BuildDriver.BeltMode.HAZARD_RT) || mode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/build_driver_mould.animation.json");
        }

        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/build_driver.animation.json");
    }
}