package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.KivatBatTwoNdm;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.KivatBatTwoNdItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class KivatBatTwoItemRenderer extends GeoItemRenderer<KivatBatTwoNdItem> {
    public KivatBatTwoItemRenderer() {
        super(new kivatBatTwoNdmModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "kivat_bat_two_ndm")));
    }
}
