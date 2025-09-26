package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Objects;

/**
 * 骑士踢冷却HUD - 显示骑士踢技能的冷却时间
 */
public class KickCooldownHUD {
    // 冷却HUD的ID
    public static final String HUD_ID = "kick_cooldown";

    // 冷却时间常量（毫秒）- 40秒
    private static final long KICK_COOLDOWN_TIME = 40000;

    // 只保留必要的常量
    // 冷却条相关常量 - 调整为适当比例(原32x8缩放)
    private static final int HUD_Y_OFFSET = 35; // 在物品栏上方的偏移量

    // HUD渲染器实例
    public static final IGuiOverlay HUD_KICK_COOLDOWN = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        renderKickCooldownHUD(guiGraphics, screenWidth, screenHeight);
    };

    private static void renderKickCooldownHUD(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return;
        }

        // 获取玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());

        // 检查玩家是否佩戴了骑士腰带
        if (!KamenBossArmorUtil.isKamenBossArmorEquipped(player)) {
            return; // 未佩戴腰带，不显示HUD
        }

        // 计算冷却时间（使用实际游戏数据）
        long currentTime = System.currentTimeMillis();
        long timeSinceLastKick = currentTime - variables.lastKickTime;
        long remainingCooldown = Math.max(0, KICK_COOLDOWN_TIME - timeSinceLastKick);
        // 将毫秒转换为秒，并向上取整，确保显示的是整数秒
        int remainingSeconds = (int) Math.ceil(remainingCooldown / 1000.0);

        // 确保显示的冷却时间准确反映游戏状态
        if (remainingSeconds < 0) {
            remainingSeconds = 0; // 避免显示负数值
        }

        // 只保留文本显示
        Font fontRenderer = minecraft.font;
        Component cooldownText = Component.translatable("hud.kamen_rider_boss_you_and_me.rider_cooldown", remainingSeconds);
        int textWidth = fontRenderer.width(cooldownText.getString());
        int textY = screenHeight - HUD_Y_OFFSET - 5;

        // 根据剩余冷却时间设置文本颜色
        int textColor = remainingSeconds <= 1 ? 0x00FF00 : (remainingSeconds <= KICK_COOLDOWN_TIME / 1000 / 2 ? 0xFFFF00 : 0xFF0000);
        guiGraphics.drawString(fontRenderer, cooldownText,
                (screenWidth - textWidth) / 2, textY, textColor);
    }
}
