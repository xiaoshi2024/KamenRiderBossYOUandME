package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.CherrySonicArrowAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CherryEnergyArrowPacket {
    public static void encode(CherryEnergyArrowPacket msg, FriendlyByteBuf buf) {}

    public static CherryEnergyArrowPacket decode(FriendlyByteBuf buf) {
        return new CherryEnergyArrowPacket();
    }

    public static void handle(CherryEnergyArrowPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                CherrySonicArrowAbilityHandler.tryCherryEnergyArrow(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

