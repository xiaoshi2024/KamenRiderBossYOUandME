package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGaimJinbaLemonHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/* 黑暗铠武赫尔海姆裂缝技能消息 */
public record DarkGaimHelheimCrackPacket() {
    public static void encode(DarkGaimHelheimCrackPacket msg, FriendlyByteBuf buf) {}
    public static DarkGaimHelheimCrackPacket decode(FriendlyByteBuf buf) { return new DarkGaimHelheimCrackPacket(); }
    public static void handle(DarkGaimHelheimCrackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) DarkGaimJinbaLemonHandler.activateHelheimCrack(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}