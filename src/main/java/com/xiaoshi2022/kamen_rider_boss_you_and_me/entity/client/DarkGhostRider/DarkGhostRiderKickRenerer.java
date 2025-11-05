package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.DarkGhostRider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DarkGhostRiderKickEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DarkGhostRiderKickRenerer extends GeoEntityRenderer<DarkGhostRiderKickEntity> {

    public DarkGhostRiderKickRenerer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DarkGhostRiderKickModel());
        this.shadowRadius = 0.3F; // 设置阴影大小
    }

    @Override
    public void render(DarkGhostRiderKickEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 关键修复：在渲染前检查实体是否为null
        if (entity == null || entity.isRemoved()) {
            return;
        }

        // 在渲染之前设置模型的缩放比例
        poseStack.scale(2.0F, 2.0F, 2.0F); // 放大模型2倍
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean shouldRender(DarkGhostRiderKickEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double camX, double camY, double camZ) {
        // 即使距离较远，也要确保只有在实体有效时才渲染
        return entity != null && !entity.isRemoved() && super.shouldRender(entity, frustum, camX, camY, camZ);
    }
}