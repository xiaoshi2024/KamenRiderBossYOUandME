package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.riderkick;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/* 骑士踢请求数据包 */
public record RiderKickRequestPacket() {
    public static void encode(RiderKickRequestPacket msg, FriendlyByteBuf buf) {}
    public static RiderKickRequestPacket decode(FriendlyByteBuf buf) { return new RiderKickRequestPacket(); }
    public static void handle(RiderKickRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) {
                // 服务器端处理骑士踢请求
                sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                    capability.kick = true;
                    capability.wudi = true;
                    capability.lastKickTime = System.currentTimeMillis();
                    capability.kickStartTime = System.currentTimeMillis();
                    capability.syncPlayerVariables(sp);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}