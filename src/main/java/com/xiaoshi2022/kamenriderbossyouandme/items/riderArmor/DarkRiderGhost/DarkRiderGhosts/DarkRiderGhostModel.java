package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.DarkRiderGhost.DarkRiderGhosts;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.DarkRiderGhost.DarkRiderGhost;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DarkRiderGhostModel extends GeoModel<DarkRiderGhost> {
    @Override
    public ResourceLocation getAnimationResource(DarkRiderGhost object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/dark_rider_ghost.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(DarkRiderGhost object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/dark_rider_ghost.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarkRiderGhost object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/dark_rider_ghost.png");
    }
}