package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.dragonfruit;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.Dragonfruit;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DragonfruitModel extends GeoModel<Dragonfruit> {

    @Override
    public ResourceLocation getModelResource(Dragonfruit object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/dragonfruit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Dragonfruit object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/dragonfruit.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Dragonfruit animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/dragonfruit.animation.json");
    }
}