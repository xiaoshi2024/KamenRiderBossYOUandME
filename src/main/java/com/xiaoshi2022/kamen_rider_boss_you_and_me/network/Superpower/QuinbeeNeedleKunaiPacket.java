package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.QuinbeeAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QuinbeeNeedleKunaiPacket {
    public QuinbeeNeedleKunaiPacket() {
    }

    public QuinbeeNeedleKunaiPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                QuinbeeAbilityHandler.tryNeedleKunai(player);
            }
        });
        context.get().setPacketHandled(true);
    }
}