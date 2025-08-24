package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.dragon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.DragonfruitBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class dragonfruitRenderer extends GeoBlockRenderer<DragonfruitBlockEntity> {
    public dragonfruitRenderer(BlockEntityRendererProvider.Context context) {
        super(new dragonfruitModel());
    }
}