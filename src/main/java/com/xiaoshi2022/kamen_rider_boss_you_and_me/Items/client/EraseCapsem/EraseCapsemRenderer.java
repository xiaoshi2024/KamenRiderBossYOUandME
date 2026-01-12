package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.EraseCapsem;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.EraseCapsem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class EraseCapsemRenderer extends GeoItemRenderer<EraseCapsem> {
    public EraseCapsemRenderer() {
        super(new EraseCapsemModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "erase_capsem")));
    }
}