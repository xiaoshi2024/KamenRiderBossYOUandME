package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.TwoWeapon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class TwoWeaponGunModel<T extends GeoAnimatable> extends DefaultedGeoModel<T> {

    public TwoWeaponGunModel() {
        super(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "two_weapon"));
    }

    @Override
    protected String subtype() {
        return "item";
    }
    
    @Override
    public TwoWeaponGunModel<T> withAltModel(ResourceLocation altPath) {
        return (TwoWeaponGunModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/item/two_weapon.geo.json"));
    }

    @Override
    public TwoWeaponGunModel<T> withAltAnimations(ResourceLocation altPath) {
        return (TwoWeaponGunModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/item/two_weapon.animation.json"));
    }

    @Override
    public TwoWeaponGunModel<T> withAltTexture(ResourceLocation altPath) {
        return (TwoWeaponGunModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/item/two_weapon.png"));
    }
}