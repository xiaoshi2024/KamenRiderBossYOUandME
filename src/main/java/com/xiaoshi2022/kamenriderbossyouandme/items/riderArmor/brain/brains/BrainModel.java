package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.brain.brains;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.brain.Brain;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BrainModel extends GeoModel<Brain> {
    @Override
    public ResourceLocation getAnimationResource(Brain object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/brain_rider.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Brain object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/brain_rider.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Brain object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/brain_rider.png");
    }
}