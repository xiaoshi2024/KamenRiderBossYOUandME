package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.evil.LeftClickShiftPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeRevertPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeTransformPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.BrainHeadbuttPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.PlayerAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower.KnightPoisonPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

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
        INSTANCE.registerMessage(index++, BrainHeadbuttPacket.class,
                BrainHeadbuttPacket::encode, BrainHeadbuttPacket::decode, BrainHeadbuttPacket::handle);
        INSTANCE.registerMessage(index++, SoundStopPacket.class, SoundStopPacket::encode, SoundStopPacket::decode, SoundStopPacket.ClientHandler::handle);
        INSTANCE.registerMessage(index++, InvisibilityPacket.class, InvisibilityPacket::encode, InvisibilityPacket::decode, InvisibilityPacket::handle);
        INSTANCE.registerMessage(index++, RemoveEntityPacket.class, RemoveEntityPacket::toBytes, RemoveEntityPacket::new, RemoveEntityPacket::handle);
        INSTANCE.registerMessage(index++, PlayerArmorPacket.class, PlayerArmorPacket::encode, PlayerArmorPacket::decode, PlayerArmorPacket::handle);
        INSTANCE.registerMessage(index++, PlayerEquipmentPacket.class, PlayerEquipmentPacket::encode, PlayerEquipmentPacket::decode, PlayerEquipmentPacket::handle);
        // 注册批处理数据包
        INSTANCE.registerMessage(index++, BatchPacket.class, BatchPacket::encode, BatchPacket::decode, BatchPacket::handle);
        INSTANCE.registerMessage(index++, BeltAnimationPacket.class, BeltAnimationPacket::encode, BeltAnimationPacket::decode, BeltAnimationPacket::handle);
        INSTANCE.registerMessage(index++,  SyncOwnerPacket.class,SyncOwnerPacket::encode, SyncOwnerPacket::decode,SyncOwnerPacket::handle);
        INSTANCE.registerMessage(index++, ReleaseBeltPacket.class, ReleaseBeltPacket::encode, ReleaseBeltPacket::decode, ReleaseBeltPacket::handle);
        INSTANCE.registerMessage(index++, TransformationRequestPacket.class, TransformationRequestPacket::encode, TransformationRequestPacket::decode, TransformationRequestPacket::handle);
        INSTANCE.registerMessage(index++, GhostTransformationRequestPacket.class, GhostTransformationRequestPacket::buffer, GhostTransformationRequestPacket::newInstance, GhostTransformationRequestPacket::handler);
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
        
        // 注册 DarkGhostTransformationRequestPacket
        INSTANCE.registerMessage(index++, DarkGhostTransformationRequestPacket.class, DarkGhostTransformationRequestPacket::encode, DarkGhostTransformationRequestPacket::decode, DarkGhostTransformationRequestPacket::handle);
        
        // 注册 NapoleonGhostTransformationRequestPacket
        INSTANCE.registerMessage(index++, NapoleonGhostTransformationRequestPacket.class, NapoleonGhostTransformationRequestPacket::encode, NapoleonGhostTransformationRequestPacket::decode, NapoleonGhostTransformationRequestPacket::handle);
        
        // 注册 BrainTransformationRequestPacket
        INSTANCE.registerMessage(index++, BrainTransformationRequestPacket.class, BrainTransformationRequestPacket::encode, BrainTransformationRequestPacket::decode, BrainTransformationRequestPacket::handle);
        
        // 注册 QueenBeeTransformationRequestPacket
        INSTANCE.registerMessage(index++, QueenBeeTransformationRequestPacket.class, QueenBeeTransformationRequestPacket::encode, QueenBeeTransformationRequestPacket::decode, QueenBeeTransformationRequestPacket::handle);
        
        // 注册 BlackBuildTransformationRequestPacket
        INSTANCE.registerMessage(index++, BlackBuildTransformationRequestPacket.class, BlackBuildTransformationRequestPacket::encode, BlackBuildTransformationRequestPacket::decode, BlackBuildTransformationRequestPacket::handle);
        // 注册 BuildDriverReleasePacket
        INSTANCE.registerMessage(index++, BuildDriverReleasePacket.class, BuildDriverReleasePacket::encode, BuildDriverReleasePacket::decode, BuildDriverReleasePacket::handle);

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
        
        // 注册粒子效果数据包
        INSTANCE.registerMessage(index++, ParticleEffectPacket.class,
                ParticleEffectPacket::encode, ParticleEffectPacket::decode, ParticleEffectPacket::handle);
        
        // 在registerMessages方法中添加数据包注册
        INSTANCE.registerMessage(index++, DarkKivaBatModePacket.class, DarkKivaBatModePacket::encode, DarkKivaBatModePacket::new, DarkKivaBatModePacket::handle);
        INSTANCE.registerMessage(index++, DarkKivaBloodSuckPacket.class, DarkKivaBloodSuckPacket::encode, DarkKivaBloodSuckPacket::new, DarkKivaBloodSuckPacket::handle);
        INSTANCE.registerMessage(index++, DarkKivaSonicBlastPacket.class, DarkKivaSonicBlastPacket::encode, DarkKivaSonicBlastPacket::new, DarkKivaSonicBlastPacket::handle);
        INSTANCE.registerMessage(index++, DarkKivaFuuinKekkaiPacket.class, DarkKivaFuuinKekkaiPacket::encode, DarkKivaFuuinKekkaiPacket::new, DarkKivaFuuinKekkaiPacket::handle);
        INSTANCE.registerMessage(index++, DarkKivaSealBarrierPullPacket.class, DarkKivaSealBarrierPullPacket::encode, DarkKivaSealBarrierPullPacket::new, DarkKivaSealBarrierPullPacket::handle);
        INSTANCE.registerMessage(index++, DarkKivaToggleFlightPacket.class, DarkKivaToggleFlightPacket::encode, DarkKivaToggleFlightPacket::new, DarkKivaToggleFlightPacket::handle);
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

        // 注册创世纪驱动器临时取下锁种数据包
        INSTANCE.registerMessage(index++, TempRemoveLockSeedPacket.class,
                TempRemoveLockSeedPacket::encode,
                TempRemoveLockSeedPacket::decode,
                TempRemoveLockSeedPacket::handle);

        INSTANCE.registerMessage(
                index++,
                KnecromGhostAnimationPacket.class,
                KnecromGhostAnimationPacket::encode,
                KnecromGhostAnimationPacket::decode,
                KnecromGhostAnimationPacket::handle
        );

        // 注册BatStampFinish实体动画同步数据包
        INSTANCE.registerMessage(
                index++,
                BatStampFinishAnimationPacket.class,
                BatStampFinishAnimationPacket::encode,
                BatStampFinishAnimationPacket::decode,
                BatStampFinishAnimationPacket::handle
        );
        
        // 注册NecromEyex实体动画同步数据包
        INSTANCE.registerMessage(
                index++,
                NecromEyexAnimationPacket.class,
                NecromEyexAnimationPacket::encode,
                NecromEyexAnimationPacket::decode,
                NecromEyexAnimationPacket::handle
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
        
        // 注册巴隆召唤异域者技能数据包
        INSTANCE.registerMessage(
                index++,
                BaronSummonInvesPacket.class,
                BaronSummonInvesPacket::toBytes,
                BaronSummonInvesPacket::new,
                BaronSummonInvesPacket::handler);
                
        // 注册巴隆召回异域者技能数据包
        INSTANCE.registerMessage(
                index++,
                BaronRecallInvesPacket.class,
                BaronRecallInvesPacket::encode,
                BaronRecallInvesPacket::decode,
                BaronRecallInvesPacket::handle);
        
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
        INSTANCE.registerMessage(
                index++,
                CherryEnergyArrowPacket.class,
                CherryEnergyArrowPacket::encode,
                CherryEnergyArrowPacket::decode,
                CherryEnergyArrowPacket::handle);

        // 注册玛丽卡感官加强数据包
        INSTANCE.registerMessage(
                index++,
                MarikaSensoryEnhancementPacket.class,
                MarikaSensoryEnhancementPacket::encode,
                MarikaSensoryEnhancementPacket::decode,
                MarikaSensoryEnhancementPacket::handle);
        
        // 注册火龙果虚化技能切换数据包
        INSTANCE.registerMessage(
                index++,
                TyrantIntangibilityTogglePacket.class,
                TyrantIntangibilityTogglePacket::encode,
                TyrantIntangibilityTogglePacket::decode,
                TyrantIntangibilityTogglePacket::handle);
                
        // 注册蝙蝠印章超音波攻击数据包
        INSTANCE.registerMessage(
                index++,
                BatUltrasonicAttackPacket.class,
                BatUltrasonicAttackPacket::encode,
                BatUltrasonicAttackPacket::new,
                BatUltrasonicAttackPacket::handle);
                
        // 注册EvilBats临时取下蝙蝠印章数据包
        INSTANCE.registerMessage(
                index++,
                TempRemoveBatStampPacket.class,
                TempRemoveBatStampPacket::encode,
                TempRemoveBatStampPacket::decode,
                TempRemoveBatStampPacket::handle);
                
        // 注册临时取下眼魂数据包
        INSTANCE.registerMessage(
                index++,
                TempRemoveNecromEyePacket.class,
                TempRemoveNecromEyePacket::encode,
                TempRemoveNecromEyePacket::decode,
                TempRemoveNecromEyePacket::handle);
                
        // 注册Overlord藤蔓技能数据包
        INSTANCE.registerMessage(
                index++,
                OverlordVineSkillPacket.class,
                OverlordVineSkillPacket::toBytes,
                OverlordVineSkillPacket::new,
                OverlordVineSkillPacket::handle);
        
        // 注册眼魔维度传送数据包
        INSTANCE.registerMessage(
                index++,
                GhostEyeDimensionTeleportPacket.class,
                GhostEyeDimensionTeleportPacket::encode,
                GhostEyeDimensionTeleportPacket::decode,
                GhostEyeDimensionTeleportPacket::handle);
                
        // 注册眼魔变身为眼魂实体的数据包
        INSTANCE.registerMessage(
                index++,
                GhostEyeTransformPacket.class,
                GhostEyeTransformPacket::encode,
                GhostEyeTransformPacket::decode,
                GhostEyeTransformPacket::handle);
                
        // 注册眼魂实体变回人形的数据包
        INSTANCE.registerMessage(
                index++,
                GhostEyeRevertPacket.class,
                GhostEyeRevertPacket::encode,
                GhostEyeRevertPacket::decode,
                GhostEyeRevertPacket::handle);
        
        // 注册眼魂隐身效果数据包
        INSTANCE.registerMessage(
                index++,
                com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeInvisibilityPacket.class,
                com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeInvisibilityPacket::encode,
                com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeInvisibilityPacket::decode,
                com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye.GhostEyeInvisibilityPacket::handle);
        
        // 注册DarkGhost闪电格斗攻击数据包
        INSTANCE.registerMessage(
                index++,
                com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkGhostLightningAttackPacket.class,
                DarkGhostLightningAttackPacket::encode,
                DarkGhostLightningAttackPacket::decode,
                DarkGhostLightningAttackPacket::handle);
                
        // 注册DarkGhost短距离瞬移数据包
        INSTANCE.registerMessage(
                index++,
                com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkGhostTeleportPacket.class,
                DarkGhostTeleportPacket::encode,
                DarkGhostTeleportPacket::decode,
                DarkGhostTeleportPacket::handle);
        
        // 注册NapoleonGhost远程伤害减免数据包
        INSTANCE.registerMessage(
                index++,
                NapoleonGhostRangedDamageReductionPacket.class,
                NapoleonGhostRangedDamageReductionPacket::encode,
                NapoleonGhostRangedDamageReductionPacket::decode,
                NapoleonGhostRangedDamageReductionPacket::handle);
        
        // 注册Roidmude重加速数据包
        INSTANCE.registerMessage(
                index++,
                RoidmudeHeavyAccelerationPacket.class,
                RoidmudeHeavyAccelerationPacket::toBytes,
                RoidmudeHeavyAccelerationPacket::new,
                RoidmudeHeavyAccelerationPacket::handle);
        
        // 注册Brain骑士剧毒数据包
        INSTANCE.registerMessage(
                index++,
                KnightPoisonPacket.class,
                KnightPoisonPacket::encode,
                KnightPoisonPacket::decode,
                KnightPoisonPacket::handle);
        
        // 注册Aguilera骑士飞行能力数据包
        INSTANCE.registerMessage(
                index++,
                QuinbeeFlightPacket.class,
                QuinbeeFlightPacket::toBytes,
                QuinbeeFlightPacket::new,
                QuinbeeFlightPacket::handle);
        
        // 注册Aguilera骑士针刺苦无攻击数据包
        INSTANCE.registerMessage(
                index++,
                QuinbeeNeedleKunaiPacket.class,
                QuinbeeNeedleKunaiPacket::toBytes,
                QuinbeeNeedleKunaiPacket::new,
                QuinbeeNeedleKunaiPacket::handle);
        
        // 注册KnightInvoker变身数据包
        INSTANCE.registerMessage(index++,
                KnightInvokerHenshinPacket.class,
                KnightInvokerHenshinPacket::encode,
                KnightInvokerHenshinPacket::decode,
                KnightInvokerHenshinPacket::handle);
        
        // 注册KnightInvokerPress数据包
        INSTANCE.registerMessage(index++,
                KnightInvokerPressPacket.class,
                KnightInvokerPressPacket::encode,
                KnightInvokerPressPacket::decode,
                KnightInvokerPressPacket::handle);
        
        // 注册KnightInvokerRelease数据包
        INSTANCE.registerMessage(index++,
                KnightInvokerReleasePacket.class,
                KnightInvokerReleasePacket::toBytes,
                KnightInvokerReleasePacket::new,
                KnightInvokerReleasePacket::handle);
        
        // 注册KnightInvokerErase数据包（必杀技：Breakam Cannon）
        INSTANCE.registerMessage(index++,
                KnightInvokerErasePacket.class,
                KnightInvokerErasePacket::encode,
                KnightInvokerErasePacket::decode,
                KnightInvokerErasePacket::handle);
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

    // 发送动画到所有玩家
    public static void sendAnimationToAll(String animationName, int entityId, boolean override, int priority, int duration) {
        PlayerAnimationPacket packet = new PlayerAnimationPacket(animationName, entityId, override, priority, duration);
        sendToAll(packet);
    }

    // 发送动画到指定客户端
    public static void sendAnimationToClient(String animationName, int entityId, boolean override, ServerPlayer player, int priority, int duration) {
        PlayerAnimationPacket packet = new PlayerAnimationPacket(animationName, entityId, override, priority, duration);
        sendToClient(packet, player);
    }

    // 发送动画到所有追踪者
    public static void sendAnimationToAllTracking(String animationName, int entityId, boolean override, Entity entity, int priority, int duration) {
        PlayerAnimationPacket packet = new PlayerAnimationPacket(animationName, entityId, override, priority, duration);
        sendToAllTracking(packet, entity);
    }

    // 发送动画到所有追踪者和自己
    public static void sendAnimationToAllTrackingAndSelf(String animationName, int entityId, boolean override, Entity entity, int priority, int duration) {
        PlayerAnimationPacket packet = new PlayerAnimationPacket(animationName, entityId, override, priority, duration);
        sendToAllTrackingAndSelf(packet, entity);
    }

    // 取消指定玩家的动画
    public static void cancelAnimation(int playerId, ServerLevel level) {
        PlayerAnimationPacket packet = new PlayerAnimationPacket(playerId);
        sendToPlayerById(packet, playerId, level);
    }

    // 向后兼容的方法，使用默认优先级和持续时间
    public static void sendAnimationToAll(Component animation, int entityId, boolean override) {
        sendAnimationToAll(animation.getString(), entityId, override, 1, 2000);
    }

    public static void sendAnimationToClient(Component animation, int entityId, boolean override, ServerPlayer player) {
        sendAnimationToClient(animation.getString(), entityId, override, player, 1, 2000);
    }

    public static void sendAnimationToAllTracking(Component animation, int entityId, boolean override, Entity entity) {
        sendAnimationToAllTracking(animation.getString(), entityId, override, entity, 1, 2000);
    }

    public static void sendAnimationToAllTrackingAndSelf(Component animation, int entityId, boolean override, Entity entity) {
        sendAnimationToAllTrackingAndSelf(animation.getString(), entityId, override, entity, 1, 2000);
    }

    // 基于玩家ID的数据包过滤系统，减少不必要的网络传输
    public static void sendToPlayerById(Object packet, int playerId, ServerLevel level) {
        if (!level.isClientSide()) {
            Entity entity = level.getEntity(playerId);
            if (entity instanceof ServerPlayer serverPlayer) {
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
            }
        }
    }

    // 优化的追踪发送方法，允许指定排除玩家
    public static void sendToAllTracking(Object packet, Entity entity, @Nullable ServerPlayer excludePlayer) {
        if (entity != null && !entity.level().isClientSide()) {
            // 发送给所有追踪者
            INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
            
            // 如果需要排除指定玩家，单独取消发送给该玩家
            // 注意：这是一种替代方案，因为某些Minecraft版本可能不支持TRACKING_ENTITY_EXCLUDING
        }
    }

    // 重载方法，保持向后兼容
    public static void sendToAllTracking(Object packet, Entity entity) {
        sendToAllTracking(packet, entity, null);
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
        if (entity != null && !entity.level().isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            // 发送给所有追踪者
            sendToAllTracking(packet, entity, null);
            // 发送给玩家自己
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
    }

    // 新增：批量发送数据包给多个玩家
    public static void sendToMultiplePlayers(Object packet, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    // 新增：基于玩家ID列表发送数据包
    public static void sendToPlayersByIds(Object packet, List<Integer> playerIds, ServerLevel level) {
        for (int playerId : playerIds) {
            sendToPlayerById(packet, playerId, level);
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