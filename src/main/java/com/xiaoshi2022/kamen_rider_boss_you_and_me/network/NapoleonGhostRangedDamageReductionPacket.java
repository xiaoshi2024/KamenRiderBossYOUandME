package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.NapoleonGhostAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NapoleonGhostRangedDamageReductionPacket {
    public NapoleonGhostRangedDamageReductionPacket() {
        // 空构造函数，用于网络传输
    }

    public static void encode(NapoleonGhostRangedDamageReductionPacket packet, FriendlyByteBuf buffer) {
        // 无需编码额外数据
    }

    public static NapoleonGhostRangedDamageReductionPacket decode(FriendlyByteBuf buffer) {
        return new NapoleonGhostRangedDamageReductionPacket();
    }

    public static void handle(NapoleonGhostRangedDamageReductionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 调用拿破仑Ghost能力处理器中的方法来激活远程伤害减免功能
                NapoleonGhostAbilityHandler.activateRangedDamageReduction(player);
            }
        });
        context.setPacketHandled(true);
    }
}