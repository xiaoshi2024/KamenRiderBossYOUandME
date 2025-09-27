package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.batstampfinish;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatStampFinishEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BatStampFinishRenderer extends GeoEntityRenderer<BatStampFinishEntity> {

    public BatStampFinishRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BatStampFinishModel());
        this.shadowRadius = 0.3F; // 设置阴影大小
    }
    @Override
    public void render(BatStampFinishEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 在渲染之前设置模型的缩放比例
        poseStack.scale(4.0F, 4.0F, 4.0F); // 放大模型4倍
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}