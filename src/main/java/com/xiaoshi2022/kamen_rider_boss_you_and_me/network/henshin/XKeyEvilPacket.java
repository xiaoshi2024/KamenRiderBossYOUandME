package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.XKeyLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// 2. 最终变身包
public record XKeyEvilPacket(UUID playerId) {
    public static void encode(XKeyEvilPacket msg, FriendlyByteBuf buf) { buf.writeUUID(msg.playerId); }
    public static XKeyEvilPacket decode(FriendlyByteBuf buf) { return new XKeyEvilPacket(buf.readUUID()); }
    public static void handle(XKeyEvilPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null && sp.getUUID().equals(msg.playerId)) {
                System.out.println("收到最终变身请求，玩家: " + sp.getName().getString());
                XKeyLogic.toEvil(sp);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}