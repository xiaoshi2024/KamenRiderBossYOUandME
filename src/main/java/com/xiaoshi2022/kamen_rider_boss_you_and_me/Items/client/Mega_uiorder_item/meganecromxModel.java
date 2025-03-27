package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class meganecromxModel extends GeoModel<Mega_uiorder> {
    public meganecromxModel(ResourceLocation megaUiorderItem) {
        super();
    }

    @Override
    public ResourceLocation getModelResource(Mega_uiorder animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "geo/item/meganecromx.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Mega_uiorder animatable){
        return new ResourceLocation("kamen_rider_boss_you_and_me", "textures/item/meganecromx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Mega_uiorder animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me", "animations/item/meganecromx.animation.json");
    }
}
