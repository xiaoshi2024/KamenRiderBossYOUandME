package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.noxknight.noxknights;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.noxknight.NoxKnight;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NoxKnightModel extends GeoModel<NoxKnight> {
    @Override
    public ResourceLocation getModelResource(NoxKnight object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/nox_knight.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NoxKnight object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/nox_knight.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NoxKnight animatable) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/nox_knight.animation.json");
    }
}