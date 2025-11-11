package com.xiaoshi2022.kamen_rider_boss_you_and_me.villagers;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraftforge.eventbus.api.IEventBus;

public class VillagerRegistry {
    public static void register(IEventBus eventBus) {
        // 注册POI类型和职业
        TimeJackerProfession.POI_TYPE.register(eventBus);
        TimeJackerProfession.PROFESSION.register(eventBus);
        
        kamen_rider_boss_you_and_me.LOGGER.info("Registered Time Jacker profession and POI type");
    }
}