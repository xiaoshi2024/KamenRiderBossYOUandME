package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.BatStamp;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.BatStampItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BatStampRenderer extends GeoItemRenderer<BatStampItem> {
    public BatStampRenderer() {
        super(new BatStampModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "bat_stamp")));
    }
}