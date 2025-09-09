package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.armor;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EvilBatsModel extends GeoModel<EvilBatsArmor> {
    @Override
    public ResourceLocation getModelResource(EvilBatsArmor object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/evilbats.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EvilBatsArmor object) {
        // 假设纹理文件名为evilbats.png，位于textures/item/armor目录下
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/evilbats.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EvilBatsArmor animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/evilbats.animation.json");
    }
}