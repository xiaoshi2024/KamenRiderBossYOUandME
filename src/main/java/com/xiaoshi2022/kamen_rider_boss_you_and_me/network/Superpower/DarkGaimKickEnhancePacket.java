package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGaimJinbaLemonHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/* 黑暗铠武踢力增强技能消息 */
public record DarkGaimKickEnhancePacket() {
    public static void encode(DarkGaimKickEnhancePacket msg, FriendlyByteBuf buf) {}
    public static DarkGaimKickEnhancePacket decode(FriendlyByteBuf buf) { return new DarkGaimKickEnhancePacket(); }
    public static void handle(DarkGaimKickEnhancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) DarkGaimJinbaLemonHandler.activateKickEnhance(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}