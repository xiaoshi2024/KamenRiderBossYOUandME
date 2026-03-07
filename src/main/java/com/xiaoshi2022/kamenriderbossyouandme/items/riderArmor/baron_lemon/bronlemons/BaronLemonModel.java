package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.baron_lemon.bronlemons;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.baron_lemon.BaronLemon;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BaronLemonModel extends GeoModel<BaronLemon> {
    @Override
    public ResourceLocation getAnimationResource(BaronLemon object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/baron_lemon.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BaronLemon object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/baron_lemon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaronLemon object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/baron_lemon.png");
    }
}