package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.orange_lemons;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.OrangelsxEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class OrangelsxRenderer extends GeoBlockRenderer<OrangelsxEntity> {
    public OrangelsxRenderer(BlockEntityRendererProvider.Context context) {
        super(new OrangelsxModel());
    }
}
