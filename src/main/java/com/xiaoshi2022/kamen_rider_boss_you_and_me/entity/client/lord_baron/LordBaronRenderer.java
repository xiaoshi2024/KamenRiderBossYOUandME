package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.lord_baron;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class LordBaronRenderer extends GeoEntityRenderer<LordBaronEntity> {
    public LordBaronRenderer(EntityRendererProvider.Context context) {
        super(context, new LordBaronModel());

        // 仅添加基础物品渲染层
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, LordBaronEntity entity) {
                // 仅绑定rightItem骨骼（与Blockbench模型一致）
                return "rightItem".equals(bone.getName()) ? entity.getMainHandItem() : null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, LordBaronEntity entity) {
                // 固定使用第三人称右手渲染模式
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, LordBaronEntity entity,
                                              MultiBufferSource bufferSource, float partialTick,
                                              int packedLight, int packedOverlay) {
                // 基础位置调整（根据模型需要修改）
                poseStack.translate(0.1f, 0.2f, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public void renderFinal(PoseStack poseStack, LordBaronEntity animatable, BakedGeoModel model,
                            MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        // 保留基础渲染逻辑
        super.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}