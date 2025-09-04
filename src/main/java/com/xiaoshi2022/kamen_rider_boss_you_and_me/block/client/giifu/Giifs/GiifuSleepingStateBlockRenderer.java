package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.Giifs;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.GiifuSleepingStateBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GiifuSleepingStateBlockRenderer extends GeoBlockRenderer<GiifuSleepingStateBlockEntity> {
    public GiifuSleepingStateBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new GiifuSleepingStateBlockModel());
    }
}