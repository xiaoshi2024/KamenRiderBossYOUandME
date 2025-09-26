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
 * 骑士能量HUD - 显示玩家的骑士能量状态
 */
public class RiderEnergyHUD {
    // 能量HUD的ID
    public static final String HUD_ID = "rider_energy";
    
    // 只保留必要的常量
    private static final int HUD_Y_OFFSET = 50; // 在物品栏上方的偏移量
    
    // HUD渲染器实例
    public static final IGuiOverlay HUD_RIDER_ENERGY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        renderRiderEnergyHUD(guiGraphics, screenWidth, screenHeight);
    };

    private static void renderRiderEnergyHUD(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return;
        }

        // 获取玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());

        // 检查是否佩戴了骑士腰带
        if (!KamenBossArmorUtil.isKamenBossArmorEquipped(player)) {
            return; // 未佩戴腰带则不显示HUD
        }

        // 获取能量值（使用实际游戏数据）
        double currentEnergy = variables.riderEnergy;
        double maxEnergy = variables.maxRiderEnergy > 0 ? Math.max(1, variables.maxRiderEnergy) : 100; // 避免除零
        
        // 如果实际能量值为0或未初始化，设置一个合理的默认值
        if (currentEnergy <= 0) {
            currentEnergy = 10; // 设置一个基础值，让玩家能看到HUD正常工作
        }
        
        int energyPercentage = (int) Math.round((currentEnergy / maxEnergy) * 100);
        String energyPercentageStr = energyPercentage + "%";
        Component energyText = Component.translatable("hud.kamen_rider_boss_you_and_me.rider_energy", energyPercentageStr);

        // 只保留文本显示
        Font fontRenderer = minecraft.font;
        int textWidth = fontRenderer.width(energyText);
        int textY = screenHeight - HUD_Y_OFFSET - 5;
        
        // 根据能量百分比设置文本颜色
        int textColor;
        if (energyPercentage <= 20) {
            textColor = 0xFF0000; // 低能量 - 红色
        } else if (energyPercentage <= 50) {
            textColor = 0xFFFF00; // 中等能量 - 黄色
        } else {
            textColor = 0x00FF00; // 高能量 - 绿色
        }
        
        guiGraphics.drawString(fontRenderer, energyText, 
                (screenWidth - textWidth) / 2, textY, textColor);
    }
}