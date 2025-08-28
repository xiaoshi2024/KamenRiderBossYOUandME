package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.armor;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DarkKivaArmorModel extends GeoModel<DarkKivaItem> {
    @Override
    public ResourceLocation getModelResource(DarkKivaItem object) {
        // 这里需要指向实际的模型文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/dark_kiva.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarkKivaItem object) {
        // 这里需要指向实际的纹理文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/dark_kiva.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DarkKivaItem animatable) {
        // 这里需要指向实际的动画文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/dark_kiva.animation.json");
    }
}