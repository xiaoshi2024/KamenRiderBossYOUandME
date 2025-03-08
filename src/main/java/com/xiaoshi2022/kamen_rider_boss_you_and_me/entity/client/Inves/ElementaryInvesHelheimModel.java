package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Inves;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ElementaryInvesHelheimModel extends GeoModel<ElementaryInvesHelheim> {

    @Override
    public ResourceLocation getModelResource(ElementaryInvesHelheim animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/entity/inves_heilehim.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ElementaryInvesHelheim animatable) {
        String variant = animatable.getVariant();
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/entity/inves_heilehim_" + variant + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(ElementaryInvesHelheim animatable) {
        return new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/entity/inves_heilehim.animation.json");
    }
}
