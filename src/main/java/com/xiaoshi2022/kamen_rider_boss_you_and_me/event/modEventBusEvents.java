package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class modEventBusEvents {
    @Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

    }
    @Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public final class CommonListener {
        @SubscribeEvent
        public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.GIIFUDEMOS_ENTITY.get(), GiifuDemosEntity.createAttributes().build());
        }
    }
}