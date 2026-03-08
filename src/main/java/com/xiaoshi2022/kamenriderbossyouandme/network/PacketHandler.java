package com.xiaoshi2022.kamenriderbossyouandme.network;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.DriverSyncPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.ReleaseBeltPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PacketHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar(KamenRiderBossYOUandME.MODID)
                .versioned("0.0.1")
                .playToClient(
                        BeltAnimationPacket.TYPE,
                        BeltAnimationPacket.STREAM_CODEC,
                        BeltAnimationPacket::handle
                )
                .playToClient(
                        DriverSyncPacket.TYPE,
                        DriverSyncPacket.STREAM_CODEC,
                        DriverSyncPacket::handle
                )
                .playToServer(
                        ReleaseBeltPacket.TYPE,
                        ReleaseBeltPacket.STREAM_CODEC,
                        ReleaseBeltPacket::handle
                );
    }

    public static void sendToClient(ServerPlayer player, CustomPacketPayload packet) {
        player.connection.send(packet);
    }

    public static void sendToAllTracking(ServerPlayer player, CustomPacketPayload packet) {
        player.server.getPlayerList().getPlayers().forEach(p -> {
            if (p.level() == player.level()) {
                sendToClient(p, packet);
            }
        });
    }
    
    public static void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload packet) {
        // 发送给玩家自己
        sendToClient(player, packet);
        // 发送给其他追踪玩家
        player.server.getPlayerList().getPlayers().forEach(p -> {
            if (p != player && p.level() == player.level()) {
                sendToClient(p, packet);
            }
        });
    }

    public static void sendToServer(CustomPacketPayload packet) {
        net.minecraft.client.Minecraft.getInstance().getConnection().send(packet);
    }
}