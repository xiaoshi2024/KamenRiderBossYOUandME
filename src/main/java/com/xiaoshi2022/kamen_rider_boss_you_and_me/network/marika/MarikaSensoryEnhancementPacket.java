package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.marika;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.MarikaAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 玛丽卡感官加强技能数据包
 * 当玩家按下V键时发送到服务器，触发感官加强技能
 */
public class MarikaSensoryEnhancementPacket {

    public MarikaSensoryEnhancementPacket() {
        // 空构造函数，用于网络传输
    }

    public static void encode(MarikaSensoryEnhancementPacket packet, FriendlyByteBuf buffer) {
        // 不需要编码任何数据
    }

    public static MarikaSensoryEnhancementPacket decode(FriendlyByteBuf buffer) {
        return new MarikaSensoryEnhancementPacket();
    }

    public static void handle(MarikaSensoryEnhancementPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 调用玛丽卡能力处理器中的感官加强方法
                MarikaAbilityHandler.activateSensoryEnhancement(player);
            }
        });
        context.setPacketHandled(true);
    }
}