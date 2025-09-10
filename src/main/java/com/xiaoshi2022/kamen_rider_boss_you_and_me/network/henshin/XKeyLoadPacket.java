package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.XKeyLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// 1. 加载武器包
public record XKeyLoadPacket(UUID playerId, boolean b) {
    public static void encode(XKeyLoadPacket msg, FriendlyByteBuf buf) { buf.writeUUID(msg.playerId); buf.writeBoolean(msg.b); }
    public static XKeyLoadPacket decode(FriendlyByteBuf buf) { return new XKeyLoadPacket(buf.readUUID(), buf.readBoolean()); }
    public static void handle(XKeyLoadPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null && sp.getUUID().equals(msg.playerId)) {
                XKeyLogic.loadWeapon(sp, msg.b());   // 传递boolean参数
            }
        });
        ctx.get().setPacketHandled(true);
    }
}