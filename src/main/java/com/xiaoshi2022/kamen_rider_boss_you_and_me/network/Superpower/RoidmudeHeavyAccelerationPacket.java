package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RoidmudeHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RoidmudeHeavyAccelerationPacket {
    public RoidmudeHeavyAccelerationPacket() {
    }

    public RoidmudeHeavyAccelerationPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .orElse(new KRBVariables.PlayerVariables());
                RoidmudeHandler.handleHeavyAcceleration(player, variables);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("重加速已激活！"), true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
