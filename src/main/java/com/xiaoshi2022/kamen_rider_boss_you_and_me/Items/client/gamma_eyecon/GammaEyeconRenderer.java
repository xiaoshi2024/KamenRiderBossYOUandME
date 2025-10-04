package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.gamma_eyecon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.GammaEyecon;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GammaEyeconRenderer extends GeoItemRenderer<GammaEyecon> {
    public GammaEyeconRenderer() {
        super(new GammaEyeconModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"gamma_eyecon")));
    }
}