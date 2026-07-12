package com.xiaoshi2022.kamenriderbossyouandme.network;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.DriverSyncPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.ReleaseBeltPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.BYAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.InvisibilitySyncPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.PlayerMovementPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PacketHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {
        // ✅ 所有包都在这里注册，不区分客户端/服务端
        var registrar = event.registrar(KamenRiderBossYOUandME.MODID)
                .versioned("0.0.1");

        registrar.playToClient(
                BeltAnimationPacket.TYPE,
                BeltAnimationPacket.STREAM_CODEC,
                BeltAnimationPacket::handle
        );

        registrar.playToClient(
                DriverSyncPacket.TYPE,
                DriverSyncPacket.STREAM_CODEC,
                DriverSyncPacket::handle
        );

        registrar.playToServer(
                ReleaseBeltPacket.TYPE,
                ReleaseBeltPacket.STREAM_CODEC,
                ReleaseBeltPacket::handle
        );

        // ✅ BYAnimationPacket 的 handler 使用 context.player() 而不是 Minecraft.getInstance()
        registrar.playToClient(
                BYAnimationPacket.TYPE,
                BYAnimationPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player clientPlayer = context.player();
                    if (clientPlayer == null) return;
                    if (!clientPlayer.getUUID().equals(payload.playerId())) return;

                    // 使用 context.player() 替代 Minecraft.getInstance()
                    com.xiaoshi2022.kamenriderbossyouandme.impl.playerAnimator.PlayerAnimationHandler.handleAnimation(
                            clientPlayer, payload.animationId(), payload.fadeDuration()
                    );
                })
        );

        // ✅ 注册隐身同步包
        registrar.playToClient(
                InvisibilitySyncPacket.TYPE,
                InvisibilitySyncPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player player = context.player();
                    if (player == null) return;
                    if (!player.getUUID().equals(payload.playerId())) return;

                    // 客户端设置隐身
                    player.setInvisible(payload.invisible());

                    // 如果是本地玩家，也更新渲染
                    if (player == net.minecraft.client.Minecraft.getInstance().player) {
                        // 强制更新
                        player.refreshDimensions();
                    }
                })
        );

        // ✅ PlayerMovementPacket 的 handler 使用 context.player()
        registrar.playToClient(
                PlayerMovementPacket.TYPE,
                PlayerMovementPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player clientPlayer = context.player();
                    if (clientPlayer == null) return;
                    if (!clientPlayer.getUUID().equals(payload.playerId())) return;

                    // 只在客户端执行
                    if (clientPlayer.level().isClientSide()) {
                        net.minecraft.world.phys.Vec3 movement = new net.minecraft.world.phys.Vec3(payload.x(), payload.y(), payload.z());
                        clientPlayer.hurtMarked = true;
                        if (payload.operationType().equals("add")) {
                            clientPlayer.addDeltaMovement(movement);
                        } else if (payload.operationType().equals("set")) {
                            clientPlayer.setDeltaMovement(movement);
                        }
                    }
                })
        );
    }

    // ========== 服务端发送包的方法 ==========

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
        sendToClient(player, packet);
        player.server.getPlayerList().getPlayers().forEach(p -> {
            if (p != player && p.level() == player.level()) {
                sendToClient(p, packet);
            }
        });
    }

    // ========== 客户端发送包到服务端 ==========

    /**
     * 发送数据包到服务端（仅在客户端可用）
     */
    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(CustomPacketPayload packet) {
        var connection = net.minecraft.client.Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(packet);
        }
    }
}