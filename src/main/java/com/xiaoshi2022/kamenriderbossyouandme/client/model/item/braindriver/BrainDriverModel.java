package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.braindriver;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrainDriverModel<T extends BrainDriver> extends GeoModel<T> {
    private final BrainDriver.BeltMode mode;

    // 默认构造函数，使用DEFAULT模式
    public BrainDriverModel() {
        this(BrainDriver.BeltMode.DEFAULT);
    }

    // 带模式参数的构造函数
    public BrainDriverModel(BrainDriver.BeltMode mode) {
        this.mode = mode;
    }

    @Override
    public ResourceLocation getModelResource(T t) {
        // 根据模式返回不同的模型
        if (mode == BrainDriver.BeltMode.BRAIN) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/brain_driver_bright.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/brain_driver.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T t) {
        // 所有模式使用相同的纹理
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/brain_driver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(T t) {
        // 所有模式使用相同的动画资源
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/brain_driver.animation.json");
    }
}
