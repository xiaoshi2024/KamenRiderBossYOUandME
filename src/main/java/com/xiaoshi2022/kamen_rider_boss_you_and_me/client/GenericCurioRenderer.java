package com.xiaoshi2022.kamen_rider_boss_you_and_me.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.drakkivabelt.DrakKivaBeltRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver.GenesisDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver.sengokudrivers_epmtysRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item.Mega_uiorderRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class GenericCurioRenderer implements ICurioRenderer {
    private final ICurioRenderer megaUiorderRenderer = new Mega_uiorderRenderer();
    private final ICurioRenderer sengokuDriverRenderer = new sengokudrivers_epmtysRenderer();
    private final ICurioRenderer genesisDriverRenderer = new GenesisDriverRenderer();
    private final ICurioRenderer drakKivaBeltRenderer = new DrakKivaBeltRenderer();

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (renderLayerParent.getModel() instanceof HumanoidModel<?>) {
            if (stack.getItem() instanceof Mega_uiorder) {
                megaUiorderRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            } else if (stack.getItem() instanceof sengokudrivers_epmty) {
                sengokuDriverRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }else if (stack.getItem() instanceof Genesis_driver) {
                genesisDriverRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            } else if (stack.getItem() instanceof DrakKivaBelt) {
                drakKivaBeltRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }
    }
}