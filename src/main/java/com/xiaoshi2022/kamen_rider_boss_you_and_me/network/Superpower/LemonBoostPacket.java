package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGaimJinbaLemonHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/* 柠檬加速消息 */
public record LemonBoostPacket() {
    public static void encode(LemonBoostPacket msg, FriendlyByteBuf buf) {}
    public static LemonBoostPacket decode(FriendlyByteBuf buf) { return new LemonBoostPacket(); }
    public static void handle(LemonBoostPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) DarkGaimJinbaLemonHandler.tryLemonBoost(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}
