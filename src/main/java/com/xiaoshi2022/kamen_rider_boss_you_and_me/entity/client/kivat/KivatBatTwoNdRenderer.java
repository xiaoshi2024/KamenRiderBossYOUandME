package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.kivat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KivatBatTwoNdRenderer extends GeoEntityRenderer<KivatBatTwoNd> {

    public KivatBatTwoNdRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KivatBatTwoNdModel());
        // 如果模型很小，可以调阴影大小（默认 0.5F）
        this.shadowRadius = 0.3F;
    }

    /* 可选：动态缩放、旋转、摆 pose */
    @Override
    public void render(KivatBatTwoNd entity,
                       float entityYaw,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight) {

        float scale = 1.8F;
        poseStack.pushPose();   // ← 自己再包一层，确保对称
        poseStack.scale(scale, scale, scale);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();    // ← 必须每次都有
    }
}
