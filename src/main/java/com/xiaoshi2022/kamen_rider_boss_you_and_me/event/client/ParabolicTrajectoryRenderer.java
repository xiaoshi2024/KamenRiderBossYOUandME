package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/**
 * 骑士踢抛物线轨迹渲染器 - 渲染骑士踢技能的抛物线轨迹
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ParabolicTrajectoryRenderer {

    private static final float LINE_WIDTH = 2.0F;
    private static final int SEGMENTS = 10;
    private static final float ALPHA = 0.8F;
    private static final float RED = 1.0F;
    private static final float GREEN = 0.5F;
    private static final float BLUE = 0.0F;

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            renderParabolicTrajectory(event);
        }
    }

    private static void renderParabolicTrajectory(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return;
        }

        // 获取玩家变量，检查是否正在进行骑士踢
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY)
                .orElse(new KRBVariables.PlayerVariables());

        // 只有在踢击状态下才渲染轨迹
        if (!variables.kick) {
            return;
        }

        // 计算抛物线轨迹
        Vec3 startPos = player.position();
        Vec3 lookVec = player.getLookAngle();

        // 水平速度
        double horizontalSpeed = 1.5;
        double horizontalX = lookVec.x * horizontalSpeed;
        double horizontalZ = lookVec.z * horizontalSpeed;

        // 垂直速度和重力
        double initialYVelocity = 0.4;
        double gravity = 0.08;

        // 渲染抛物线轨迹
        renderTrajectoryLine(event, player.level(), startPos, horizontalX, horizontalZ, initialYVelocity, gravity);
    }

    private static void renderTrajectoryLine(RenderLevelStageEvent event, Level level, Vec3 startPos, double horizontalX, double horizontalZ, double initialYVelocity, double gravity) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher entityRenderDispatcher = minecraft.getEntityRenderDispatcher();

        // 设置渲染状态
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(RED, GREEN, BLUE, ALPHA);
        RenderSystem.lineWidth(LINE_WIDTH);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        // 计算并渲染轨迹段
        for (int i = 0; i <= SEGMENTS; i++) {
            double t = (double) i / SEGMENTS;
            double time = t * 20; // 假设轨迹持续20刻

            // 计算当前位置
            double x = startPos.x + horizontalX * time;
            double y = startPos.y + 1.0 + initialYVelocity * time - 0.5 * gravity * time * time;
            double z = startPos.z + horizontalZ * time;

            // 检查该位置是否在世界边界内
            if (x < level.getMinBuildHeight() || x > level.getMaxBuildHeight() ||
                    z < level.getMinBuildHeight() || z > level.getMaxBuildHeight() ||
                    y < level.getMinBuildHeight() || y > level.getMaxBuildHeight()) {
                break;
            }

            // 检查该位置是否被阻挡
            BlockPos blockPos = new BlockPos((int) x, (int) y, (int) z);
            if (level.getBlockState(blockPos).isSolidRender(level, blockPos)) {
                break;
            }

            // 添加顶点
            Vec3 pos = new Vec3(x, y, z);
            pos = pos.subtract(entityRenderDispatcher.camera.getPosition());
            bufferBuilder.vertex(pos.x, pos.y, pos.z)
                    .color(RED, GREEN, BLUE, ALPHA)
                    .endVertex();
        }

        // 结束渲染
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0F);
    }
}