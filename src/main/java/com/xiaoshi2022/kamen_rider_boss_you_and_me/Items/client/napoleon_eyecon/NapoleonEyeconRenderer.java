package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.napoleon_eyecon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.NapoleonEyecon;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class NapoleonEyeconRenderer extends GeoItemRenderer<NapoleonEyecon> {
    public NapoleonEyeconRenderer() {
        super(new NapoleonEyeconModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "nb_eye")));
    }
}