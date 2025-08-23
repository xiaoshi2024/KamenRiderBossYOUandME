package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.lemonenergy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.lemon_energy;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LemonEnergyRenderer extends GeoItemRenderer<lemon_energy> {
    // 默认构造函数，使用普通模型
    public LemonEnergyRenderer() {
        super(new LemonEnergyModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"lemon_energy")));
    }

    // 接受自定义模型的构造函数
    private LemonEnergyRenderer(GeoModel<lemon_energy> model) {
        super(model);
    }

    // 为darkLemon变种提供静态工厂方法
    public static GeoItemRenderer<lemon_energy> createDarkLemonRenderer() {
        return new LemonEnergyRenderer(new darkLemonModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"dark_lemo_energy")));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        try {
            // 设置当前渲染的物品栈
            lemon_energy.setCurrentRenderStack(stack);
            
            // 添加特殊渲染效果
            if (transformType == ItemDisplayContext.GUI) {
                poseStack.pushPose();
                poseStack.scale(1.2F, 1.2F, 1.2F); // GUI中稍微放大
                super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
                poseStack.popPose();
            } else {
                super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
            }
        } finally {
            // 确保在渲染完成后清除物品栈
            lemon_energy.clearCurrentRenderStack();
        }
    }
}