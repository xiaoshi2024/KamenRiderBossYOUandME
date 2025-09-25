package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DukeKnightAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SummonDukeKnightPacket {
    public static SummonDukeKnightPacket newPacket(FriendlyByteBuf buf) { return new SummonDukeKnightPacket(); }
    public static void buffer(SummonDukeKnightPacket msg, FriendlyByteBuf buf) {}
    public static void handle(SummonDukeKnightPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                DukeKnightAbilityHandler.summonDukeKnight(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}