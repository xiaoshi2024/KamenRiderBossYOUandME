package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.lemonenergy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.bananafruit.bananafruitModel;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.lemon_energy;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LemonEnergyRenderer extends GeoItemRenderer<lemon_energy> {
    public LemonEnergyRenderer() {
        super(new LemonEnergyModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"lemon_energy")));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 添加特殊渲染效果
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.pushPose();
            poseStack.scale(1.2F, 1.2F, 1.2F); // GUI中稍微放大
            super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        } else {
            super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
        }
    }
}