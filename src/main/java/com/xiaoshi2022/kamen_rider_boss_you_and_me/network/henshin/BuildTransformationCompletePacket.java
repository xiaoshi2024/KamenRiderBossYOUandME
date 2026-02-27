package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BuildTransformationCompletePacket {

    public BuildTransformationCompletePacket() {
    }

    public static void encode(BuildTransformationCompletePacket msg, FriendlyByteBuf buffer) {
        // 不需要编码任何数据
    }

    public static BuildTransformationCompletePacket decode(FriendlyByteBuf buffer) {
        return new BuildTransformationCompletePacket();
    }

    public static void handle(BuildTransformationCompletePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 在客户端处理
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                // 重置客户端的变身状态
                com.xiaoshi2022.kamen_rider_boss_you_and_me.event.BuildDriverKeyHandler.resetTransformationState();
                System.out.println("[BuildTransformationCompletePacket] 收到服务器通知，重置客户端变身状态");
            }
        });
        ctx.get().setPacketHandled(true);
    }
}