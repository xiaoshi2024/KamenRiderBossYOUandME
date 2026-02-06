package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.builddriver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class BuildDriverRenderer extends GeoItemRenderer<BuildDriver> implements ICurioRenderer {
    /* ---------- 形态模型 ---------- */
    private final BuildDriverModel defaultModel = new BuildDriverModel(BuildDriver.BeltMode.DEFAULT);
    private final BuildDriverModel rtModel = new BuildDriverModel(BuildDriver.BeltMode.RT);
    private final BuildDriverModel rModel = new BuildDriverModel(BuildDriver.BeltMode.R);
    private final BuildDriverModel tModel = new BuildDriverModel(BuildDriver.BeltMode.T);
    private final BuildDriverModel hazardEmptyModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_EMPTY);
    private final BuildDriverModel hazardRtModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_RT);
    private final BuildDriverModel hazardRModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_R);
    private final BuildDriverModel hazardTModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_T);
    private final BuildDriverModel hazardRtMouldModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_RT_MOULD, true);
    
    /* ---------- 变身模型 ---------- */
    private final BuildDriverModel defaultTransformModel = new BuildDriverModel(BuildDriver.BeltMode.DEFAULT, true);
    private final BuildDriverModel rtTransformModel = new BuildDriverModel(BuildDriver.BeltMode.RT, true);
    private final BuildDriverModel rTransformModel = new BuildDriverModel(BuildDriver.BeltMode.R, true);
    private final BuildDriverModel tTransformModel = new BuildDriverModel(BuildDriver.BeltMode.T, true);
    private final BuildDriverModel hazardEmptyTransformModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_EMPTY, true);
    private final BuildDriverModel hazardRtTransformModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_RT, true);
    private final BuildDriverModel hazardRTransformModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_R, true);
    private final BuildDriverModel hazardTTransformModel = new BuildDriverModel(BuildDriver.BeltMode.HAZARD_T, true);

    // 保存当前被渲染的物品栈
    private ItemStack currentItemStack;

    public BuildDriverRenderer() {
        super(new BuildDriverModel());
    }

    /* ---------- 根据腰带模式和状态返回对应模型 ---------- */
    @Override
    public GeoModel<BuildDriver> getGeoModel() {
        if (currentItemStack != null && currentItemStack.getItem() instanceof BuildDriver belt) {
            BuildDriver.BeltMode mode = belt.getMode(currentItemStack);
            boolean isTransforming = belt.getIsTransforming(currentItemStack);
            
            // 根据模式和变身状态返回对应模型
            switch (mode) {
                case RT:
                    return isTransforming ? rtTransformModel : rtModel;
                case R:
                    return isTransforming ? rTransformModel : rModel;
                case T:
                    return isTransforming ? tTransformModel : tModel;
                case HAZARD_EMPTY:
                    return isTransforming ? hazardEmptyTransformModel : hazardEmptyModel;
                case HAZARD_RT:
                    return isTransforming ? hazardRtTransformModel : hazardRtModel;
                case HAZARD_R:
                    return isTransforming ? hazardRTransformModel : hazardRModel;
                case HAZARD_T:
                    return isTransforming ? hazardTTransformModel : hazardTModel;
                case HAZARD_RT_MOULD:
                    return hazardRtMouldModel;
                default:
                    return isTransforming ? defaultTransformModel : defaultModel;
            }
        }
        return super.getGeoModel();
    }

    /* ---------- 物品渲染（手持 / 展示框 / 地面掉落） ---------- */
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 保存当前物品栈
        this.currentItemStack = stack;
        
        super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // 保存当前物品栈
        this.currentItemStack = itemStack;
        
        poseStack.pushPose();
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            HumanoidModel<LivingEntity> model = (HumanoidModel<LivingEntity>) humanoidModel;

            // 将模型移动到身体位置（胸甲位置）
            model.body.translateAndRotate(poseStack);

            // 调整位置和旋转，使其看起来像胸甲
            poseStack.scale(0.5F, 0.5F, 0.5F); // 可以根据需要调整缩放
            poseStack.translate(0.0F, 1.2, -0.2); // 微调位置使其居中

            // 如果需要，可以添加旋转
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F)); // 如果需要旋转物品

            // 使用ItemInHandRenderer渲染物品
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
