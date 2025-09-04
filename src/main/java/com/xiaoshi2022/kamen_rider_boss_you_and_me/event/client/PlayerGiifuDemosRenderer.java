//package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.client;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.GiifuDems.GiifuDemosModel;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.GiifuDems.GiifuDemosRenderer;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
//import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.client.event.RenderPlayerEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import tocraft.walkers.api.PlayerShape;
//
//@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
//public class PlayerGiifuDemosRenderer {
//    private static final GiifuDemosModel model = new GiifuDemosModel();
//    private static final ResourceLocation texture = new ResourceLocation("kamen_rider_boss_you_and_me", "textures/entity/giifudemos.png");
//
//    @SubscribeEvent
//    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
//        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
//
////        // 检查玩家是否正在进行基夫变异
////        if (player instanceof PlayerExpand && ((PlayerExpand) player).isUndergoingGiifuMutation()) {
////            // 取消默认的玩家渲染
////            event.setCanceled(true);
////
////            // 渲染变异动画效果
////            renderGiifuMutationAnimation(player, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick());
////        }
//        // 检查玩家是否变身为基夫德莫斯
//       if (PlayerShape.getCurrentShape(player) instanceof GiifuDemosEntity) {
//            // 取消默认的玩家渲染
//            event.setCanceled(true);
//
//            // 渲染基夫德莫斯模型
//            renderGiifuDemosModel(player, (GiifuDemosEntity) PlayerShape.getCurrentShape(player), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick());
//        }
//    }
//
//    /**
//     * 渲染玩家的基夫变异动画效果
//     */
//    private static void renderGiifuMutationAnimation(AbstractClientPlayer player, PoseStack poseStack,
//                                                     MultiBufferSource bufferSource, int packedLight, float partialTick) {
//        poseStack.pushPose();
//
//        // 获取 Minecraft 客户端实例
//        Minecraft minecraft = Minecraft.getInstance();
//
//        // 构造 EntityRendererProvider.Context 对象
//        EntityRendererProvider.Context context = new EntityRendererProvider.Context(
//                minecraft.getEntityRenderDispatcher(),
//                minecraft.getItemRenderer(),
//                minecraft.getBlockRenderer(),
//                minecraft.getEntityRenderDispatcher().getItemInHandRenderer(),
//                minecraft.getResourceManager(),
//                minecraft.getEntityModels(),
//                minecraft.font
//        );
//
//        // 创建基夫德莫斯实体和渲染器用于动画
//        // 使用ModEntityTypes创建实体
//        GiifuDemosEntity tempEntity = ModEntityTypes.GIIFUDEMOS_ENTITY.get().create(player.level());
//        if (tempEntity != null) {
//            GiifuDemosRenderer renderer = new GiifuDemosRenderer(context);
//
//            // 复制玩家的属性到临时实体
//            // 1. 位置同步
//            tempEntity.copyPosition(player);
//
//            // 2. 旋转同步 - 直接设置实体的旋转角度
//            tempEntity.setYRot(player.getYRot());
//            tempEntity.setXRot(player.getXRot());
//
//            // 3. 移动状态同步
//            Vec3 deltaMovement = player.getDeltaMovement();
//            tempEntity.setDeltaMovement(deltaMovement);
//
//            // 4. 直接模拟实体移动来触发isMoving()状态
//            if (deltaMovement.lengthSqr() > 0.001D) {
//                try {
//                    tempEntity.xOld = tempEntity.getX() - deltaMovement.x * partialTick;
//                    tempEntity.zOld = tempEntity.getZ() - deltaMovement.z * partialTick;
//                } catch (Exception e) {
//                    // 忽略可能的异常
//                }
//            }
//
//            // 5. 其他运动状态同步
//            tempEntity.setNoGravity(player.isNoGravity());
//            tempEntity.setSprinting(player.isSprinting());
//            tempEntity.setSwimming(player.isSwimming());
//            tempEntity.swinging = player.swinging;
//            tempEntity.swingTime = player.swingTime;
//            tempEntity.swingingArm = player.swingingArm;
//
//            // 6. 物品同步
//            tempEntity.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem());
//            tempEntity.setItemInHand(InteractionHand.OFF_HAND, player.getOffhandItem());
//
//            // 触发变异动画
////            tempEntity.startMutationAnimation();
//
//            // 渲染变异中的模型
//            poseStack.scale(1.0F, 1.0F, 1.0F);
//
//            // 在渲染前调用aiStep()，确保动画状态已经更新
//            tempEntity.aiStep();
//
//            // 确保使用玩家的旋转角度进行渲染，保证朝向同步
//            float yRot = player.getYRot();
//            float xRot = player.getXRot();
//
//            // 调用渲染方法
//            renderer.render(tempEntity, yRot, partialTick, poseStack, bufferSource, packedLight);
//        }
//
//        poseStack.popPose();
//    }
//
//    private static void renderGiifuDemosModel(AbstractClientPlayer player, GiifuDemosEntity giifuDemosEntity,PoseStack poseStack, MultiBufferSource bufferSource,                                              int packedLight, float partialTick) {
//        poseStack.pushPose();
//
//        // 设置模型缩放和位置
//        poseStack.scale(1.0F, 1.0F, 1.0F);
//
//        // 复制玩家的状态到基夫德莫斯实体，确保动画和朝向正确
//        // 1. 位置同步 - 使用copyPosition确保位置完全一致
//        giifuDemosEntity.copyPosition(player);
//
//        // 2. 旋转同步 - 直接设置实体的旋转角度
//        giifuDemosEntity.setYRot(player.getYRot());
//        giifuDemosEntity.setXRot(player.getXRot());
//
//        // 3. 移动状态同步 - 这是动画播放的关键
//        // 设置deltaMovement确保移动速度正确传递
//        Vec3 deltaMovement = player.getDeltaMovement();
//        giifuDemosEntity.setDeltaMovement(deltaMovement);
//
//        // 4. 直接模拟实体移动来触发isMoving()状态
//        if (deltaMovement.lengthSqr() > 0.001D) {
//            try {
//                // 直接设置移动状态的方式
//                giifuDemosEntity.xOld = giifuDemosEntity.getX() - deltaMovement.x * partialTick;
//                giifuDemosEntity.zOld = giifuDemosEntity.getZ() - deltaMovement.z * partialTick;
//            } catch (Exception e) {
//                // 忽略可能的异常
//            }
//        }
//
//        // 5. 其他运动状态同步
//        giifuDemosEntity.setSprinting(player.isSprinting());
//        giifuDemosEntity.setSwimming(player.isSwimming());
//        giifuDemosEntity.swinging = player.swinging;
//        giifuDemosEntity.swingTime = player.swingTime;
//        giifuDemosEntity.swingingArm = player.swingingArm;
//
//        // 6. 物品同步
//        giifuDemosEntity.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem());
//        giifuDemosEntity.setItemInHand(InteractionHand.OFF_HAND, player.getOffhandItem());
//
//        // 获取 Minecraft 客户端实例
//        Minecraft minecraft = Minecraft.getInstance();
//
//        // 构造 EntityRendererProvider.Context 对象
//        EntityRendererProvider.Context context = new EntityRendererProvider.Context(
//                minecraft.getEntityRenderDispatcher(),
//                minecraft.getItemRenderer(),
//                minecraft.getBlockRenderer(),
//                minecraft.getEntityRenderDispatcher().getItemInHandRenderer(),
//                minecraft.getResourceManager(),
//                minecraft.getEntityModels(),
//                minecraft.font
//        );
//
//        // 创建 GiifuDemosRenderer 实例
//        GiifuDemosRenderer renderer = new GiifuDemosRenderer(context);
//
//        // 在渲染前调用aiStep()，确保动画状态已经更新
//        giifuDemosEntity.aiStep();
//
//        // 使用玩家的旋转角度进行渲染，确保朝向完全同步
//        float yRot = player.getYRot();
//        float xRot = player.getXRot();
//
//        // 调用渲染方法
//        renderer.render(giifuDemosEntity, yRot, partialTick, poseStack, bufferSource, packedLight);
//
//        poseStack.popPose();
//    }
//}