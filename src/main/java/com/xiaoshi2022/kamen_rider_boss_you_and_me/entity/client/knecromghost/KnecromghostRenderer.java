package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.knecromghost;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.KnecromghostEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KnecromghostRenderer extends GeoEntityRenderer<KnecromghostEntity> {

    public KnecromghostRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KnecromghostModel());
        this.shadowRadius = 0.1F; // 设置极小的阴影
    }
}
