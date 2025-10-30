package com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerModel;
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
public class VillagerCuriosLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final ItemRenderer itemRenderer;

    public VillagerCuriosLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLightIn,
                       T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        // 检查是否是村民类型
        if (!BeltCurioIntegration.isVillagerType(entity)) {
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
     * 渲染腰带物品 - 专门针对MCA村民的修复版
     */
    private void renderBelt(T entity, ItemStack stack, PoseStack matrixStack, MultiBufferSource buffer,
                            int packedLightIn) {
        matrixStack.pushPose();

        // 获取模型并调整姿势以适配村民模型
        M model = this.getParentModel();

        try {
            // 方法1: 处理MCA村民模型
            if (isMCAModel(model)) {
                // MCA村民使用HumanoidModel结构
                if (model instanceof HumanoidModel<?> humanoidModel) {
                    HumanoidModel<LivingEntity> humanoid = (HumanoidModel<LivingEntity>) humanoidModel;

                    // 将模型移动到身体位置
                    humanoid.body.translateAndRotate(matrixStack);

                    // MCA村民的特殊调整 - 考虑体型缩放
                    float scale = getMCAScale(entity);
                    matrixStack.scale(0.35F * scale, 0.45F * scale, 0.35F * scale);
                    matrixStack.translate(0.0F, 1.3F / scale, -0.42F);

                    // 旋转调整
                    matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                }
            }
            // 方法2: 处理原版村民模型
            else if (model instanceof VillagerModel<?> villagerModel) {
                // 原版村民模型使用root部分
                villagerModel.root().translateAndRotate(matrixStack);

                // 调整位置和缩放，使其更适合村民模型
                matrixStack.scale(0.4F, 0.5F, 0.4F);
                matrixStack.translate(0.0F, 0.8F, -0.2F);

                // 旋转调整
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            // 方法3: 处理其他HumanoidModel
            else if (model instanceof HumanoidModel<?> humanoidModel) {
                HumanoidModel<LivingEntity> humanoid = (HumanoidModel<LivingEntity>) humanoidModel;

                // 将模型移动到身体位置
                humanoid.body.translateAndRotate(matrixStack);

                // 基础缩放和位置调整
                matrixStack.scale(0.35F, 0.45F, 0.35F);
                matrixStack.translate(0.0F, 1.3F, -0.42F);

                // 旋转调整
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            // 方法4: 通用处理
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
     * 检查是否是MCA村民模型
     */
    private boolean isMCAModel(EntityModel<?> model) {
        String className = model.getClass().getName().toLowerCase();
        return className.contains("mca") ||
                className.contains("villagerentitymodel") ||
                model.getClass().getSimpleName().contains("MCA");
    }

    /**
     * 获取MCA村民的缩放比例
     */
    private float getMCAScale(LivingEntity entity) {
        try {
            // 尝试通过反射获取MCA的缩放属性
            Class<?> entityClass = entity.getClass();
            if (entityClass.getName().contains("VillagerEntityMCA")) {
                // 尝试获取MCA的尺寸相关方法
                java.lang.reflect.Method getScaleMethod = entityClass.getMethod("getScale");
                Object scale = getScaleMethod.invoke(entity);
                if (scale instanceof Float) {
                    return (Float) scale;
                }

                // 尝试获取原始缩放因子
                java.lang.reflect.Method getRawScaleFactorMethod = entityClass.getMethod("getRawScaleFactor");
                Object rawScale = getRawScaleFactorMethod.invoke(entity);
                if (rawScale instanceof Float) {
                    return (Float) rawScale;
                }
            }
        } catch (Exception e) {
            // 如果无法获取，返回默认值
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