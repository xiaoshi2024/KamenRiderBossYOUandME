package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.TyrantAbilityHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * 处理虚化技能的玩家透明效果
 * 使处于虚化状态的玩家在所有客户端上看起来都是透明的，包括盔甲和装备
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = kamen_rider_boss_you_and_me.MODID)
public class IntangibilityRenderHandler {
    
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        // 获取被渲染的玩家
        Player player = event.getEntity();
        
        // 检查玩家是否处于虚化状态
        if (TyrantAbilityHandler.isPlayerIntangible(player)) {
            // 启用混合模式
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // 设置透明度为更透明
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.1F);
        }
    }
    
    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        // 获取被渲染的玩家
        Player player = event.getEntity();
        
        // 检查玩家是否处于虚化状态
        if (TyrantAbilityHandler.isPlayerIntangible(player)) {

            // 确保禁用混合以避免影响其他渲染
            RenderSystem.disableBlend();
        }
    }
}