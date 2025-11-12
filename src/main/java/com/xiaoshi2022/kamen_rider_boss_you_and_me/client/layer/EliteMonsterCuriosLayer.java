package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class EliteMonsterCuriosLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final ItemRenderer itemRenderer;

    public EliteMonsterCuriosLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn,
                       T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        // 检查是否是EliteMonsterNpc类型
        if (!(entity instanceof EliteMonsterNpc)) {
            return;
        }
        
        // 检查是否装备了腰带（在Curio槽、主手或副手）或者处于变身状态
        EliteMonsterNpc eliteMonster = (EliteMonsterNpc) entity;
        boolean hasBeltAnywhere = BeltCurioIntegration.hasBeltInCurioSlot(entity) || 
                                BeltCurioIntegration.isBeltItem(eliteMonster.getMainHandItem()) || 
                                BeltCurioIntegration.isBeltItem(eliteMonster.getOffhandItem());
        
        if (!hasBeltAnywhere && !eliteMonster.isTransformed()) {
            return;
        }

        // 尝试获取腰带：优先从Curio槽，然后检查主手和副手
        Optional<ItemStack> beltOpt = BeltCurioIntegration.getBeltFromEntity(entity);
        if (beltOpt.isPresent()) {
            ItemStack beltStack = beltOpt.get();
            renderBelt(entity, beltStack, matrixStack, buffer, packedLightIn);
        }
    }

    /**
     * 渲染腰带物品 - 专门针对EliteMonsterNpc的版本
     */
    private void renderBelt(T entity, ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer,
                            int packedLightIn) {
        matrixStack.pushPose();

        // 获取模型并调整姿势以适配EliteMonster模型
        M model = this.getParentModel();

        try {
            // 处理HumanoidModel类型的模型（EliteMonsterNpc可能使用类似的模型结构）
            if (model instanceof HumanoidModel<?> humanoidModel) {
                HumanoidModel<LivingEntity> humanoid = (HumanoidModel<LivingEntity>) humanoidModel;

                // 将模型移动到身体位置
                humanoid.body.translateAndRotate(matrixStack);

                // EliteMonsterNpc的特殊调整 - 基于实体大小和形态
                float scale = getEliteMonsterScale(entity);
                matrixStack.scale(0.35F * scale, 0.45F * scale, 0.35F * scale);
                matrixStack.translate(0.0F, 1.3F / scale, -0.42F);

                // 旋转调整
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            // 通用处理
            else {
                ModelPart bodyPart = findBodyPart(model);
                if (bodyPart != null) {
                    bodyPart.translateAndRotate(matrixStack);
                    matrixStack.scale(0.38F, 0.48F, 0.38F);
                    matrixStack.translate(0.0F, 1.0F, -0.3F);
                    matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                } else {
                    // 如果无法找到合适的模型部分，使用默认的腰部位置变换
                    applyGenericBeltTransform(matrixStack);
                }
            }

            // 根据腰带类型进行微调
            applyBeltSpecificAdjustments(stack, matrixStack);

        } catch (Exception e) {
            // 如果发生任何错误，使用安全的默认变换
            applyGenericBeltTransform(matrixStack);
        }

        // 渲染物品 - 使用FIXED显示上下文
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                packedLightIn, OverlayTexture.NO_OVERLAY, matrixStack, buffer, entity.level(), 0);

        matrixStack.popPose();
    }

    /**
     * 获取EliteMonsterNpc的缩放比例
     */
    private float getEliteMonsterScale(LivingEntity entity) {
        // EliteMonsterNpc可能有不同的大小，这里可以根据实体类型或状态调整
        if (entity instanceof EliteMonsterNpc) {
            // 可以根据变身状态调整缩放
            if (((EliteMonsterNpc) entity).isTransformed()) {
                return 1.0F; // 变身后的标准大小
            }
        }
        return 1.0F; // 默认缩放
    }

    /**
     * 查找模型的body部分
     */
    private ModelPart findBodyPart(EntityModel<?> model) {
        // 尝试常见的body字段名称
        String[] possibleFieldNames = {"body", "torso", "chest", "upperBody"};

        for (String fieldName : possibleFieldNames) {
            try {
                Field field = model.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object part = field.get(model);
                if (part instanceof ModelPart) {
                    return (ModelPart) part;
                }
            } catch (Exception e) {
                // 继续尝试下一个字段名
            }
        }

        return null;
    }

    /**
     * 应用通用的腰带变换
     */
    private void applyGenericBeltTransform(PoseStack matrixStack) {
        matrixStack.scale(0.4F, 0.5F, 0.4F);
        matrixStack.translate(0.0F, 0.9F, -0.25F);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
    }

    /**
     * 根据腰带类型应用特定的调整
     */
    private void applyBeltSpecificAdjustments(ItemStack stack, PoseStack matrixStack) {
        if (stack.getItem() instanceof Genesis_driver) {
            matrixStack.translate(0.0D, 0.04D, -0.015D);
            matrixStack.scale(1.08F, 1.08F, 1.08F);
        } else if (stack.getItem() instanceof sengokudrivers_epmty) {
            matrixStack.translate(0.0D, 0.02D, -0.01D);
            matrixStack.scale(1.04F, 1.04F, 1.04F);
        } else if (stack.getItem() instanceof DrakKivaBelt) {
            matrixStack.translate(0.0D, 0.05D, -0.02D);
            matrixStack.scale(1.12F, 1.12F, 1.12F);
        } else if (stack.getItem() instanceof Two_sidriver) {
            matrixStack.translate(0.0D, 0.03D, -0.012D);
            matrixStack.scale(1.06F, 1.06F, 1.06F);
        }
    }
}