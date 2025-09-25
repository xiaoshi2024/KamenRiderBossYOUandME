package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DukePlayerAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DukeCombatAnalysisPacket {
    public static DukeCombatAnalysisPacket newPacket(FriendlyByteBuf buf) { 
        return new DukeCombatAnalysisPacket(); 
    }
    
    public static void buffer(DukeCombatAnalysisPacket msg, FriendlyByteBuf buf) {}
    
    public static void handle(DukeCombatAnalysisPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                DukePlayerAbilityHandler.activateCombatAnalysis(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}