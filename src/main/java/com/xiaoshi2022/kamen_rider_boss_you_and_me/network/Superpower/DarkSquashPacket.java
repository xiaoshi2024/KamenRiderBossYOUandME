package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGaimJinbaLemonHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/* 黑暗爆破消息 */
public record DarkSquashPacket() {
    public static void encode(DarkSquashPacket msg, FriendlyByteBuf buf) {}
    public static DarkSquashPacket decode(FriendlyByteBuf buf) { return new DarkSquashPacket(); }
    public static void handle(DarkSquashPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) DarkGaimJinbaLemonHandler.tryDarkSquash(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}
