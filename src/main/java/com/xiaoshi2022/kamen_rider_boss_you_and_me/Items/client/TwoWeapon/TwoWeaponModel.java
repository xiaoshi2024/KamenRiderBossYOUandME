package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.TwoWeapon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class TwoWeaponModel  <T extends GeoAnimatable> extends DefaultedGeoModel<T> {

    /**
     * Create a new instance of this model class.<br>
     * The asset path should be the truncated relative path from the base folder.<br>
     * E.G.
     * <pre>
     *     {@code
     * 		new ResourceLocation("myMod", "animals/red_fish")
     *                }</pre>
     *
     * @param assetSubpath
     */
    public TwoWeaponModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }
    @Override
    public TwoWeaponModel<T> withAltModel(ResourceLocation altPath) {
        return (TwoWeaponModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"geo/item/two_weapon.geo.json"));
    }

    /**
     * Changes the constructor-defined animations path for this model to an alternate.<br>
     * This is useful if your animatable shares an animations path with another animatable that differs in path to the model and texture for this model
     */
    @Override
    public TwoWeaponModel<T> withAltAnimations(ResourceLocation altPath) {
        return (TwoWeaponModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"animations/item/two_weapon.animation.json"));
    }

    /**
     * Changes the constructor-defined texture path for this model to an alternate.<br>
     * This is useful if your animatable shares a texture path with another animatable that differs in path to the model and animations for this model
     */
    @Override
    public TwoWeaponModel<T> withAltTexture(ResourceLocation altPath) {
        return (TwoWeaponModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"textures/item/two_weapon.png"));
    }
}