package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 安全传送工具类，提供在不同维度间安全传送玩家的方法
 */
public class SafeTeleportUtil {
    
    /**
     * 寻找或创建一个安全的传送位置
     * @param level 服务器世界
     * @param startPos 起始搜索位置
     * @return 安全的传送位置
     */
    public static BlockPos findOrCreateSafePosition(ServerLevel level, BlockPos startPos) {
        // 首先检查起始位置是否安全
        if (isPositionSafe(level, startPos)) {
            return startPos;
        }
        
        // 在周围寻找安全位置，使用扩大的搜索范围
        int searchRadius = 15; // 增加搜索半径以提高找到安全位置的概率
        int verticalSearchRange = 20; // 增加垂直搜索范围
        
        // 优先搜索起始位置周围的区域
        for (int y = -verticalSearchRange; y <= verticalSearchRange; y++) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos pos = startPos.offset(x, y, z);
                    if (isPositionSafe(level, pos)) {
                        return pos;
                    }
                }
            }
        }
        
        // 如果没有找到安全位置，创建一个临时平台
        BlockPos platformPos = new BlockPos(startPos.getX(), Math.max(1, Math.min(level.getHeight() - 10, startPos.getY())), startPos.getZ());
        createTemporaryPlatform(level, platformPos);
        return platformPos.above(); // 返回平台上方的安全位置
    }
    
    /**
     * 检查指定位置是否安全
     * @param level 服务器世界
     * @param pos 要检查的位置
     * @return 位置是否安全
     */
    public static boolean isPositionSafe(ServerLevel level, BlockPos pos) {
        // 检查Y坐标是否在有效范围内
        if (pos.getY() < 1 || pos.getY() > level.getHeight() - 2) {
            return false;
        }
        
        // 检查脚下是否有方块支撑
        BlockPos below = pos.below();
        if (level.isEmptyBlock(below) || !level.getBlockState(below).isSolidRender(level, below)) {
            return false;
        }
        
        // 检查玩家碰撞箱范围内是否有方块
        // 玩家的碰撞箱通常是1x1x2，所以我们需要检查当前位置和上方一格
        for (int yOffset = 0; yOffset <= 1; yOffset++) {
            BlockPos checkPos = pos.offset(0, yOffset, 0);
            BlockState state = level.getBlockState(checkPos);
            
            // 检查是否有方块会阻挡玩家
            if (state.blocksMotion() || !state.canBeReplaced()) {
                return false;
            }
            
            // 检查是否是危险方块（如岩浆、火）
            if (state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                return false;
            }
        }
        
        // 检查头部空间是否足够（上方两格）
        BlockPos headPos = pos.above(2);
        if (!level.isEmptyBlock(headPos) && level.getBlockState(headPos).blocksMotion()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 创建一个临时平台供玩家站立
     * @param level 服务器世界
     * @param center 平台中心位置
     */
    public static void createTemporaryPlatform(ServerLevel level, BlockPos center) {
        // 使用石头创建一个5x5的平台，以确保更大的安全区域
        BlockState platformState = Blocks.STONE.defaultBlockState();
        int platformSize = 2; // 向四周延伸的距离
        
        for (int x = -platformSize; x <= platformSize; x++) {
            for (int z = -platformSize; z <= platformSize; z++) {
                BlockPos pos = center.offset(x, 0, z);
                // 只在空气或可替换方块上放置平台方块
                if (level.isEmptyBlock(pos) || level.getBlockState(pos).canBeReplaced()) {
                    level.setBlock(pos, platformState, 3);
                }
            }
        }
        
        // 清理平台上方的方块，确保有足够的空间
        for (int y = 1; y <= 3; y++) {
            for (int x = -platformSize; x <= platformSize; x++) {
                for (int z = -platformSize; z <= platformSize; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    // 移除阻挡方块
                    if (state.blocksMotion() || !state.canBeReplaced()) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }
    
    /**
     * 执行安全的维度间传送
     * @param player 要传送的玩家
     * @param targetLevel 目标维度
     * @param targetPos 目标位置
     */
    public static void safelyTeleport(ServerPlayer player, ServerLevel targetLevel, BlockPos targetPos) {
        // 找到安全的传送位置
        BlockPos safePos = findOrCreateSafePosition(targetLevel, targetPos);
        
        // 保存玩家的生命和经验
        float health = player.getHealth();
        int experienceLevel = player.experienceLevel;
        float experienceProgress = player.experienceProgress;
        
        // 执行传送
        player.teleportTo(
            targetLevel,
            safePos.getX() + 0.5D,  // 中心位置
            safePos.getY() + 0.1D,  // 稍微高一点，避免碰撞
            safePos.getZ() + 0.5D,
            player.getYRot(),
            player.getXRot()
        );
        
        // 恢复玩家的生命和经验
        player.setHealth(health);
        player.experienceLevel = experienceLevel;
        player.experienceProgress = experienceProgress;
        
        // 确保玩家不会卡在方块中
        player.fallDistance = 0.0F;
        player.setDeltaMovement(0, 0, 0);
        
        // 检查玩家是否在水中，如果是则添加空气
        if (player.isInWater()) {
            player.setAirSupply(player.getMaxAirSupply());
        }
        
        // 确保玩家在传送后不会立即受伤
        player.hurtTime = 0;
        player.invulnerableTime = 20; // 给玩家20刻（1秒）的无敌时间
    }
    
    /**
     * 为死亡后重生的玩家执行特别安全的传送
     * @param player 要传送的玩家
     * @param targetLevel 目标维度
     * @param targetPos 目标位置
     */
    public static void safelyTeleportAfterDeath(ServerPlayer player, ServerLevel targetLevel, BlockPos targetPos) {
        // 使用更严格的安全检查
        BlockPos safePos = findOrCreateSafePosition(targetLevel, targetPos);
        
        // 设置重生点为安全位置
        player.setRespawnPosition(targetLevel.dimension(), safePos, 0.0F, true, false);
        
        // 完全恢复玩家状态
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        player.clearFire();
        player.removeAllEffects();
        
        // 执行传送
        player.teleportTo(
            targetLevel,
            safePos.getX() + 0.5D,
            safePos.getY() + 0.1D,
            safePos.getZ() + 0.5D,
            player.getYRot(),
            player.getXRot()
        );
        
        // 确保玩家不会卡在方块中
        player.fallDistance = 0.0F;
        player.setDeltaMovement(0, 0, 0);
        
        // 给予更长的无敌时间
        player.invulnerableTime = 100; // 给玩家100刻（5秒）的无敌时间
    }
}