package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 注意：此类已被禁用，因为Duke的超能力已修改为召唤骑士实体
 * 原有的能量护盾功能已不再使用
 */
@Mod.EventBusSubscriber
public class DukeShieldDamageHandler {

    // 处理实体伤害事件，应用Duke能量护盾减免
    @SubscribeEvent
    public static void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        // 由于Duke的超能力已修改为召唤骑士实体，此处理程序已被禁用
        // 保留此类以保持API兼容性
    }
}