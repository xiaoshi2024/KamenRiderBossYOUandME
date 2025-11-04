package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.darkRider_eyecon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.DarkRiderEyecon;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DarkRiderEyeconRenderer extends GeoItemRenderer<DarkRiderEyecon> {
    public DarkRiderEyeconRenderer() {
        super(new DarkRiderEyeconModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "dark_rider_eye")));
    }
}
