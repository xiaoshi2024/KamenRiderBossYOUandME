package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Necrom_eye;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class Necrom_eyeRenderer extends GeoItemRenderer<Necrom_eye> {
    public Necrom_eyeRenderer() {
        super(new Necrom_eyeModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"necrom_eye")));
    }
}
