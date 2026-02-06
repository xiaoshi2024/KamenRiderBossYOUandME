package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.builddriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BuildDriverModel extends GeoModel<BuildDriver> {
    private final BuildDriver.BeltMode mode;
    private final boolean isTransforming;

    // 默认构造函数，使用DEFAULT模式
    public BuildDriverModel() {
        this(BuildDriver.BeltMode.DEFAULT, false);
    }

    // 带模式参数的构造函数
    public BuildDriverModel(BuildDriver.BeltMode mode) {
        this(mode, false);
    }

    // 带模式和变身状态参数的构造函数
    public BuildDriverModel(BuildDriver.BeltMode mode, boolean isTransforming) {
        this.mode = mode;
        this.isTransforming = isTransforming;
    }

    @Override
    public ResourceLocation getModelResource(BuildDriver object) {
        // 只有 HAZARD_RT 模式在变身状态或 HAZARD_RT_MOULD 模式时使用变身模型
        if ((isTransforming && mode == BuildDriver.BeltMode.HAZARD_RT) || mode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
            return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/build_driver_mould.geo.json");
        }
        
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
        // 只有 HAZARD_RT 模式在变身状态或 HAZARD_RT_MOULD 模式时使用变身纹理
        if ((isTransforming && mode == BuildDriver.BeltMode.HAZARD_RT) || mode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
            return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/build_driver_mould.png");
        }
        
        // 否则使用默认纹理
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/build_driver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BuildDriver animatable) {
        // 只有 HAZARD_RT 模式在变身状态或 HAZARD_RT_MOULD 模式时使用变身动画
        if ((isTransforming && mode == BuildDriver.BeltMode.HAZARD_RT) || mode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
            return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/build_driver_mould.animation.json");
        }
        
        // 否则使用默认动画
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/build_driver.animation.json");
    }
}
