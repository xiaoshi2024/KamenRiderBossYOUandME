package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGaimJinbaLemonHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/* 阵羽护盾消息 */
public record JinbaGuardPacket() {
    public static void encode(JinbaGuardPacket msg, FriendlyByteBuf buf) {}
    public static JinbaGuardPacket decode(FriendlyByteBuf buf) { return new JinbaGuardPacket(); }
    public static void handle(JinbaGuardPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) DarkGaimJinbaLemonHandler.tryJinbaGuard(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}