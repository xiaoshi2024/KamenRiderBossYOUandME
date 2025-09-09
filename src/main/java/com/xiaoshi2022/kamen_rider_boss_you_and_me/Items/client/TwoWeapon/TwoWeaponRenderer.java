package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.TwoWeapon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TwoWeaponRenderer extends GeoItemRenderer<TwoWeaponItem> {

    private static final GeoModel<TwoWeaponItem> DEFAULT_MODEL =
            new TwoWeaponModel<>(new ResourceLocation("kamen_rider_boss_you_and_me", "two_weapon"));
    private static final GeoModel<TwoWeaponItem> BAT_MODEL     = new TwoWeaponBatModel<>();

    public TwoWeaponRenderer() {
        super(DEFAULT_MODEL);
    }

    @Override
    public GeoModel<TwoWeaponItem> getGeoModel() {
        /* 3. 把 ItemStack 传进去，不然会报“0 个实参” */
        TwoWeaponItem.Variant v = TwoWeaponItem.getVariant(currentItemStack);
        return switch (v) {
            case BAT -> BAT_MODEL;
            default  -> DEFAULT_MODEL;
        };
    }

    @Override
    public ResourceLocation getTextureLocation(TwoWeaponItem animatable) {
        TwoWeaponItem.Variant v = TwoWeaponItem.getVariant(currentItemStack);
        return switch (v) {
            case BAT -> BAT_MODEL.getTextureResource(animatable);
            default  -> DEFAULT_MODEL.getTextureResource(animatable);
        };
    }
}