package com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.braindriver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.client.model.item.braindriver.BrainDriverModel;
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

public class BrainDriverRenderer extends GeoItemRenderer<BrainDriver> implements ICurioRenderer {
    /* ---------- 模式模型 ---------- */
    private final BrainDriverModel defaultModel = new BrainDriverModel(BrainDriver.BeltMode.DEFAULT);
    private final BrainDriverModel brainModel = new BrainDriverModel(BrainDriver.BeltMode.BRAIN);
    
    // 保存当前被渲染的物品栈
    private ItemStack currentItemStack;
    
    public BrainDriverRenderer() {
        super(new BrainDriverModel());
    }
    
    /* ---------- 根据腰带模式返回对应模型 ---------- */
    @Override
    public GeoModel<BrainDriver> getGeoModel() {
        if (currentItemStack != null && currentItemStack.getItem() instanceof BrainDriver belt) {
            BrainDriver.BeltMode mode = belt.getCurrentMode(currentItemStack);
            
            // 根据模式返回对应模型
            if (mode == BrainDriver.BeltMode.BRAIN) {
                return brainModel;
            }
            return defaultModel;
        }
        return super.getGeoModel();
    }
    
    @Override
    public void renderByItem(ItemStack stack, 
                             ItemDisplayContext transformType, 
                             PoseStack poseStack, 
                             MultiBufferSource buffer, 
                             int packedLight, 
                             int packedOverlay) {
        // 保存当前物品栈
        this.currentItemStack = stack;
        super.renderByItem(stack, transformType, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // 保存当前物品栈
        this.currentItemStack = itemStack;
        
        poseStack.pushPose();
        if (renderLayerParent.getModel() instanceof HumanoidModel) {
            @SuppressWarnings("unchecked")
            HumanoidModel<T> model = (HumanoidModel<T>) renderLayerParent.getModel();

            // 将模型移动到身体位置（胸甲位置）
            model.body.translateAndRotate(poseStack);

            // 调整位置和旋转，使其看起来像胸甲
            poseStack.scale(0.5F, 0.5F, 0.5F); // 可以根据需要调整缩放
            poseStack.translate(0.0F, 1.2, -0.2); // 微调位置使其居中 1.2上下，-0.2前后

            // 旋转物品
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

            // 渲染物品
            ItemInHandRenderer renderer = new ItemInHandRenderer(
                    Minecraft.getInstance(),
                    Minecraft.getInstance().getEntityRenderDispatcher(),
                    Minecraft.getInstance().getItemRenderer()
            );
            renderer.renderItem(
                    slotContext.entity(),
                    itemStack,
                    ItemDisplayContext.FIXED,
                    false,
                    poseStack,
                    multiBufferSource,
                    light
            );
        }
        poseStack.popPose();
    }
}
