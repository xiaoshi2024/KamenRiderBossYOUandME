package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BaronRecallInvesPacket {

    public BaronRecallInvesPacket() {
        // 空构造函数，用于网络传输
    }

    // 从字节缓冲区读取数据
    public static BaronRecallInvesPacket decode(FriendlyByteBuf buf) {
        return new BaronRecallInvesPacket();
    }

    // 将数据写入字节缓冲区
    public static void encode(BaronRecallInvesPacket packet, FriendlyByteBuf buf) {
        // 这个数据包不需要额外的数据
    }

    // 在服务器端处理数据包
    public static void handle(BaronRecallInvesPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                // 调用BaronSummonInvesLogic中的召回方法
                BaronSummonInvesLogic.recallSummonedInves(player);
            }
        });
        context.get().setPacketHandled(true);
    }
}