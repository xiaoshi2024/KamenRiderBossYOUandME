package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.BaronAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BaronBananaEnergyPacket {
    public BaronBananaEnergyPacket() {
        // 空构造函数，不需要额外数据
    }

    public BaronBananaEnergyPacket(FriendlyByteBuf buffer) {
        // 从缓冲区读取数据的构造函数
        // 这个数据包不需要额外数据，所以留空
    }

    public void encode(FriendlyByteBuf buffer) {
        // 编码数据到缓冲区
        // 这个数据包不需要额外数据，所以留空
    }

    public static void handle(BaronBananaEnergyPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                BaronAbilityHandler.tryBananaEnergy(player);
            }
        });
        context.setPacketHandled(true);
    }
}