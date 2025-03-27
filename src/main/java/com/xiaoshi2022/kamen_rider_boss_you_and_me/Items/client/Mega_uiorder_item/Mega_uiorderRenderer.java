package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class Mega_uiorderRenderer extends GeoItemRenderer<Mega_uiorder> implements ICurioRenderer {
    private final GeoModel<Mega_uiorder> defaultModel = new Mega_uiorderModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "mega_uiorder_item"));
    private final GeoModel<Mega_uiorder> necrom_eyeModel = new meganecromxModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "mega_uiorder_item"));
    private final GeoModel<Mega_uiorder> xwModel = new megauiorderxwModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "mega_uiorder_item")); // 新增 XW 模型
    public Mega_uiorderRenderer() {
        super(new Mega_uiorderModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "mega_uiorder_item")));
    }

    @Override
    public GeoModel<Mega_uiorder> getGeoModel() {
        // 获取当前 ItemStack
        ItemStack stack = getCurrentItemStack();

        if (stack.getItem() instanceof Mega_uiorder) {
            Mega_uiorder mega = (Mega_uiorder) stack.getItem();
            Mega_uiorder.Mode mode = mega.getCurrentMode(stack);

            // 根据模式返回不同的模型
            switch (mode) {
                case NECROM_EYE:
                    return necrom_eyeModel; // 返回幽冥模式模型
                case XW_MODE:
                    return xwModel; // 返回 XW 模式模型
                default:
                    return defaultModel; // 返回默认模式模型
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
            HumanoidModel<LivingEntity> ArmedModel = (HumanoidModel<LivingEntity>) humanoidModel;
            ArmedModel.translateToHand(HumanoidArm.LEFT, poseStack);
            poseStack.scale(1.2F, 1.2F, 1.2F);
            poseStack.translate(0.19F, 0.4F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Direction.UP.getRotation()); // 调整位置，使手环渲染在手腕上
            ItemInHandRenderer renderer = new ItemInHandRenderer(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(), Minecraft.getInstance().getItemRenderer());
            renderer.renderItem(slotContext.entity(), itemStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, multiBufferSource, light);
        }
        poseStack.popPose();
    }
}