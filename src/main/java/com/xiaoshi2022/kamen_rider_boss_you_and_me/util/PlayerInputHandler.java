package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.BerserkMovementInput;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.ClientBerserkState;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerInputHandler {
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;
        
        if (ClientBerserkState.isBerserk()) {
            if (!(mc.player.input instanceof BerserkMovementInput)) {
                mc.player.input = new BerserkMovementInput();
            }
        } else {
            if (mc.player.input instanceof BerserkMovementInput) {
                mc.player.input = new net.minecraft.client.player.KeyboardInput(mc.options);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerInput(MovementInputUpdateEvent event) {
        if (event.getEntity() instanceof net.minecraft.client.player.LocalPlayer player) {
            if (ClientBerserkState.isBerserk()) {
                event.getInput().up = false;
                event.getInput().down = false;
                event.getInput().left = false;
                event.getInput().right = false;
                event.getInput().jumping = false;
                event.getInput().shiftKeyDown = false;
            }
        }
    }
    
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