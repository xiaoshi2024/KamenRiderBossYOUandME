package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.MelonSX;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.melonxEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class melonsxRenderer extends GeoBlockRenderer<melonxEntity> {
    public melonsxRenderer(BlockEntityRendererProvider.Context context) {
        super(new melonxModel());
    }
}
