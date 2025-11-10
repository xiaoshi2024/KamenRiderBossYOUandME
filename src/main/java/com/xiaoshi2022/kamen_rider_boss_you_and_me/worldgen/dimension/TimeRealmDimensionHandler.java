package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.SafeTeleportUtil;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class TimeRealmDimensionHandler {

    // 存储玩家在进入时间领域维度前的位置，以便返回时使用
    private static final Map<ServerPlayer, BlockPos> playerLastPositions = new HashMap<>();

    /**
     * 处理玩家进入时间领域维度的事件
     */
    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation dimension = event.getDimension().location();
            // 如果玩家正在进入时间领域维度
            if (dimension.equals(TimeRealmDimension.TIME_REALM_DIM.location())) {
                // 记录玩家进入前的位置
                savePlayerLastPosition(player);

                // 确保时间领域维度已被加载
                ensureTimeRealmLoaded(player);
            }
        }
    }

    /**
     * 处理玩家维度切换后的事件，确保玩家正确生成在时间领域
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 如果玩家刚刚进入时间领域维度
            if (player.level().dimension().location().equals(TimeRealmDimension.TIME_REALM_DIM.location())) {
                // 初始化时间领域维度
                TimeRealmDimension.initializeDimension(player.serverLevel());

                // 确保玩家生成在安全位置
                teleportToSafeSpawn(player);
            }
        }
    }

    /**
     * 处理玩家重生事件，确保在时间领域死亡后正确重生
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 如果玩家在时间领域死亡并选择在时间领域重生
            if (player.level().dimension().location().equals(TimeRealmDimension.TIME_REALM_DIM.location())) {
                // 确保玩家重生在安全位置
                teleportToSafeSpawn(player);
            }
        }
    }

    /**
     * 处理玩家死亡事件，清除其保存的位置信息
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (isPlayerInTimeRealm(player)) {
                // 玩家在时间领域死亡，清除其位置记录
                playerLastPositions.remove(player);
            }
        }
    }

    /**
     * 保存玩家进入时间领域前的位置
     */
    private static void savePlayerLastPosition(ServerPlayer player) {
        playerLastPositions.put(player, player.blockPosition());
    }

    /**
     * 确保时间领域维度已被加载
     */
    private static void ensureTimeRealmLoaded(ServerPlayer player) {
        ServerLevel timeRealm = player.server.getLevel(TimeRealmDimension.TIME_REALM_DIM);
        if (timeRealm != null) {
            // 初始化维度
            TimeRealmDimension.initializeDimension(timeRealm);
        }
    }

    /**
     * 将玩家传送到时间领域，优先使用玩家在上一个维度的位置
     */
    private static void teleportToSafeSpawn(ServerPlayer player) {
        ServerLevel timeRealm = player.server.getLevel(TimeRealmDimension.TIME_REALM_DIM);
        if (timeRealm != null) {
            // 获取玩家在进入前保存的位置
            BlockPos lastPosition = playerLastPositions.get(player);
            if (lastPosition != null) {
                // 基于玩家上一个维度的位置，在时间领域中找到安全位置
                BlockPos targetPos = new BlockPos(lastPosition.getX(), 70, lastPosition.getZ());
                BlockPos safePos = SafeTeleportUtil.findOrCreateSafePosition(timeRealm, targetPos);
                player.teleportTo(timeRealm, safePos.getX() + 0.5D, safePos.getY() + 0.1D, safePos.getZ() + 0.5D, player.getYRot(), player.getXRot());
            } else {
                // 如果没有保存的位置，使用默认出生点
                BlockPos safePos = TimeRealmDimension.getSafeSpawnPosition(timeRealm);
                player.teleportTo(timeRealm, safePos.getX() + 0.5D, safePos.getY(), safePos.getZ() + 0.5D, player.getYRot(), player.getXRot());
            }
        }
    }

    /**
     * 将玩家从时间领域传送回主世界或上一个维度
     */
    public static void teleportPlayerBack(ServerPlayer player) {
        if (isPlayerInTimeRealm(player)) {
            // 获取玩家保存的位置
            BlockPos lastPosition = playerLastPositions.get(player);

            if (lastPosition != null) {
                // 传送回主世界或上一个维度
                ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    player.teleportTo(overworld, lastPosition.getX() + 0.5D, lastPosition.getY(), lastPosition.getZ() + 0.5D, player.getYRot(), player.getXRot());
                    // 清除保存的位置信息
                    playerLastPositions.remove(player);
                }
            } else {
                // 如果没有保存的位置信息，传送到主世界出生点
                ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    BlockPos spawnPos = overworld.getSharedSpawnPos();
                    player.teleportTo(overworld, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
                }
            }
        }
    }

    /**
     * 将玩家传送到时间领域，使用玩家当前位置的对应坐标
     */
    public static void teleportPlayerToTimeRealm(ServerPlayer player) {
        ServerLevel timeRealm = player.server.getLevel(TimeRealmDimension.TIME_REALM_DIM);
        if (timeRealm != null && !isPlayerInTimeRealm(player)) {
            // 保存当前位置
            savePlayerLastPosition(player);

            // 初始化维度
            TimeRealmDimension.initializeDimension(timeRealm);

            // 基于玩家当前位置，在时间领域中找到安全位置
            BlockPos currentPos = player.blockPosition();
            BlockPos targetPos = new BlockPos(currentPos.getX(), 70, currentPos.getZ());
            BlockPos safePos = SafeTeleportUtil.findOrCreateSafePosition(timeRealm, targetPos);

            // 传送玩家
            player.teleportTo(timeRealm, safePos.getX() + 0.5D, safePos.getY() + 0.1D, safePos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        }
    }

    /**
     * 检查玩家是否在时间领域维度
     */
    public static boolean isPlayerInTimeRealm(ServerPlayer player) {
        return player.level().dimension().location().equals(TimeRealmDimension.TIME_REALM_DIM.location());
    }

    /**
     * 处理时间领域中的玩家tick事件，用于持续检查和维护
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide &&
                event.level.dimension().location().equals(TimeRealmDimension.TIME_REALM_DIM.location())) {

            ServerLevel level = (ServerLevel) event.level;

            // 每100tick（约5秒）执行一次检查
            if (level.getGameTime() % 100 == 0) {
                // 确保维度已初始化
                if (!TimeRealmDimension.isDimensionInitialized(level)) {
                    TimeRealmDimension.initializeDimension(level);
                }
            }
        }
    }
}