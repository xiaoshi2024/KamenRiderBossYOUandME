package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.StoriousMonsterBook;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.StoriousMonsterBook;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class StoriousMonsterBookRenderer extends GeoItemRenderer<StoriousMonsterBook> {
    public StoriousMonsterBookRenderer() {
        super(new StoriousMonsterBookModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"storiousmonsterbook")));
//        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
