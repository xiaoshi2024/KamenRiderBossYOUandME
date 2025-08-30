package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas.thronex;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas.ThroneBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ThroneBlockRenderer extends GeoBlockRenderer<ThroneBlockEntity> {
    public ThroneBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new ThroneBlockModel());
    }
}
