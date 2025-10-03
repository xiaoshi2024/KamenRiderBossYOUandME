package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.dark_kiva_seal_barrier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkKivaSealBarrierEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DarkKivaSealBarrierRenderer extends GeoEntityRenderer<DarkKivaSealBarrierEntity> {
    public DarkKivaSealBarrierRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DarkKivaSealBarrierModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(DarkKivaSealBarrierEntity entity, float entityYaw, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight) {
        // 可以在这里添加额外的渲染逻辑，比如根据实体状态调整渲染参数
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}