package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BaronSummonInvesPacket {
    
    public BaronSummonInvesPacket() {
        // 空构造函数，用于反序列化
    }
    
    public BaronSummonInvesPacket(FriendlyByteBuf buffer) {
        // 从缓冲区读取数据（如果需要）
    }
    
    public void toBytes(FriendlyByteBuf buffer) {
        // 将数据写入缓冲区（如果需要）
    }
    
    public static void handler(BaronSummonInvesPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 确保在服务器端执行
            if (context.getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    // 处理召唤异域者的逻辑
                    handleSummonInves(player);
                }
            }
        });
        context.setPacketHandled(true);
    }
    
    private static void handleSummonInves(ServerPlayer player) {
        // 发送动画到客户端
        if (player.level().isClientSide()) return;

        PacketHandler.sendAnimationToAll(
                Component.literal("shows"),
                player.getId(),
                false
        );
        // 这里实现召唤异域者的逻辑，参照LordBaronEntity中的方法
        BaronSummonInvesLogic.summonInvesAroundPlayer(player);
    }
}