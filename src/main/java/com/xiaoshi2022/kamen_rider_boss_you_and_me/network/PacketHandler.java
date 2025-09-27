package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkGaimKickEnhancePacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkGaimBlindnessFieldPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkGaimHelheimCrackPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.evil.LeftClickShiftPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaBatModePacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaBloodSuckPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DarkKivaSonicBlastPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.DukeCombatAnalysisPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.BaronBananaEnergyPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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
        INSTANCE.registerMessage(index++, PlayerAnimationPacket.class,
                PlayerAnimationPacket::encode, PlayerAnimationPacket::decode, PlayerAnimationPacket::handle);
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
        INSTANCE.registerMessage(index++, PlayerJoinSyncPacket.class, PlayerJoinSyncPacket::encode, PlayerJoinSyncPacket::decode, PlayerJoinSyncPacket::handle);
        
        // 在registerMessages方法中添加数据包注册
        INSTANCE.registerMessage(index++, DarkKivaBatModePacket.class, DarkKivaBatModePacket::encode, DarkKivaBatModePacket::new, DarkKivaBatModePacket::handle);
        INSTANCE.registerMessage(index++, DarkKivaBloodSuckPacket.class, DarkKivaBloodSuckPacket::encode, DarkKivaBloodSuckPacket::new, DarkKivaBloodSuckPacket::handle);
        INSTANCE.registerMessage(index++, DarkKivaSonicBlastPacket.class, DarkKivaSonicBlastPacket::encode, DarkKivaSonicBlastPacket::new, DarkKivaSonicBlastPacket::handle);
        INSTANCE.registerMessage(index++, DriverSyncPacket.class,
                DriverSyncPacket::encode,
                DriverSyncPacket::decode,
                DriverSyncPacket::handle);
        INSTANCE.registerMessage(index++, XKeyLoadPacket.class,
                XKeyLoadPacket::encode, XKeyLoadPacket::decode, XKeyLoadPacket::handle);
        INSTANCE.registerMessage(index++, XKeyEvilPacket.class,
                XKeyEvilPacket::encode, XKeyEvilPacket::decode, XKeyEvilPacket::handle);
        INSTANCE.registerMessage(index++, LeftClickShiftPacket.class,
                LeftClickShiftPacket::encode,
                LeftClickShiftPacket::decode,
                LeftClickShiftPacket::handle);
        INSTANCE.registerMessage(index++, WeaponSyncPacket.class,
                WeaponSyncPacket::encode,
                WeaponSyncPacket::decode,
                WeaponSyncPacket::handle);
        // 在 PacketHandler 的 register 方法中添加
        INSTANCE.registerMessage(
                index++,
                BatDarksAnimationPacket.class,
                BatDarksAnimationPacket::encode,
                BatDarksAnimationPacket::decode,
                BatDarksAnimationPacket::handle
        );
        INSTANCE.registerMessage(index++, WeaponRemovePacket.class,
                WeaponRemovePacket::encode, WeaponRemovePacket::decode, WeaponRemovePacket::handle);

        INSTANCE.registerMessage(
                index++,
                KnecromGhostAnimationPacket.class,
                KnecromGhostAnimationPacket::encode,
                KnecromGhostAnimationPacket::decode,
                KnecromGhostAnimationPacket::handle
        );
        INSTANCE.registerMessage(
                index++,
                SummonDukeKnightPacket.class,
                SummonDukeKnightPacket::buffer,
                SummonDukeKnightPacket::newPacket,
                SummonDukeKnightPacket::handle);
        
        // 注册Duke战斗数据分析数据包
        INSTANCE.registerMessage(
                index++,
                DukeCombatAnalysisPacket.class,
                DukeCombatAnalysisPacket::buffer,
                DukeCombatAnalysisPacket::newPacket,
                DukeCombatAnalysisPacket::handle);
        
        // 注册基础巴隆香蕉能量数据包
        INSTANCE.registerMessage(
                index++,
                BaronBananaEnergyPacket.class,
                BaronBananaEnergyPacket::encode,
                BaronBananaEnergyPacket::new,
                BaronBananaEnergyPacket::handle);
        
        // 注册巴隆柠檬形态骑士2技能数据包
        INSTANCE.registerMessage(
                index++,
                BaronLemonAbilityPacket.class,
                BaronLemonAbilityPacket::toBytes,
                BaronLemonAbilityPacket::new,
                BaronLemonAbilityPacket::handle);
        
        // 注册黑暗铠武阵羽柠檬技能数据包
        INSTANCE.registerMessage(
                index++,
                DarkGaimKickEnhancePacket.class,
                DarkGaimKickEnhancePacket::encode,
                DarkGaimKickEnhancePacket::decode,
                DarkGaimKickEnhancePacket::handle);
        INSTANCE.registerMessage(
                index++,
                DarkGaimBlindnessFieldPacket.class,
                DarkGaimBlindnessFieldPacket::encode,
                DarkGaimBlindnessFieldPacket::decode,
                DarkGaimBlindnessFieldPacket::handle);
        INSTANCE.registerMessage(
                index++,
                DarkGaimHelheimCrackPacket.class,
                DarkGaimHelheimCrackPacket::encode,
                DarkGaimHelheimCrackPacket::decode,
                DarkGaimHelheimCrackPacket::handle);
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

    public static void sendAnimationToAll(Component animation, int entityId, boolean override) {
        PlayerAnimationPacket packet = new PlayerAnimationPacket(animation, entityId, override);
        sendToAll(packet);
    }

    public static void sendAnimationToClient(Component animation, int entityId, boolean override, ServerPlayer player) {
        PlayerAnimationPacket packet = new PlayerAnimationPacket(animation, entityId, override);
        sendToClient(packet, player);
    }

    public static void sendAnimationToAllTracking(Component animation, int entityId, boolean override, Entity entity) {
        PlayerAnimationPacket packet = new PlayerAnimationPacket(animation, entityId, override);
        sendToAllTracking(packet, entity);
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

    public static void sendToAllTrackingAndSelf(Object packet, Entity entity) {
        if (entity != null && !entity.level().isClientSide && entity instanceof ServerPlayer serverPlayer) {
            // 发送给所有追踪者
            INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
            // 发送给玩家自己
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
    }

    public static void sendToAllTrackingExcept(Object packet, Entity entity, ServerPlayer exclude) {
        if (entity != null && !entity.level().isClientSide()) {
            // 获取所有追踪该实体的玩家，然后手动排除
            for (ServerPlayer trackingPlayer : ((ServerLevel)entity.level())
                    .getPlayers(player -> player != exclude && player.distanceToSqr(entity) < 64 * 64)) {
                sendToClient(packet, trackingPlayer);
            }
        }
    }
}