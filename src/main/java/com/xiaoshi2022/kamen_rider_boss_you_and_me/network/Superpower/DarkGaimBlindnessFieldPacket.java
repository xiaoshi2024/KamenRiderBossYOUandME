package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGaimJinbaLemonHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/* 黑暗铠武失明领域技能消息 */
public record DarkGaimBlindnessFieldPacket() {
    public static void encode(DarkGaimBlindnessFieldPacket msg, FriendlyByteBuf buf) {}
    public static DarkGaimBlindnessFieldPacket decode(FriendlyByteBuf buf) { return new DarkGaimBlindnessFieldPacket(); }
    public static void handle(DarkGaimBlindnessFieldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) DarkGaimJinbaLemonHandler.activateBlindnessField(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}