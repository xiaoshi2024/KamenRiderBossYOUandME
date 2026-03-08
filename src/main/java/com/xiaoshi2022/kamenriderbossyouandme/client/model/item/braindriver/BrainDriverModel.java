package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.braindriver;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrainDriverModel<T extends BrainDriver> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(T t) {
        // 所有模式使用相同的模型
        return getDefaultModelResource();
    }

    @Override
    public ResourceLocation getTextureResource(T t) {
        // 所有模式使用相同的纹理
        return getDefaultTextureResource();
    }

    @Override
    public ResourceLocation getAnimationResource(T t) {
        // 所有模式使用相同的动画资源
        return getDefaultAnimationResource();
    }

    // 默认模型资源
    public ResourceLocation getDefaultModelResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/brain_driver.geo.json");
    }

    // 默认纹理资源
    public ResourceLocation getDefaultTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/brain_driver.png");
    }

    // 默认动画资源
    public ResourceLocation getDefaultAnimationResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/brain_driver.animation.json");
    }
}
