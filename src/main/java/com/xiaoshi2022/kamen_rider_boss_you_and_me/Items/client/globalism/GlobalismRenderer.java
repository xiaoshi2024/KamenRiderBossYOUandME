package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.globalism;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.Globalism;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GlobalismRenderer  extends GeoItemRenderer<Globalism> {
    public GlobalismRenderer() {
        super(new GlobalismModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "globalism")));
    }
}
