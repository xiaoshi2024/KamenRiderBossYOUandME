package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.kivas;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ThroneItemRenderer extends GeoItemRenderer<ThroneBlockItem> {

    public ThroneItemRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(
                kamen_rider_boss_you_and_me.MODID,
                "throne_item" // 对应模型路径
        )));
    }

    // 可选：自定义渲染设置
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 调整手持模型的大小和位置
        poseStack.pushPose();

        // 根据不同的显示上下文调整
        switch (transformType) {
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.translate(0, 0.5f, 0);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                poseStack.scale(0.4f, 0.4f, 0.4f);
                poseStack.translate(0, 0.8f, 0);
            }
            case GUI -> {
                poseStack.scale(0.6f, 0.6f, 0.6f);
                poseStack.translate(0, 0.2f, 0);
            }
            case GROUND -> {
                poseStack.scale(0.3f, 0.3f, 0.3f);
            }
        }

        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }
}