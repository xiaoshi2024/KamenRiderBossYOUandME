package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.core.object.Color;

import javax.annotation.Nullable;
import java.util.Locale;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ModArmorsRenderProcedure {

    // 处理玩家渲染前的模型隐藏逻辑（避免原生模型与自定义盔甲重叠）
    @SubscribeEvent
    public static void renderPlayerPreEvent(RenderPlayerEvent.Pre event) {
        if (event.getEntity() == null) return;
        Player player = event.getEntity();
        PlayerRenderer renderer = event.getRenderer();
        PlayerModel<AbstractClientPlayer> playerModel = renderer.getModel();

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        // 根据GeoItem类型盔甲隐藏原生模型部位
        playerModel.head.visible = !(head.getItem() instanceof GeoItem);
        playerModel.hat.visible = !(head.getItem() instanceof GeoItem);
        playerModel.body.visible = !(chest.getItem() instanceof GeoItem);
        playerModel.rightArm.visible = !(chest.getItem() instanceof GeoItem);
        playerModel.leftArm.visible = !(chest.getItem() instanceof GeoItem);
        playerModel.leftSleeve.visible = !(chest.getItem() instanceof GeoItem);
        playerModel.rightSleeve.visible = !(chest.getItem() instanceof GeoItem);
        playerModel.jacket.visible = !(chest.getItem() instanceof GeoItem);
        playerModel.rightLeg.visible = !(legs.getItem() instanceof GeoItem || feet.getItem() instanceof GeoItem);
        playerModel.leftLeg.visible = !(legs.getItem() instanceof GeoItem || feet.getItem() instanceof GeoItem);
        playerModel.leftPants.visible = !(legs.getItem() instanceof GeoItem || feet.getItem() instanceof GeoItem);
        playerModel.rightPants.visible = !(legs.getItem() instanceof GeoItem || feet.getItem() instanceof GeoItem);
    }

    // 处理手臂渲染逻辑（优先渲染动画盔甲，再渲染普通盔甲）
    @SubscribeEvent
    public static void renderArmEvent(RenderArmEvent event) {
        if (event.getPlayer() == null) return;
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = event.getPlayer();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = mc.getFrameTime();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        PlayerRenderer playerRenderer = (PlayerRenderer) dispatcher.getRenderer(player);
        PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();

        // 统一判断：是否是本mod的胸甲 + 未开启"renderArmUse"标签
        boolean isModChest = ForgeRegistries.ITEMS.getKey(chest.getItem()).toString().contains("kamen_rider_boss_you_and_me");
        boolean skipRender = chest.getOrCreateTag().getBoolean("renderArmUse");
        if (!isModChest || skipRender) return;

        poseStack.pushPose();
        try {
            // 优先级1：如果是带动画的GeoItem盔甲
            if (chest.getItem() instanceof GeoItem geoItem) {
                GeoArmorRenderer geoRenderer = (GeoArmorRenderer) getArmorModelHook(player, chest, EquipmentSlot.CHEST, playerModel);
                VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(geoRenderer.getTextureLocation(geoItem)));
                BakedGeoModel bakedModel = geoRenderer.getGeoModel().getBakedModel(geoRenderer.getGeoModel().getModelResource(geoItem));

                // 隐藏盔甲其他部位，只显示当前手臂
                SetAllBoneNoVisible(geoRenderer);
                GeoBone armBone = (event.getArm() == HumanoidArm.RIGHT) ? geoRenderer.getRightArmBone() : geoRenderer.getLeftArmBone();
                armBone.setHidden(false);
                armBone.updateRotation(0, 0, 0);
                armBone.updatePosition(0, 0, 0);

                // 渲染动画盔甲手臂
                geoRenderer.actuallyRender(
                        poseStack, (Item) geoItem, bakedModel,
                        RenderType.entityTranslucent(geoRenderer.getTextureLocation(geoItem)),
                        bufferSource, buffer, true, partialTick, packedLight,
                        OverlayTexture.NO_OVERLAY,
                        Color.WHITE.getRed() / 255f, Color.WHITE.getGreen() / 255f,
                        Color.WHITE.getBlue() / 255f, Color.WHITE.getAlpha() / 255f
                );
                event.setCanceled(true); // 取消原生手臂渲染
            }
            // 优先级2：如果是普通ArmorItem盔甲
            else if (chest.getItem() instanceof ArmorItem armorItem) {
                HumanoidModel<?> armorModel = (HumanoidModel<?>) getArmorModelHook(player, chest, EquipmentSlot.CHEST, playerModel);
                ResourceLocation armorTex = getArmorResource(player, chest, EquipmentSlot.CHEST, null);
                VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(armorTex));

                // 配置盔甲模型状态（取消攻击/蹲伏等动画干扰）
                ModelPart armPart = (event.getArm() == HumanoidArm.RIGHT) ? armorModel.rightArm : armorModel.leftArm;
                playerModel.attackTime = 0.0F;
                playerModel.crouching = false;
                playerModel.swimAmount = 0.0F;
                playerModel.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                armPart.xRot = 0.0F;

                // 渲染普通盔甲手臂
                armPart.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
                event.setCanceled(true); // 取消原生手臂渲染
            }
        } finally {
            poseStack.popPose(); // 确保渲染后恢复姿态栈，避免异常
        }
    }

    // 处理手持物品时的手臂渲染逻辑（玩家隐身时生效）
    @SubscribeEvent
    public static void renderHandEvent(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        LocalPlayer player = mc.player;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack mainHandStack = event.getItemStack();
        InteractionHand hand = event.getHand();

        // 统一判断：本mod胸甲 + 未开启"renderArmUse" + 玩家隐身 + 主手为空 + 未瞄准
        boolean isModChest = ForgeRegistries.ITEMS.getKey(chest.getItem()).toString().contains("kamen_rider_boss_you_and_me");
        boolean skipRender = chest.getOrCreateTag().getBoolean("renderArmUse");
        if (!isModChest || skipRender || !player.isInvisible() || hand != InteractionHand.MAIN_HAND || !mainHandStack.isEmpty() || player.isScoping()) {
            return;
        }

        // 渲染原生手臂（适配两种盔甲类型）
        if (chest.getItem() instanceof GeoItem || chest.getItem() instanceof ArmorItem) {
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffSource = event.getMultiBufferSource();
            int packedLight = event.getPackedLight();
            float equipProgress = event.getEquipProgress();
            float swingProgress = event.getSwingProgress();

            poseStack.pushPose();
            renderPlayerArm(poseStack, buffSource, packedLight, equipProgress, swingProgress, player.getMainArm());
            poseStack.popPose();
        }
    }


    // 隐藏Geo盔甲除手臂外的其他部位（内部工具方法）
    private static void SetAllBoneNoVisible(GeoArmorRenderer render) {
        render.getHeadBone().setHidden(true);
        render.getBodyBone().setHidden(true);
        render.getRightLegBone().setHidden(true);
        render.getRightBootBone().setHidden(true);
        render.getLeftLegBone().setHidden(true);
        render.getLeftBootBone().setHidden(true);
    }

    // 获取盔甲模型（适配Forge钩子，内部工具方法）
    private static net.minecraft.client.model.Model getArmorModelHook(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<?> model) {
        return ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
    }

    // 获取普通盔甲的纹理路径（内部工具方法）
    public static ResourceLocation getArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
        ArmorItem item = (ArmorItem) stack.getItem();
        String texture = item.getMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }
        // 构建盔甲纹理路径（符合Minecraft默认规范）
        String texPath = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png",
                domain, texture, 1, (type == null ? "" : "_" + type));
        texPath = ForgeHooksClient.getArmorTexture(entity, stack, texPath, slot, type);
        return new ResourceLocation(texPath);
    }

    // 渲染原生玩家手臂（内部工具方法）
    public static void renderPlayerArm(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                       float equipProgress, float swingProgress, HumanoidArm arm) {
        boolean isRightArm = arm != HumanoidArm.LEFT;
        float sideOffset = isRightArm ? 1.0F : -1.0F;

        // 计算手臂摆动的角度（原生逻辑，确保动作自然）
        float swingSqrt = Mth.sqrt(swingProgress);
        float swingY1 = -0.3F * Mth.sin(swingSqrt * (float) Math.PI);
        float swingY2 = 0.4F * Mth.sin(swingSqrt * (float) Math.PI * 2);
        float swingZ = -0.4F * Mth.sin(swingProgress * (float) Math.PI);

        // 调整手臂姿态（位置和旋转）
        poseStack.translate(sideOffset * (swingY1 + 0.64000005F), swingY2 - 0.6F + equipProgress * -0.6F, swingZ - 0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(sideOffset * 45.0F));

        float swingX1 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float swingX2 = Mth.sin(swingSqrt * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(sideOffset * swingX2 * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(sideOffset * swingX1 * -20.0F));

        // 绑定玩家皮肤纹理并渲染手臂
        AbstractClientPlayer clientPlayer = Minecraft.getInstance().player;
        RenderSystem.setShaderTexture(0, clientPlayer.getSkinTextureLocation());

        poseStack.translate(sideOffset * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(sideOffset * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(sideOffset * -135.0F));
        poseStack.translate(sideOffset * 5.6F, 0.0F, 0.0F);

        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(clientPlayer);
        if (isRightArm) {
            playerRenderer.renderRightHand(poseStack, bufferSource, packedLight, clientPlayer);
        } else {
            playerRenderer.renderLeftHand(poseStack, bufferSource, packedLight, clientPlayer);
        }
    }
}