package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.MOD)
public class TransformationConfig {
    // 全局开关状态，默认为关闭
    private static boolean globalGiveWeaponEnabled = false;
    
    // 服务器特定的开关状态，允许不同服务器有不同设置
    private static final Map<String, Boolean> serverSpecificSettings = new HashMap<>();
    
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // 初始化配置
        globalGiveWeaponEnabled = false;
    }
    
    /**
     * 获取全局开关状态
     */
    public static boolean isGlobalGiveWeaponEnabled() {
        return globalGiveWeaponEnabled;
    }
    
    /**
     * 设置全局开关状态
     */
    public static void setGlobalGiveWeaponEnabled(boolean enabled) {
        globalGiveWeaponEnabled = enabled;
    }
    
    /**
     * 获取特定服务器的开关状态
     * 如果没有为该服务器设置特定状态，则返回全局状态
     */
    public static boolean isGiveWeaponEnabledForServer(String serverId) {
        return serverSpecificSettings.getOrDefault(serverId, globalGiveWeaponEnabled);
    }
    
    /**
     * 设置特定服务器的开关状态
     */
    public static void setGiveWeaponEnabledForServer(String serverId, boolean enabled) {
        serverSpecificSettings.put(serverId, enabled);
    }
}