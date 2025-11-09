package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Another_zi_o_click;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.aiziowc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class aiziowcRenderer extends GeoItemRenderer<aiziowc> {
    private final GeoModel<aiziowc> defaultModel;
    private final GeoModel<aiziowc> anotherModel;
    private final GeoModel<aiziowc> denoModel;
    private final GeoModel<aiziowc> dcdModel;

    public aiziowcRenderer() {
        super(new aiziowcModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"aiziowc")));
        this.defaultModel = new aiziowcModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"aiziowc"));
        // 使用默认模型作为备用，避免找不到模型文件时崩溃
        this.anotherModel = defaultModel;
        this.denoModel = new aiden_owcModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"aiden_owc"));
        this.dcdModel = new aidcdwcModel<>(new ResourceLocation(kamen_rider_boss_you_and_me.MODID,"aidcdwc"));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, 
                           MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 获取当前模式
        aiziowc.Mode mode = ((aiziowc)stack.getItem()).getCurrentMode(stack);

        // 根据模式选择模型
        GeoModel<aiziowc> currentModel;
        if (mode == aiziowc.Mode.ANOTHER) {
            currentModel = anotherModel;
        } else if (mode == aiziowc.Mode.DEN_O) {
            currentModel = denoModel;
        } else if (mode == aiziowc.Mode.DCD) {
            currentModel = dcdModel;
        } else {
            currentModel = defaultModel;
        }

        // 使用反射临时修改模型字段
        try {
            java.lang.reflect.Field modelField = GeoItemRenderer.class.getDeclaredField("model");
            modelField.setAccessible(true);
            modelField.set(this, currentModel);

            // 调用父类渲染
            super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);

            // 恢复默认模型
            modelField.set(this, defaultModel);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果反射失败，使用默认模型渲染
            super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
        }
    }
}

