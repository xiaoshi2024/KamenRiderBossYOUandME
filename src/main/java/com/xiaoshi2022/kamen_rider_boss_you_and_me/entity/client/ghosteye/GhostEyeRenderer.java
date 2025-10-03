package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.ghosteye;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GhostEyeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GhostEyeRenderer extends GeoEntityRenderer<GhostEyeEntity> {

    public GhostEyeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GhostEyeModel());
        this.shadowRadius = 0.3F; // 设置阴影大小
    }
}