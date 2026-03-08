package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.braindriver;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrainDriverModel<T extends BrainDriver> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(T t) {
        // 根据不同的腰带模式返回不同的模型
        switch (t.getCurrentMode()) {
            case BRAIN:
                return getBrainModelResource();
            default:
                return getDefaultModelResource();
        }
    }

    @Override
    public ResourceLocation getTextureResource(T t) {
        // 根据不同的腰带模式返回不同的纹理
        switch (t.getCurrentMode()) {
            case BRAIN:
                return getBrainTextureResource();
            default:
                return getDefaultTextureResource();
        }
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

    // Brain模式模型资源
    public ResourceLocation getBrainModelResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/brain_driver_brain.geo.json");
    }

    // Brain模式纹理资源
    public ResourceLocation getBrainTextureResource() {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/brain_driver_brain.png");
    }
}
