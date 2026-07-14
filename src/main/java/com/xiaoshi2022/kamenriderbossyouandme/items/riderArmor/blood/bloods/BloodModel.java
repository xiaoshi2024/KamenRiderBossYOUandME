package com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blood.bloods;

import com.xiaoshi2022.kamenriderbossyouandme.items.riderArmor.blood.Blood;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BloodModel extends GeoModel<Blood> {
    @Override
    public ResourceLocation getAnimationResource(Blood object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "animations/item/armor/rider_blood.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Blood object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "geo/item/armor/rider_blood.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Blood object) {
        return ResourceLocation.fromNamespaceAndPath("kamenriderbossyouandme", "textures/item/armor/rider_blood.png");
    }
}