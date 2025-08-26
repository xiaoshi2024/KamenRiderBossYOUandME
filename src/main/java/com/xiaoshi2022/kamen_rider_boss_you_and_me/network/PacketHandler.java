package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkSquashPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.JinbaGuardPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.LemonBoostPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.*;
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
        // 注册 LemonTransformationRequestPacket
        INSTANCE.registerMessage(index++, LemonTransformationRequestPacket.class, LemonTransformationRequestPacket::encode, LemonTransformationRequestPacket::decode, LemonTransformationRequestPacket::handle);

        // 注册 BananaTransformationRequestPacket
        INSTANCE.registerMessage(index++, BananaTransformationRequestPacket.class, BananaTransformationRequestPacket::encode, BananaTransformationRequestPacket::decode, BananaTransformationRequestPacket::handle);

        INSTANCE.registerMessage(index++, MelonTransformationRequestPacket.class, MelonTransformationRequestPacket::encode, MelonTransformationRequestPacket::decode, MelonTransformationRequestPacket::handle);
        
        // 注册 CherryTransformationRequestPacket
        INSTANCE.registerMessage(index++, CherryTransformationRequestPacket.class, CherryTransformationRequestPacket::encode, CherryTransformationRequestPacket::decode, CherryTransformationRequestPacket::handle);

        INSTANCE.registerMessage(index++, MarikaTransformationRequestPacket.class, MarikaTransformationRequestPacket::encode, MarikaTransformationRequestPacket::decode, MarikaTransformationRequestPacket::handle);

        // 注册 DarkOrangeTransformationRequestPacket
        INSTANCE.registerMessage(index++, DarkOrangeTransformationRequestPacket.class, DarkOrangeTransformationRequestPacket::encode, DarkOrangeTransformationRequestPacket::decode, DarkOrangeTransformationRequestPacket::handle);

        // 注册 DarkOrangeReleaseRequestPacket
        INSTANCE.registerMessage(index++, DarkOrangeReleaseRequestPacket.class, DarkOrangeReleaseRequestPacket::encode, DarkOrangeReleaseRequestPacket::decode, DarkOrangeReleaseRequestPacket::handle);

        // 注册 DragonfruitTransformationRequestPacket
        INSTANCE.registerMessage(index++, DragonfruitTransformationRequestPacket.class, DragonfruitTransformationRequestPacket::encode, DragonfruitTransformationRequestPacket::decode, DragonfruitTransformationRequestPacket::handle);
        INSTANCE.registerMessage(index++, JinbaGuardPacket.class,
                JinbaGuardPacket::encode, JinbaGuardPacket::decode,
                JinbaGuardPacket::handle);
        INSTANCE.registerMessage(index++, DarkSquashPacket.class,
                DarkSquashPacket::encode, DarkSquashPacket::decode,
                DarkSquashPacket::handle);
        INSTANCE.registerMessage(index++, LemonBoostPacket.class,
                LemonBoostPacket::encode, LemonBoostPacket::decode,
                LemonBoostPacket::handle);
        INSTANCE.registerMessage(index++, SyncTransformationPacket.class,
                SyncTransformationPacket::encode,
                SyncTransformationPacket::decode,
                SyncTransformationPacket::handle);
        INSTANCE.registerMessage(
                index++,
                SyncBloodlinePacket.class,
                SyncBloodlinePacket::encode,
                SyncBloodlinePacket::new,
                SyncBloodlinePacket::handle
        );
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

    // 添加 sendToAll 方法
    public static void sendToAll(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        if (player != null && !player.hasDisconnected()) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    public static void sendToAllTracking(Object packet, Entity entity) {
        if (entity != null && !entity.level().isClientSide()) {
            INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
        }
    }

    public static void sendToAllAround(Object packet, ServerLevel level, double x, double y, double z, double radius) {
        INSTANCE.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        x, y, z, radius, level.dimension()
                )),
                packet
        );
    }
}