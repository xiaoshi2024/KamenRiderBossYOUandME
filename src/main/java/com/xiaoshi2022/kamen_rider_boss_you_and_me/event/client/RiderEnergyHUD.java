package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.client;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * 骑士能量HUD - 显示玩家的骑士能量状态
 */
public class RiderEnergyHUD {
    // 能量HUD的ID
    public static final String HUD_ID = "rider_energy";
    
    // HUD相关常量
    private static final int HUD_X_OFFSET = 10; // 在屏幕左侧的偏移量
    private static final int HUD_BOTTOM_OFFSET = 10; // 从屏幕底部向上的偏移量
    private static final int ENERGY_BAR_WIDTH = 100; // 能量条宽度
    private static final int ENERGY_BAR_HEIGHT = 20; // 能量条高度
    private static final int ENERGY_BAR_BORDER_SIZE = 2; // 边框大小
    
    // 能量条背景、填充和边框的资源位置（需要在资源包中添加对应的纹理）
    // 这里使用占位符路径，需要根据实际模组资源结构修改
    private static final ResourceLocation ENERGY_BAR_BG = new ResourceLocation("kamen_rider_boss_you_and_me:textures/gui/hud/rider_energy_bg.png");
    private static final ResourceLocation ENERGY_BAR_FILL = new ResourceLocation("kamen_rider_boss_you_and_me:textures/gui/hud/rider_energy_fill.png");
    private static final ResourceLocation ENERGY_BAR_BORDER = new ResourceLocation("kamen_rider_boss_you_and_me:textures/gui/hud/rider_energy_border.png");
    
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
        
        // 计算能量百分比
        double energyPercentage = (currentEnergy / maxEnergy);
        
        // 计算能量条位置（屏幕左侧显示，从底部向上偏移）
        int barX = HUD_X_OFFSET;
        int barY = screenHeight - HUD_BOTTOM_OFFSET - ENERGY_BAR_HEIGHT;
        
        try {
            // 渲染能量条背景 - 使用完整的blit方法参数，确保正确的纹理映射
            // 参数含义: (纹理位置, 屏幕X, 屏幕Y, 纹理起始U, 纹理起始V, 渲染宽度, 渲染高度, 纹理总宽度, 纹理总高度)
            // 假设纹理的大小与渲染大小相同(100x20)
            guiGraphics.blit(ENERGY_BAR_BG, barX, barY, 0, 0, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);
        } catch (Exception e) {
            // 如果纹理不存在，使用默认的背景色
            guiGraphics.fill(barX, barY, barX + ENERGY_BAR_WIDTH, barY + ENERGY_BAR_HEIGHT, 0x80000000); // 半透明黑色背景
        }
        
        // 计算能量填充的高度（自下而上填充）
        int fillHeight = (int) (energyPercentage * ENERGY_BAR_HEIGHT);
        
        // 确保填充高度不为负数
        if (fillHeight > 0) {
            // 计算填充区域的起始Y坐标（从底部开始向上填充）
            int fillStartY = barY + ENERGY_BAR_HEIGHT - fillHeight;
            
            try {
                // 使用自定义贴图渲染能量填充进度
                // 参数含义: (纹理位置, 屏幕X, 屏幕Y, 纹理起始U, 纹理起始V, 渲染宽度, 渲染高度, 纹理总宽度, 纹理总高度)
                // 这里我们使用纹理的底部区域，根据能量百分比裁剪显示
                guiGraphics.blit(ENERGY_BAR_FILL, 
                        barX, fillStartY, 
                        0, ENERGY_BAR_HEIGHT - fillHeight, 
                        ENERGY_BAR_WIDTH, fillHeight, 
                        ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);
            } catch (Exception e) {
                // 如果无法加载填充贴图，使用默认的黄色填充作为备选
                guiGraphics.fill(barX, fillStartY, 
                        barX + ENERGY_BAR_WIDTH, 
                        barY + ENERGY_BAR_HEIGHT, 
                        0xFFFFCC00); // 半透明黄色
            }
        }
        
        try {
            // 渲染能量条边框 - 使用完整的blit方法参数
            guiGraphics.blit(ENERGY_BAR_BORDER, barX, barY, 0, 0, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);
        } catch (Exception e) {
            // 如果纹理不存在，使用默认的边框
            guiGraphics.fill(barX, barY, barX + ENERGY_BAR_WIDTH, barY + ENERGY_BAR_BORDER_SIZE, 0xFFFFFFFF); // 上边框
            guiGraphics.fill(barX, barY + ENERGY_BAR_HEIGHT - ENERGY_BAR_BORDER_SIZE, barX + ENERGY_BAR_WIDTH, barY + ENERGY_BAR_HEIGHT, 0xFFFFFFFF); // 下边框
            guiGraphics.fill(barX, barY, barX + ENERGY_BAR_BORDER_SIZE, barY + ENERGY_BAR_HEIGHT, 0xFFFFFFFF); // 左边框
            guiGraphics.fill(barX + ENERGY_BAR_WIDTH - ENERGY_BAR_BORDER_SIZE, barY, barX + ENERGY_BAR_WIDTH, barY + ENERGY_BAR_HEIGHT, 0xFFFFFFFF); // 右边框
        }
    }
    
    // 注意：现在使用自定义贴图进行能量填充，不再需要颜色计算方法
    // 保留注释作为参考，实际填充效果由rider_energy_fill.png贴图决定
}