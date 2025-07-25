package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Another_zi_o_click;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class aiziowcRenderer extends GeoItemRenderer<aiziowc> {

    public aiziowcRenderer() {
        super(new aiziowcModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"aiziowc")));
    }
}

