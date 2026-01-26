package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class TimeRealmPlayerTickHandler {

    /**
     * 处理玩家在时间领域维度中的tick逻辑
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level level = player.level();

        // 只在服务器端和时间领域维度中执行
        if (event.phase == TickEvent.Phase.END && !level.isClientSide &&
                level.dimension().location().equals(TimeRealmDimension.TIME_REALM_DIM.location())) {
            handleTimeRealmEffects(player);

            // 确保维度已初始化
            if (player instanceof ServerPlayer) {
                initializeDimension((ServerPlayer) player);

                // 定期生成时劫者实体（作为维度中的平民）
                if (level.getGameTime() % 200 == 0) { // 每10秒检查一次
                    spawnTimeJackerAsCitizen((ServerLevel) level, player.blockPosition());

                    // 检查是否需要在附近的堡垒中生成时间王族
                    checkAndSpawnTimeRoyaltyInFortresses((ServerLevel) level, player.blockPosition());
                }

                // 确保玩家位于安全位置（处理意外掉落到虚空等情况）
                ensurePlayerIsSafe((ServerPlayer) player);
            }
        }
    }

    /**
     * 处理时间领域对玩家的影响
     */
    private static void handleTimeRealmEffects(Player player) {
        // 给玩家施加缓慢的时间效果，让他们感到时间流逝异常
        if (player.tickCount % 20 == 0) { // 每秒检查一次
            // 有30%的几率获得短暂的缓慢效果
            if (player.level().random.nextFloat() < 0.3f) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        40, // 2秒
                        0,
                        false,
                        false
                ));
            }

            // 有10%的几率获得短暂的急迫效果（时间加速的感觉）
            if (player.level().random.nextFloat() < 0.1f) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        20, // 1秒
                        0,
                        false,
                        false
                ));
            }
        }
    }

    /**
     * 初始化维度
     */
    private static void initializeDimension(ServerPlayer player) {
        TimeRealmDimension.initializeDimension(player.serverLevel());
    }

    /**
     * 确保玩家位于安全位置
     */
    private static void ensurePlayerIsSafe(ServerPlayer player) {
        // 检查玩家是否在极低点（可能掉入虚空）
        if (player.getY() < 0) {
            BlockPos safePos = TimeRealmDimension.getSafeSpawnPosition(player.serverLevel());
            player.teleportTo(safePos.getX(), safePos.getY(), safePos.getZ());
        }
    }

    /**
     * 在维度中生成时劫者实体作为平民
     */
    private static void spawnTimeJackerAsCitizen(ServerLevel level, BlockPos playerPos) {
        // 检查实体数量，避免生成过多
        AABB searchArea = new AABB(playerPos).inflate(100);
        int timeJackerCount = level.getEntitiesOfClass(com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity.class,
                searchArea).size();

        // 最多在附近生成10个时劫者（作为平民）
        if (timeJackerCount < 10 && level.random.nextFloat() < 0.5f) {
            spawnTimeJacker(level, playerPos);
        }
    }

    /**
     * 检查并在附近的堡垒中生成时间王族
     */
    private static void checkAndSpawnTimeRoyaltyInFortresses(ServerLevel level, BlockPos playerPos) {
        // 检查是否有时间王族在附近
        AABB searchArea = new AABB(playerPos).inflate(200);
        int timeRoyaltyCount = level.getEntitiesOfClass(com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeRoyaltyEntity.class,
                searchArea).size();

        // 如果附近时间王族数量较少，尝试在附近的堡垒结构中生成
        if (timeRoyaltyCount < 3 && level.random.nextFloat() < 0.2f) {
            // TODO: 实际游戏中，这里应该检查附近是否有堡垒结构
            // 目前简单实现：在玩家周围较大范围内随机生成
            if (level.random.nextFloat() < 0.1f) { // 较低概率生成
                spawnTimeRoyalty(level, playerPos);
            }
        }
    }

    /**
     * 生成时劫者实体（作为平民）
     */
    private static void spawnTimeJacker(ServerLevel level, BlockPos playerPos) {
        // 在玩家周围较大范围内随机位置生成
        int x = playerPos.getX() + level.random.nextInt(80) - 40;
        int z = playerPos.getZ() + level.random.nextInt(80) - 40;
        int y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).getY();

        if (y > 0) {
            BlockPos spawnPos = new BlockPos(x, y, z);
            com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity jacker =
                    (com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity) ModEntityTypes.TIME_JACKER.get().spawn(level, spawnPos, MobSpawnType.NATURAL);

            if (jacker != null) {
                jacker.setPersistenceRequired(); // 设置为持久化实体，不会因为距离过远而消失
                // 作为平民，时劫者应该保持中立或友好状态
                jacker.setNoAi(false); // 允许AI行为
            }
        }
    }

    /**
     * 生成时间王族实体（主要在堡垒中生成）
     */
    private static void spawnTimeRoyalty(ServerLevel level, BlockPos playerPos) {
        // 在玩家周围较大范围内随机位置生成（模拟在堡垒中）
        int x = playerPos.getX() + level.random.nextInt(100) - 50;
        int z = playerPos.getZ() + level.random.nextInt(100) - 50;
        int y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).getY();

        if (y > 0) {
            BlockPos spawnPos = new BlockPos(x, y, z);
            com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeRoyaltyEntity royalty =
                    (com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeRoyaltyEntity) ModEntityTypes.TIME_ROYALTY.get().spawn(level, spawnPos, MobSpawnType.NATURAL);

            if (royalty != null) {
                royalty.setPersistenceRequired(); // 设置为持久化实体
                // 给时间王族添加特殊效果，使其更加强大
                royalty.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        Integer.MAX_VALUE, // 永久效果
                        0,
                        false,
                        false
                ));

                // 添加额外的效果，使时间王族更具威胁性
                royalty.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        Integer.MAX_VALUE,
                        1,
                        false,
                        false
                ));
            }
        }
    }
}