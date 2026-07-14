package com.xiaoshi2022.kamenriderbossyouandme.client.model.item.hazardtrigger;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.items.prop.HazardTrigger;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HazardTriggerModel extends GeoModel<HazardTrigger> {

    @Override
    public ResourceLocation getModelResource(HazardTrigger object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "geo/item/hazard_trigger.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HazardTrigger object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "textures/item/hazard_trigger.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HazardTrigger animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "animations/item/hazard_trigger.animation.json");
    }
}