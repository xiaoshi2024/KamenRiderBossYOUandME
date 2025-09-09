package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.batdrakss;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatDarksEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BatDarksRenderer extends GeoEntityRenderer<BatDarksEntity> {

    public BatDarksRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BatDarksModel());
        this.shadowRadius = 0.1F; // 设置极小的阴影
    }
}