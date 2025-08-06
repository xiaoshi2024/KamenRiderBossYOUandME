package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Bananas;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.BananasEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BananasRenderer extends GeoBlockRenderer<BananasEntity> {
    public BananasRenderer(BlockEntityRendererProvider.Context context) {
        super(new BananasModel());
    }
}