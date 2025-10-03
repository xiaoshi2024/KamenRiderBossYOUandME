package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.dragonfruit;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.dragonfruit;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class dragonfruitRenderer extends GeoItemRenderer<dragonfruit> {
    public dragonfruitRenderer() {
        super(new dragonfruitModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "dragonfruit")));
    }
}