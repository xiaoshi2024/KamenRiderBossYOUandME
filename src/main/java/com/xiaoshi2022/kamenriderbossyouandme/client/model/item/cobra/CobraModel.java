package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.cobra;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.Cobra;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CobraModel extends GeoModel<Cobra> {

    @Override
    public ResourceLocation getModelResource(Cobra object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/cobra.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Cobra object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/cobra.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Cobra animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/cobra.animation.json");
    }
}