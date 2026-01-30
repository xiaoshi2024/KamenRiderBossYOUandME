package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.weekendriver;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.WeekEndriver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WeekEndriverModel extends GeoModel<WeekEndriver> {
    private final WeekEndriver.BeltMode mode;

    // 默认构造函数，使用DEFAULT模式
    public WeekEndriverModel() {
        this(WeekEndriver.BeltMode.DEFAULT);
    }

    // 带模式参数的构造函数
    public WeekEndriverModel(WeekEndriver.BeltMode mode) {
        this.mode = mode;
    }

    @Override
    public ResourceLocation getModelResource(WeekEndriver object) {
        // 根据模型实例的模式返回不同的模型
        if (mode == WeekEndriver.BeltMode.QUEEN_BEE) {
            return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/week_endriver_bee.geo.json");
        }
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/week_endriver.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WeekEndriver object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/week_endriver.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WeekEndriver animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/week_endriver.animation.json");
    }
}