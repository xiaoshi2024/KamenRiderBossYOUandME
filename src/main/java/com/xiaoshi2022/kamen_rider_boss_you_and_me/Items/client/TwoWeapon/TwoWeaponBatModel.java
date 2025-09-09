package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.TwoWeapon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class TwoWeaponBatModel<T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public TwoWeaponBatModel() {
        super(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "two_weapon_bat"));
    }

    @Override
    protected String subtype() {
        return "item";
    }

    @Override
    public TwoWeaponBatModel<T> withAltModel(ResourceLocation altPath) {
        return (TwoWeaponBatModel<T>) super.withAltModel(
                new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "geo/item/two_weapon_bat.geo.json")
        );
    }

    @Override
    public TwoWeaponBatModel<T> withAltAnimations(ResourceLocation altPath) {
        return (TwoWeaponBatModel<T>) super.withAltAnimations(
                new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "animations/item/two_weapon_bat.animation.json")
        );
    }

    @Override
    public TwoWeaponBatModel<T> withAltTexture(ResourceLocation altPath) {
        return (TwoWeaponBatModel<T>) super.withAltTexture(
                new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "textures/item/two_weapon_bat.png")
        );
    }
}