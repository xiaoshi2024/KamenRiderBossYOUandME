package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.queenbeestamp;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.QueenBeeStamp;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class QueenBeeStampRenderer extends GeoItemRenderer<QueenBeeStamp> {
    public QueenBeeStampRenderer() {
        super(new QueenBeeStampModel());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
