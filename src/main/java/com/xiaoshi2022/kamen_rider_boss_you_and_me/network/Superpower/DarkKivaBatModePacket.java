package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkKivaAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DarkKivaBatModePacket {
    public DarkKivaBatModePacket() {
    }

    public DarkKivaBatModePacket(FriendlyByteBuf buffer) {
        // 空构造函数，因为这个包不需要额外数据
    }

    public void encode(FriendlyByteBuf buffer) {
        // 不需要编码任何数据
    }

    public static void handle(DarkKivaBatModePacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                DarkKivaAbilityHandler.tryBatMode(player);
            }
        });
        context.setPacketHandled(true);
    }
}