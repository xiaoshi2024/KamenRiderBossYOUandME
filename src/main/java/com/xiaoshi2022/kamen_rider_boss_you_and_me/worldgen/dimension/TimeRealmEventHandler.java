package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 时间领域维度的事件监听器
 * 处理维度注册、世界加载等相关事件
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimeRealmEventHandler {
    
    /**
     * 当服务器启动时调用
     * 负责注册时间领域维度
     */
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // 注册维度类型和维度
        TimeRealmDimensionRegistry.registerDimensions(event.getServer());
    }
    
    /**
     * 当维度被加载时调用
     * 初始化时间领域的结构生成
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) event.getLevel();
            
            // 检查是否是时间领域维度
            if (serverLevel.dimension().equals(TimeRealmDimensionRegistry.TIME_REALM_DIMENSION)) {
                // 初始化结构生成
                TimeRealmDimension.initializeStructureGeneration(serverLevel);
                
                // 初始化维度数据
                TimeRealmDimension.initializeDimension(serverLevel);
            }
        }
    }
}