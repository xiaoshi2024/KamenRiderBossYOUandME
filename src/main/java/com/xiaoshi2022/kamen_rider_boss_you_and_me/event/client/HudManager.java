package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.client;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * HUD管理器，用于集中注册所有HUD覆盖层
 */
public class HudManager {
    
    @Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            // 注册骑士能量HUD
            event.registerAboveAll("rider_energy", RiderEnergyHUD.HUD_RIDER_ENERGY);
            
            // 注册骑士踢冷却HUD
            event.registerAboveAll("kick_cooldown", KickCooldownHUD.HUD_KICK_COOLDOWN);
            
            kamen_rider_boss_you_and_me.LOGGER.info("已注册所有HUD覆盖层");
        }
    }
}