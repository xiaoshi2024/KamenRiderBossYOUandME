package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.duke_knight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DukeKnightEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class DukeKnightRenderer extends GeoEntityRenderer<DukeKnightEntity> {
    public DukeKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new DukeKnightModel());   // ← 换成你的 DukeKnightModel

        // 右手持物层
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, DukeKnightEntity entity) {
                if ("rightItem".equals(bone.getName())) {
                    return entity.getMainHandItem();
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, DukeKnightEntity entity) {
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                              DukeKnightEntity entity, MultiBufferSource bufferSource,
                                              float partialTick, int packedLight, int packedOverlay) {
                poseStack.translate(0.1F, 0.2F, 0F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }
}