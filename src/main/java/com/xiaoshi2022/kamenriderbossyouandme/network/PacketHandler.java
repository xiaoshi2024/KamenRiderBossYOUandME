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
import net.neoforged.neoforge.network.PacketDistributor;

public class PacketHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {
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

        registrar.playToClient(
                BYAnimationPacket.TYPE,
                BYAnimationPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player clientPlayer = context.player();
                    if (clientPlayer == null) return;
                    if (!clientPlayer.getUUID().equals(payload.playerId())) return;

                    com.xiaoshi2022.kamenriderbossyouandme.impl.playerAnimator.PlayerAnimationHandler.handleAnimation(
                            clientPlayer, payload.animationId(), payload.fadeDuration()
                    );
                })
        );

        registrar.playToClient(
                InvisibilitySyncPacket.TYPE,
                InvisibilitySyncPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player player = context.player();
                    if (player == null) return;
                    if (!player.getUUID().equals(payload.playerId())) return;

                    player.setInvisible(payload.invisible());

                    if (player == net.minecraft.client.Minecraft.getInstance().player) {
                        player.refreshDimensions();
                    }
                })
        );

        // ===== 新增：融合皮肤更新包 =====
        registrar.playToClient(
                FusionSkinUpdatePacket.TYPE,
                FusionSkinUpdatePacket.STREAM_CODEC,
                FusionSkinUpdatePacket::handleData
        );

        registrar.playToClient(
                PlayerMovementPacket.TYPE,
                PlayerMovementPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player clientPlayer = context.player();
                    if (clientPlayer == null) return;
                    if (!clientPlayer.getUUID().equals(payload.playerId())) return;

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
        if (player == null || player.isRemoved()) return;
        player.connection.send(packet);
    }

    /**
     * 发送给追踪该玩家的所有客户端
     */
    public static void sendToAllTracking(ServerPlayer player, CustomPacketPayload packet) {
        if (player == null || player.isRemoved()) return;
        // NeoForge 1.21.1 的新 API
        PacketDistributor.sendToPlayersTrackingEntity(player, packet);
    }

    /**
     * 发送给追踪该玩家的所有客户端 + 玩家自己
     */
    public static void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload packet) {
        if (player == null || player.isRemoved()) return;
        // 先发送给自己
        sendToClient(player, packet);
        // 再发送给追踪者
        PacketDistributor.sendToPlayersTrackingEntity(player, packet);
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