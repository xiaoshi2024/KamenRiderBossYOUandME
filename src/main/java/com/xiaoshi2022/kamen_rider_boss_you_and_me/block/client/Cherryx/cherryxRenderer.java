package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Cherryx;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.cherryxEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class cherryxRenderer extends GeoBlockRenderer<cherryxEntity> {
    public cherryxRenderer(BlockEntityRendererProvider.Context context) {
        super(new cherryxModel());
    }
}

