package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.EliteMonster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class EliteMonsterArmorLayer extends RenderLayer<EliteMonsterNpc, HumanoidModel<EliteMonsterNpc>> {

    public EliteMonsterArmorLayer(RenderLayerParent<EliteMonsterNpc, HumanoidModel<EliteMonsterNpc>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       EliteMonsterNpc entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // 这里可以添加自定义的盔甲渲染逻辑
        // 比如在变身状态下显示特殊效果等

        if (entity.isTransformed()) {
            // 变身状态下的特殊渲染效果
            // 可以在这里添加发光效果、粒子效果等
        }
    }
}