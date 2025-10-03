package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.orangefruit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.orangefruit;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class orangefruitRenderer extends GeoItemRenderer<orangefruit> {

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        try {
            // 设置当前渲染物品栈
            orangefruit.setCurrentRenderStack(stack);
            // 调用父类方法进行渲染
            super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            // 确保清除当前渲染物品栈
            orangefruit.clearCurrentRenderStack();
        }
    }
    // 默认构造函数，使用普通模型
    public orangefruitRenderer() {
        super(new orangefruitModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"orangefruit")));
    }

    // 接受自定义模型的构造函数
    private orangefruitRenderer(GeoModel<orangefruit> model) {
        super(model);
    }

    // 为darkOrange变种提供静态工厂方法
    public static GeoItemRenderer<orangefruit> createDarkOrangeRenderer() {
        return new orangefruitRenderer(new darkOrangeModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"dark_orange")));
    }
}