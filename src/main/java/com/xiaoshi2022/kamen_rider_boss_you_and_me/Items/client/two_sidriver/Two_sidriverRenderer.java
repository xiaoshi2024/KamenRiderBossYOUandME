package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.two_sidriver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
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

public class Two_sidriverRenderer extends GeoItemRenderer<Two_sidriver> implements ICurioRenderer {

    private static final GeoModel<Two_sidriver> DEFAULT_MODEL = new Two_sidriverModel();
    private static final GeoModel<Two_sidriver> BAT_MODEL     = new Two_sidriver_batModel();
    private static final GeoModel<Two_sidriver> X_MODEL       = new Two_sidriver_xModel();

    public Two_sidriverRenderer() {
        super(DEFAULT_MODEL);
    }

    /* 运行时根据形态切换模型+纹理 */
    @Override
    public GeoModel<Two_sidriver> getGeoModel() {
        ItemStack stack = getCurrentItemStack();
        return switch (Two_sidriver.getDriverType(stack)) {
            case BAT   -> BAT_MODEL;
            case X     -> X_MODEL;
            default    -> DEFAULT_MODEL;
        };
    }

    @Override
    public ResourceLocation getTextureLocation(Two_sidriver animatable) {
        ItemStack stack = getCurrentItemStack();
        return switch (Two_sidriver.getDriverType(stack)) {
            case BAT   -> BAT_MODEL.getTextureResource(animatable);
            case X     -> X_MODEL.getTextureResource(animatable);
            default    -> DEFAULT_MODEL.getTextureResource(animatable);
        };
    }

    @Override
    public void renderByItem(ItemStack stack,
                             ItemDisplayContext transformType,
                             PoseStack poseStack,
                             MultiBufferSource buffer,
                             int packedLight,
                             int packedOverlay) {
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
            poseStack.scale(0.4F, 0.5F, 0.4F); // 可以根据需要调整缩放
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