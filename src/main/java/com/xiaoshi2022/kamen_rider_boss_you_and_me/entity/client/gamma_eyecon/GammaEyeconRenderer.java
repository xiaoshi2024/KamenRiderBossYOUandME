package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gamma_eyecon;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GammaEyeconEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GammaEyeconRenderer extends GeoEntityRenderer<GammaEyeconEntity> {

    public GammaEyeconRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GammaEyeconModel());
        this.shadowRadius = 0.3F; // 设置阴影大小，与GhostEyeRenderer保持一致
    }
}