package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 时劫者事件处理器
 * 处理时劫者相关的游戏事件
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class TimeJackerEventHandler {
    
    /**
     * 当实体加入世界时触发
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否是时劫者实体
        if (event.getEntity() instanceof TimeJackerEntity timeJacker) {
            // 可以在这里添加时劫者加入世界时的初始化逻辑
            // 例如设置特定的属性、行为或者给予特殊物品
        }
        // 也可以处理MCA村民与时间劫者的互动
        else if (event.getEntity() instanceof VillagerEntityMCA villager) {
            // 可以在这里添加村民对时劫者的特殊反应逻辑
        }
    }
    
    /**
     * 可以在这里添加更多事件监听器
     * 例如实体交互事件、攻击事件等
     */
}