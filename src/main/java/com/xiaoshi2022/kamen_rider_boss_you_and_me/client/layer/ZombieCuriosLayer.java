package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ZombieCuriosLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final ItemRenderer itemRenderer;

    public ZombieCuriosLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn,
                       T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        // 使用BeltCurioIntegration的isZombieType方法来检查是否是僵尸类型
        if (!BeltCurioIntegration.isZombieType(entity)) {
            return;
        }

        // 尝试从Curios槽位获取腰带
        Optional<ItemStack> beltOpt = BeltCurioIntegration.getBeltFromCurioSlot(entity);
        if (beltOpt.isPresent()) {
            ItemStack beltStack = beltOpt.get();
            renderBelt(entity, beltStack, matrixStack, buffer, packedLightIn);
        }
    }

    /**
     * 渲染腰带物品 - 修复版
     */
    private void renderBelt(T entity, ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer,
                            int packedLightIn) {
        matrixStack.pushPose();

        // 获取模型并调整姿势以适配僵尸模型
        M model = this.getParentModel();

        try {
            // 优先使用HumanoidModel的处理方式，参考sengokudrivers_epmtysRenderer
            if (model instanceof HumanoidModel<?> humanoidModel) {
                HumanoidModel<LivingEntity> humanoid = (HumanoidModel<LivingEntity>) humanoidModel;

                // 将模型移动到身体位置（参考sengokudrivers_epmtysRenderer）
                humanoid.body.translateAndRotate(matrixStack);

                // 基础缩放和位置调整
                matrixStack.scale(0.4F, 0.5F, 0.4F); // 使用更合适的缩放
                matrixStack.translate(0.0F, 1.2F, -0.4F); // 参考sengokudrivers_epmtysRenderer的位置

                // 旋转调整
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

            }
            // 处理原版僵尸模型
            else if (model instanceof net.minecraft.client.model.ZombieModel<?>) {
                net.minecraft.client.model.ZombieModel<?> zombieModel = (net.minecraft.client.model.ZombieModel<?>) model;

                // 定位到僵尸的腰部位置
                zombieModel.body.translateAndRotate(matrixStack);

                // 调整位置和缩放，使其更适合僵尸模型
                matrixStack.scale(0.5F, 0.6F, 0.5F); // 调整缩放比例
                matrixStack.translate(0.0F, 1.1F, -0.35F); // 调整位置

                // 旋转调整
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            } else {
                // 对于其他模型类型，尝试访问body部分
                try {
                    java.lang.reflect.Field bodyField = model.getClass().getDeclaredField("body");
                    bodyField.setAccessible(true);
                    Object body = bodyField.get(model);
                    if (body instanceof net.minecraft.client.model.geom.ModelPart) {
                        ((net.minecraft.client.model.geom.ModelPart) body).translateAndRotate(matrixStack);

                        // 通用调整
                        matrixStack.scale(0.45F, 0.55F, 0.45F);
                        matrixStack.translate(0.0F, 1.15F, -0.38F);
                        matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                    }
                } catch (Exception e) {
                    // 如果无法找到body字段，使用默认的腰部位置变换
                    matrixStack.scale(0.5F, 0.6F, 0.5F);
                    matrixStack.translate(0.0F, 1.1F, -0.35F);
                    matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                }
            }

            // 根据腰带类型进行微调 - 使用更精细的调整
            if (stack.getItem() instanceof Genesis_driver) {
                matrixStack.translate(0.0D, 0.05D, -0.02D);
                matrixStack.scale(1.1F, 1.1F, 1.1F);
            } else if (stack.getItem() instanceof sengokudrivers_epmty) {
                matrixStack.translate(0.0D, 0.03D, -0.01D);
                matrixStack.scale(1.05F, 1.05F, 1.05F);
            } else if (stack.getItem() instanceof DrakKivaBelt) {
                matrixStack.translate(0.0D, 0.06D, -0.03D);
                matrixStack.scale(1.15F, 1.15F, 1.15F);
            } else if (stack.getItem() instanceof Two_sidriver) {
                matrixStack.translate(0.0D, 0.04D, -0.015D);
                matrixStack.scale(1.08F, 1.08F, 1.08F);
            } else if (stack.getItem() instanceof GhostDriver) {
                matrixStack.translate(0.0D, 0.035D, -0.012D);
                matrixStack.scale(1.06F, 1.06F, 1.06F);
            }
        } catch (Exception e) {
            // 如果发生任何错误，使用改进的默认变换
            matrixStack.scale(0.5F, 0.6F, 0.5F);
            matrixStack.translate(0.0F, 1.1F, -0.35F);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }

        // 渲染物品 - 使用FIXED显示上下文
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                packedLightIn, OverlayTexture.NO_OVERLAY, matrixStack, buffer, entity.level(), 0);

        matrixStack.popPose();
    }
}