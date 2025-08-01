package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.KeybindHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReleaseBeltPacket {
    public void encode(FriendlyByteBuf buffer) {}

    public static ReleaseBeltPacket decode(FriendlyByteBuf buffer) {
        return new ReleaseBeltPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                KeybindHandler.completeBeltRelease(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}