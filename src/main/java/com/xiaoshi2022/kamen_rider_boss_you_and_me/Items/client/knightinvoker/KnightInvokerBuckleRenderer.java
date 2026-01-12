package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.knightinvoker;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.KnightInvokerBuckle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class KnightInvokerBuckleRenderer extends GeoItemRenderer<KnightInvokerBuckle> implements ICurioRenderer {

    /* ---------- 形态模型 ---------- */
    private final KnightInvokerBuckleModel emptyModel = new KnightInvokerBuckleModel(
            new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "knight_invoker_buckle"), true);
    private final KnightInvokerBuckleModel noxModel = new KnightInvokerBuckleModel(
            new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "knight_invoker_buckle"), false);

    // 保存当前被渲染的实体
    private LivingEntity currentEntity;

    public KnightInvokerBuckleRenderer() {
        super(new KnightInvokerBuckleModel(new ResourceLocation(kamen_rider_boss_you_and_me.MODID, "knight_invoker_buckle"), true));
    }

    /* ---------- 根据腰带模式返回对应模型 ---------- */
    @Override
    public GeoModel<KnightInvokerBuckle> getGeoModel() {
        ItemStack stack = getCurrentItemStack();
        if (stack.getItem() instanceof KnightInvokerBuckle belt) {
            return switch (belt.getMode(stack)) {
                case NOX -> noxModel;
                default     -> emptyModel;
            };
        }
        return super.getGeoModel();
    }

    /* ---------- 虚化状态下使用透明渲染 ---------- */
    @Override
    public RenderType getRenderType(KnightInvokerBuckle animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        // 使用保存的实体
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    /* ---------- 物品渲染（手持 / 展示框 / 地面掉落） ---------- */
    @Override
    public void renderByItem(ItemStack stack, 
                             ItemDisplayContext transformType, 
                             PoseStack poseStack, 
                             MultiBufferSource buffer, 
                             int packedLight, 
                             int packedOverlay) {
        // 对于物品渲染，我们无法获取实体，所以使用本地玩家
        this.currentEntity = Minecraft.getInstance().player;
        super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
    }

    /* ---------- Curio 渲染（佩戴在身体上） ---------- */
    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // 保存当前被渲染的实体
        this.currentEntity = slotContext.entity();
        poseStack.pushPose();
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            HumanoidModel<LivingEntity> model = (HumanoidModel<LivingEntity>) humanoidModel;

            // 将模型移动到身体位置（胸甲位置）
            model.body.translateAndRotate(poseStack);

            // 调整位置和旋转，使其看起来像胸甲
            poseStack.scale(0.5F, 0.6F, 0.5F); // 可以根据需要调整缩放
            poseStack.translate(0.0F, 0.5, -0.3); // 微调位置使其居中

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