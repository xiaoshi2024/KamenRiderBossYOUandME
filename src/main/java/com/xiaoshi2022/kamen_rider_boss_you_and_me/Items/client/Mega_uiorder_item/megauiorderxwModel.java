package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class megauiorderxwModel extends GeoModel<Mega_uiorder> {
    public megauiorderxwModel(ResourceLocation megaUiorder) {
    }

    @Override
    public ResourceLocation getModelResource(Mega_uiorder animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"geo/item/meganecomxw.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Mega_uiorder animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"textures/item/meganecromx.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Mega_uiorder animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"animations/item/mega_uiorder_item.animation.json");
    }
}
