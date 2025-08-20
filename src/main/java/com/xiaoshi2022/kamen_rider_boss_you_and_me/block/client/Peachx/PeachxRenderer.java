package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Peachx;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.PeachxEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PeachxRenderer extends GeoBlockRenderer<PeachxEntity> {
    public PeachxRenderer(BlockEntityRendererProvider.Context context) {
        super(new PeachxModel());
    }
}