package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.NapoleonGhost.NapoleonGhosts;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.NapoleonGhost.NapoleonGhost;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NapoleonGhostModel extends GeoModel<NapoleonGhost> {
    @Override
    public ResourceLocation getAnimationResource(NapoleonGhost object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/napoleon_ghost.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(NapoleonGhost object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/napoleon_ghost.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NapoleonGhost object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/napoleon_ghost.png");
    }
}