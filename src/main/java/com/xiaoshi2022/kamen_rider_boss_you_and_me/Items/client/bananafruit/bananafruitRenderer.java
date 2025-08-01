package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.bananafruit;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.bananafruit;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class bananafruitRenderer extends GeoItemRenderer<bananafruit> {
    public bananafruitRenderer() {
        super(new bananafruitModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"bananafruit")));
    }
}
