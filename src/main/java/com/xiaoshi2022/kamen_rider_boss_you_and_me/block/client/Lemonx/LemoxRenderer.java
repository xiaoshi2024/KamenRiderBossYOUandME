package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Lemonx;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.LemonxEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class LemoxRenderer extends GeoBlockRenderer<LemonxEntity> {
    public LemoxRenderer(BlockEntityRendererProvider.Context context) {
        super(new LemoxModel());
    }
}
