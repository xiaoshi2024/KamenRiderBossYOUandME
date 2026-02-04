package com.xiaoshi2022.kamen_rider_boss_you_and_me.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.BrainEyeglss.BrainEyeglassRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item.Mega_uiorderRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.braindriver.BrainDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.builddriver.BuildDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.drakkivabelt.DrakKivaBeltRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver.GenesisDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.ghostdriver.GhostDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.knightinvoker.KnightInvokerBuckleRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver.sengokudrivers_epmtysRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.two_sidriver.Two_sidriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.weekendriver.WeekEndriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.BrainEyeglass;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class GenericCurioRenderer implements ICurioRenderer {
    //饰品道具
    private final ICurioRenderer braineyeglassRenderer = new BrainEyeglassRenderer();
    //变身器
    private final ICurioRenderer megaUiorderRenderer = new Mega_uiorderRenderer();
    private final ICurioRenderer sengokuDriverRenderer = new sengokudrivers_epmtysRenderer();
    private final ICurioRenderer genesisDriverRenderer = new GenesisDriverRenderer();
    private final ICurioRenderer drakKivaBeltRenderer = new DrakKivaBeltRenderer();
    private final ICurioRenderer two_sidriverRenderer = new Two_sidriverRenderer();
    private final ICurioRenderer ghost_driverRenderer = new GhostDriverRenderer();
    private final ICurioRenderer brain_driverRenderer = new BrainDriverRenderer();
    private final ICurioRenderer knightInvokerBuckleRenderer = new KnightInvokerBuckleRenderer();
    private final ICurioRenderer weekendriverRenderer = new WeekEndriverRenderer();
    private final ICurioRenderer buildriverRenderer = new BuildDriverRenderer();


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
            }else if (stack.getItem() instanceof Two_sidriver) {
                two_sidriverRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }else if (stack.getItem() instanceof GhostDriver) {
                ghost_driverRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }else if (stack.getItem() instanceof BrainDriver) {
                brain_driverRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }else if (stack.getItem() instanceof KnightInvokerBuckle) {
                knightInvokerBuckleRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }else if (stack.getItem() instanceof WeekEndriver) {
                weekendriverRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }else if (stack.getItem() instanceof BuildDriver) {
                buildriverRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }

            else if (stack.getItem() instanceof BrainEyeglass) {
                braineyeglassRenderer.render(stack, slotContext, matrixStack, renderLayerParent, buffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }
    }
}