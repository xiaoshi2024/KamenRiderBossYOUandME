package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DarkRiderGhost.DarkRiderGhost;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DarkRiderGhost.DarkRiderGhostItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DarkRiderGhostModel extends GeoModel<DarkRiderGhostItem> {
    @Override
    public ResourceLocation getModelResource(DarkRiderGhostItem object) {
        // 这里需要指向实际的模型文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/armor/dark_rider_ghost.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarkRiderGhostItem object) {
        // 这里需要指向实际的纹理文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/armor/dark_rider_ghost.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DarkRiderGhostItem animatable) {
        // 这里需要指向实际的动画文件路径
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/armor/dark_rider_ghost.animation.json");
    }
}
