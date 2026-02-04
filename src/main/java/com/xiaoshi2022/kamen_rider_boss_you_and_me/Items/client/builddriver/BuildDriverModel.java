package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.builddriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BuildDriverModel extends GeoModel<BuildDriver> {
    private final BuildDriver.BeltMode mode;

    // 默认构造函数，使用DEFAULT模式
    public BuildDriverModel() {
        this(BuildDriver.BeltMode.DEFAULT);
    }

    // 带模式参数的构造函数
    public BuildDriverModel(BuildDriver.BeltMode mode) {
        this.mode = mode;
    }

    @Override
    public ResourceLocation getModelResource(BuildDriver object) {
        // 根据模型实例的模式返回不同的模型
        switch (mode) {
            case RT:
                return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver_rt.geo.json");
            case R:
                return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver_r.geo.json");
            case T:
                return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver_t.geo.json");
            case HAZARD_EMPTY:
                return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver_hazard_empty.geo.json");
            case HAZARD_RT:
                return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver_hazard_rt.geo.json");
            case HAZARD_R:
                return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver_hazard_r.geo.json");
            case HAZARD_T:
                return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver_hazard_t.geo.json");
            default:
                return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(BuildDriver object) {
        // 暂时使用同一个纹理，后续可以根据需要添加不同纹理
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/build_driver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BuildDriver animatable) {
        // 使用同一个动画文件
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/build_driver.animation.json");
    }
}
