package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.GiifuEyeBall;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.GiifuEyeBall;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GiifuEyeBallRenderer extends GeoItemRenderer<GiifuEyeBall> {
    public GiifuEyeBallRenderer() {
        super(new GiifuEyeBallModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"giifu_eyeball")));
    }
}