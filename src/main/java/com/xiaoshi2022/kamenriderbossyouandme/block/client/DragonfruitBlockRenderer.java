package com.xiaoshi2022.kamenriderbossyouandme.block.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DragonfruitBlockRenderer extends GeoBlockRenderer<DragonfruitBlockEntity> {
    public DragonfruitBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new DragonfruitBlockModel());
    }
}