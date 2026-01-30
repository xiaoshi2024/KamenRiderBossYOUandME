package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.weekendriver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.WeekEndriver;
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

public class WeekEndriverRenderer extends GeoItemRenderer<WeekEndriver> implements ICurioRenderer {
    /* ---------- 形态模型 ---------- */
    private final WeekEndriverModel defaultModel = new WeekEndriverModel(WeekEndriver.BeltMode.DEFAULT);
    private final WeekEndriverModel queenBeeModel = new WeekEndriverModel(WeekEndriver.BeltMode.QUEEN_BEE);

    // 保存当前被渲染的物品栈
    private ItemStack currentItemStack;

    public WeekEndriverRenderer() {
        super(new WeekEndriverModel());
    }

    /* ---------- 根据腰带模式返回对应模型 ---------- */
    @Override
    public GeoModel<WeekEndriver> getGeoModel() {
        if (currentItemStack != null && currentItemStack.getItem() instanceof WeekEndriver belt) {
            return switch (belt.getMode(currentItemStack)) {
                case QUEEN_BEE -> queenBeeModel;
                default        -> defaultModel;
            };
        }
        return super.getGeoModel();
    }

    /* ---------- 物品渲染（手持 / 展示框 / 地面掉落） ---------- */
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 保存当前物品栈
        this.currentItemStack = stack;
        
        // 模型模式已经在构造函数中设置，不需要在这里设置
        
        super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // 保存当前物品栈
        this.currentItemStack = itemStack;
        
        // 模型模式已经在构造函数中设置，不需要在这里设置
        
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