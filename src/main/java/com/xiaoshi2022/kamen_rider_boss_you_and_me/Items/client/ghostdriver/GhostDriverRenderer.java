package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.ghostdriver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class GhostDriverRenderer extends GeoItemRenderer<GhostDriver> implements ICurioRenderer {
    private final GeoModel<GhostDriver> defaultModel = new GhostDriverModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "ghost_driver"));
    private final GeoModel<GhostDriver> darkRiderEyeModel = new GhostDriverDarkModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "ghost_driver_dark"));

    public GhostDriverRenderer() {
        super(new GhostDriverModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "ghost_driver")));
    }

    @Override
    public GeoModel<GhostDriver> getGeoModel() {
        ItemStack stack = getCurrentItemStack();
        if (stack.getItem() instanceof GhostDriver) {
            GhostDriver belt = (GhostDriver) stack.getItem();
            GhostDriver.BeltMode mode = belt.getMode(stack);

            switch (mode) {
                case DARK_RIDER_EYE:
                    return darkRiderEyeModel; // 返回黑暗骑士眼形态模型
                default:
                    return defaultModel; // 返回默认形态模型
            }
        }
        return super.getGeoModel(); // 默认返回父类的模型
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // 调用父类的渲染逻辑
        super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        poseStack.pushPose();
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            HumanoidModel<LivingEntity> model = (HumanoidModel<LivingEntity>) humanoidModel;

            // 将模型移动到身体位置（胸甲位置）
            model.body.translateAndRotate(poseStack);

            // 调整位置和旋转，使其看起来像胸甲
            poseStack.scale(0.5F, 0.5F, 0.4F); // 可以根据需要调整缩放
            poseStack.translate(0.0F, 1.2, -0.4); // 微调位置使其居中

            // 如果需要，可以添加旋转
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F)); // 如果需要旋转物品

            // 渲染物品
            ItemInHandRenderer renderer = new ItemInHandRenderer(
                    Minecraft.getInstance(),
                    Minecraft.getInstance().getEntityRenderDispatcher(),
                    Minecraft.getInstance().getItemRenderer()
            );
            renderer.renderItem(
                    slotContext.entity(),
                    itemStack,
                    ItemDisplayContext.FIXED, // 使用FIXED或THIRD_PERSON_RIGHT_HAND
                    false,
                    poseStack,
                    multiBufferSource,
                    light
            );
        }
        poseStack.popPose();
    }
}