package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.property;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.KaizokuItem;

public class KaizokuItemModel extends GeoModel<KaizokuItem> {
    @Override
    public ResourceLocation getModelResource(KaizokuItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:geo/item/kaizoku.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KaizokuItem object) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:textures/item/kaizoku.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KaizokuItem animatable) {
        return new ResourceLocation("kamen_rider_boss_you_and_me:animations/item/kaizoku.animation.json");
    }
}
