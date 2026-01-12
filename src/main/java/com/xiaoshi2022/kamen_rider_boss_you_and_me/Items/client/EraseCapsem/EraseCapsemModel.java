package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.EraseCapsem;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class EraseCapsemModel <T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public EraseCapsemModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }

    @Override
    public EraseCapsemModel<T> withAltModel(ResourceLocation altPath) {
        return (EraseCapsemModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/item/erase_capsem.geo.json"));
    }

    @Override
    public EraseCapsemModel<T> withAltAnimations(ResourceLocation altPath) {
        return (EraseCapsemModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/item/erase_capsem.animation.json"));
    }

    @Override
    public EraseCapsemModel<T> withAltTexture(ResourceLocation altPath) {
        return (EraseCapsemModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/item/erase_capsem.png"));
    }
}