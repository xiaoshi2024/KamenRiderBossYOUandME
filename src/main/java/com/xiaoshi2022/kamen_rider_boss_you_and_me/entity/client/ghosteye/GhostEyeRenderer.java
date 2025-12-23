package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.ghosteye;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GhostEyeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class GhostEyeRenderer extends GeoEntityRenderer<GhostEyeEntity> {

    public GhostEyeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GhostEyeModel());
        this.shadowRadius = 0.3F; // 设置阴影大小
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, GhostEyeEntity entity) {
                // 绑定右手骨骼（名称需与Blockbench模型一致）
                if ("rightItem".equals(bone.getName())) {
                    return entity.getMainHandItem();
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, GhostEyeEntity entity) {
                // 设置第三人称右手渲染模式
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, GhostEyeEntity entity,
                                              MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                // 微调物品位置（可选）
                poseStack.translate(0.1f, 0.2f, 0); // X/Y/Z偏移
                poseStack.mulPose(Axis.XP.rotationDegrees(-90)); // 旋转
                super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }
}