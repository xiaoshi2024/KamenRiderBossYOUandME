package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.TransformationRequestPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static dev.kosmx.playerAnim.forge.ForgeModInterface.LOGGER;

public class PacketHandler {
    private static int currentId = 0;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("kamen_rider_boss_you_and_me", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        int index = 0;
        INSTANCE.registerMessage(index++, PlayerAnimationPacket.class, PlayerAnimationPacket::toBytes, PlayerAnimationPacket::new, PlayerAnimationPacket::handle);
        INSTANCE.registerMessage(index++, SoundStopPacket.class, SoundStopPacket::encode, SoundStopPacket::decode, SoundStopPacket.Handler::handle);
        INSTANCE.registerMessage(index++, InvisibilityPacket.class, InvisibilityPacket::encode, InvisibilityPacket::decode, InvisibilityPacket::handle);
        INSTANCE.registerMessage(index++, RemoveEntityPacket.class, RemoveEntityPacket::toBytes, RemoveEntityPacket::new, RemoveEntityPacket::handle);
        INSTANCE.registerMessage(index++, PlayerArmorPacket.class, PlayerArmorPacket::toBytes, PlayerArmorPacket::new, PlayerArmorPacket::handle);
        INSTANCE.registerMessage(index++, PlayerEquipmentPacket.class, PlayerEquipmentPacket::toBytes, PlayerEquipmentPacket::new, PlayerEquipmentPacket::handle);
        INSTANCE.registerMessage(index++, BeltAnimationPacket.class, BeltAnimationPacket::encode, BeltAnimationPacket::decode, BeltAnimationPacket::handle);
        INSTANCE.registerMessage(index++,  SyncOwnerPacket.class,SyncOwnerPacket::encode, SyncOwnerPacket::decode,SyncOwnerPacket::handle);
        INSTANCE.registerMessage(index++, ReleaseBeltPacket.class, ReleaseBeltPacket::encode, ReleaseBeltPacket::decode, ReleaseBeltPacket::handle);
        INSTANCE.registerMessage(index++, TransformationRequestPacket.class, TransformationRequestPacket::encode, TransformationRequestPacket::decode, TransformationRequestPacket::handle);
//        PacketHandler.logChannelStatus();
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

    // 添加 sendToAll 方法
    public static void sendToAll(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendToAllTracking(Object packet, Entity entity) {
        if (!entity.level().isClientSide) {  // 更安全的检查
            INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
        }
    }

//    // 添加调试方法
//    public static void logChannelStatus() {
//        LOGGER.info("Network channel initialized with {} packets", currentId);
//    }

    public static int getNextId() {
        return currentId++;
    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}