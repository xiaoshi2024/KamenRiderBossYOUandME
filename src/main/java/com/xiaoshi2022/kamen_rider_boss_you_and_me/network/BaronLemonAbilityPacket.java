package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.BaronLemonAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 巴隆柠檬形态骑士2技能触发数据包
 * 从客户端发送到服务器，请求触发柠檬能量陷阱技能
 */
public class BaronLemonAbilityPacket {

    public BaronLemonAbilityPacket() {
        // 空构造函数，用于反序列化
    }

    // 从字节缓冲区读取数据包内容
    public BaronLemonAbilityPacket(FriendlyByteBuf buf) {
        // 此数据包不需要额外数据
    }

    // 将数据包内容写入字节缓冲区
    public void toBytes(FriendlyByteBuf buf) {
        // 此数据包不需要额外数据
    }

    // 处理数据包（在服务器端执行）
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 调用巴隆柠檬形态能力处理器的技能触发方法
                BaronLemonAbilityHandler.tryLemonEnergyTrap(player);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}