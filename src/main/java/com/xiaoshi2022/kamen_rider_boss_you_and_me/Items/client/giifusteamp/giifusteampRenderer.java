package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.giifusteamp;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.giifusteamp;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class giifusteampRenderer extends GeoItemRenderer<giifusteamp> {
    public giifusteampRenderer() {
        super(new giifusteampModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"giifusteamp")));
//        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
