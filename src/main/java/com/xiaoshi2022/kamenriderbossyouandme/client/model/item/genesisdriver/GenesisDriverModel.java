package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.genesisdriver;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.Genesis_driver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenesisDriverModel<T extends Genesis_driver> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(T t) {
        // 根据不同的腰带模式返回不同的模型
        switch (t.getCurrentMode()) {
            case LEMON:
                return getLemonModelResource();
            case MELON:
                return getMelonModelResource();
            case CHERRY:
                return getCherryModelResource();
            case PEACH:
                return getPeachModelResource();
            case DRAGONFRUIT:
                return getDragonFruitModelResource();
            default:
                return getDefaultModelResource();
        }
    }

    @Override
    public ResourceLocation getTextureResource(T t) {
        // 根据不同的腰带模式返回不同的纹理
        switch (t.getCurrentMode()) {
            case LEMON:
                return getLemonTextureResource();
            case MELON:
                return getMelonTextureResource();
            case CHERRY:
                return getCherryTextureResource();
            case PEACH:
                return getPeachTextureResource();
            case DRAGONFRUIT:
                return getDragonFruitTextureResource();
            default:
                return getDefaultTextureResource();
        }
    }

    @Override
    public ResourceLocation getAnimationResource(T t) {
        // 根据不同的腰带模式返回不同的动画
        switch (t.getCurrentMode()) {
            case LEMON:
                return getLemonAnimationResource();
            case MELON:
                return getMelonAnimationResource();
            case CHERRY:
                return getCherryAnimationResource();
            case PEACH:
                return getPeachAnimationResource();
            case DRAGONFRUIT:
                return getDragonFruitAnimationResource();
            default:
                return getDefaultAnimationResource();
        }
    }

    // 默认模型资源
    public ResourceLocation getDefaultModelResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/genesis_driver.geo.json");
    }

    // 默认纹理资源
    public ResourceLocation getDefaultTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/genesis_driver_lemo.png");
    }

    // 默认动画资源
    public ResourceLocation getDefaultAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/genesis_driver.animation.json");
    }

    // 柠檬模式模型资源
    public ResourceLocation getLemonModelResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/genesis_driver_lemon.geo.json");
    }

    // 柠檬模式纹理资源
    public ResourceLocation getLemonTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/genesis_driver_lemo.png");
    }

    // 柠檬模式动画资源
    public ResourceLocation getLemonAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/genesis_driver_lemon.animation.json");
    }

    // 甜瓜模式模型资源
    public ResourceLocation getMelonModelResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/genesis_driver_melon.geo.json");
    }

    // 甜瓜模式纹理资源
    public ResourceLocation getMelonTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/genesis_driver_melon.png");
    }

    // 甜瓜模式动画资源
    public ResourceLocation getMelonAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/genesis_driver_melon.animation.json");
    }

    // 樱桃模式模型资源
    public ResourceLocation getCherryModelResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/genesis_driver_cherry.geo.json");
    }

    // 樱桃模式纹理资源
    public ResourceLocation getCherryTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/genesis_driver_cherry.png");
    }

    // 樱桃模式动画资源
    public ResourceLocation getCherryAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/genesis_driver_cherry.animation.json");
    }

    // 桃子模式模型资源
    public ResourceLocation getPeachModelResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/genesis_driver_peach.geo.json");
    }

    // 桃子模式纹理资源
    public ResourceLocation getPeachTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/genesis_driver_peach.png");
    }

    // 桃子模式动画资源
    public ResourceLocation getPeachAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/genesis_driver_peach.animation.json");
    }

    // 火龙果模式模型资源
    public ResourceLocation getDragonFruitModelResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/genesis_driver_dragonfruit.geo.json");
    }

    // 火龙果模式纹理资源
    public ResourceLocation getDragonFruitTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/genesis_driver_dragonfruit.png");
    }

    // 火龙果模式动画资源
    public ResourceLocation getDragonFruitAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/genesis_driver_dragonfruit.animation.json");
    }
}