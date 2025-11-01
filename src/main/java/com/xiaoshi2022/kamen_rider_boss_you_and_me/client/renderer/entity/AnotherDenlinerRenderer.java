package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.AnotherDenlinerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AnotherDenlinerRenderer extends GeoEntityRenderer<AnotherDenlinerEntity> {

    public AnotherDenlinerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AnotherDenlinerModel());
        this.shadowRadius = 2.0F;
    }

    @Override
    public void render(AnotherDenlinerEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - entityYaw));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}