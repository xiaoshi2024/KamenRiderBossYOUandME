package com.xiaoshi2022.kamenriderbossyouandme.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.Genesis_driver;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.braindriver.BrainDriverRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.genesisdriver.GenesisDriverRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class BYCurioRenderer implements ICurioRenderer {
    private final ICurioRenderer genesisBeltRenderer = new GenesisDriverRenderer();
    private final ICurioRenderer brainDriverRenderer = new BrainDriverRenderer();

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (renderLayerParent.getModel() instanceof HumanoidModel<?>) {
            if (stack.getItem() instanceof Genesis_driver) {
                genesisBeltRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
            if (stack.getItem() instanceof BrainDriver) {
                brainDriverRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
            // 可以在这里添加其他 Curio 物品的渲染逻辑
        }
    }
}