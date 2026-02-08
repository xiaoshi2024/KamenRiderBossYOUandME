package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerBerserkHandler {
    
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            if (BerserkModeManager.isBerserk(player)) {
                event.getEntity().setDeltaMovement(
                    event.getEntity().getDeltaMovement().x,
                    0,
                    event.getEntity().getDeltaMovement().z
                );
            }
        }
    }
}