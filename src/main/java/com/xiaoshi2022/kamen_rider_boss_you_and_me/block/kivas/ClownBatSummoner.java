package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClownBatSummoner {

    // 祭坛结构定义 - 三角形黄金块 + 钻石块
    private static final BlockPos[] ALTAR_STRUCTURE = {
            // 第一层：三角形黄金块
            new BlockPos(3, 0, 0),    // 右
            new BlockPos(-2, 0, 3),   // 左前
            new BlockPos(-2, 0, -3),  // 左后

            // 第二层：钻石块装饰
            new BlockPos(4, 0, 0),    // 外右
            new BlockPos(-3, 0, 4),   // 外左前
            new BlockPos(-3, 0, -4),  // 外左后
            new BlockPos(0, 0, 4),    // 前
            new BlockPos(0, 0, -4)    // 后
    };

    // 在 ClownBatSummoner 类中添加这些方法
    public static void resetPlayerCooldown(Player player) {
        playerCooldowns.remove(player.getUUID());
    }

    public static void resetAllCooldowns() {
        playerCooldowns.clear();
    }

    // 获取玩家冷却状态信息（用于调试）
    public static String getPlayerCooldownInfo(Player player) {
        UUID playerId = player.getUUID();
        if (playerCooldowns.containsKey(playerId)) {
            long lastSummonTime = playerCooldowns.get(playerId);
            long remaining = Math.max(0, COOLDOWN_TIME - (System.currentTimeMillis() - lastSummonTime));
            return String.format("玩家 %s 冷却剩余: %s", player.getGameProfile().getName(), formatTime(remaining));
        }
        return String.format("玩家 %s 无冷却", player.getGameProfile().getName());
    }

    // 玩家冷却时间存储（玩家UUID -> 最后召唤时间戳）
    private static final Map<UUID, Long> playerCooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 30 * 60 * 1000; // 30分钟冷却（毫秒）

    public static boolean trySummonClownBat(Player player, Level level, BlockPos thronePos) {
        // 检查玩家冷却时间
        if (isPlayerOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            player.sendSystemMessage(Component.translatable("message.summon.cooldown", formatTime(remaining)));
            return false;
        }

        // 检查祭坛结构
        if (!checkAltarStructure(level, thronePos)) {
            player.sendSystemMessage(Component.translatable("message.summon.altar_incomplete"));
            return false;
        }

        // 检查是否已经存在小丑蝙蝠（全局检查）
        if (hasExistingClownBat(level, thronePos)) {
            player.sendSystemMessage(Component.translatable("message.summon.already_exists"));
            return false;
        }

        // 检查玩家是否已经有小丑蝙蝠
        if (hasPlayerClownBat(player, level)) {
            player.sendSystemMessage(Component.translatable("message.summon.player_has_bat"));
            return false;
        }

        // 召唤小丑蝙蝠
        boolean success = summonClownBat(player, level, thronePos);
        if (success) {
            // 设置玩家冷却时间
            setPlayerCooldown(player);
        }
        return success;
    }

    // 检查玩家冷却时间
    private static boolean isPlayerOnCooldown(Player player) {
        UUID playerId = player.getUUID();
        if (playerCooldowns.containsKey(playerId)) {
            long lastSummonTime = playerCooldowns.get(playerId);
            return System.currentTimeMillis() - lastSummonTime < COOLDOWN_TIME;
        }
        return false;
    }

    // 获取剩余冷却时间（毫秒）
    private static long getRemainingCooldown(Player player) {
        UUID playerId = player.getUUID();
        if (playerCooldowns.containsKey(playerId)) {
            long lastSummonTime = playerCooldowns.get(playerId);
            long elapsed = System.currentTimeMillis() - lastSummonTime;
            return Math.max(0, COOLDOWN_TIME - elapsed);
        }
        return 0;
    }

    // 设置玩家冷却时间
    private static void setPlayerCooldown(Player player) {
        playerCooldowns.put(player.getUUID(), System.currentTimeMillis());
    }

    // 检查玩家是否已经有小丑蝙蝠
    public static boolean hasPlayerClownBat(Player player, Level level) {
        return !level.getEntitiesOfClass(KivatBatTwoNd.class,
                new AABB(player.blockPosition()).inflate(128), // 128格范围内检查
                bat -> bat.isTame() && bat.isOwnedBy(player) && bat.isAlive()
        ).isEmpty();
    }

    // 检查全局是否存在小丑蝙蝠
    private static boolean hasExistingClownBat(Level level, BlockPos center) {
        return !level.getEntitiesOfClass(KivatBatTwoNd.class,
                new AABB(center).inflate(64)).isEmpty(); // 64格范围内检查
    }

    // 格式化时间显示
    private static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + "小时" + (minutes % 60) + "分钟";
        } else if (minutes > 0) {
            return minutes + "分钟" + (seconds % 60) + "秒";
        } else {
            return seconds + "秒";
        }
    }

    private static boolean checkAltarStructure(Level level, BlockPos center) {
        // 检查黄金块三角形
        if (!level.getBlockState(center.offset(3, 0, 0)).is(Blocks.GOLD_BLOCK) ||
                !level.getBlockState(center.offset(-2, 0, 3)).is(Blocks.GOLD_BLOCK) ||
                !level.getBlockState(center.offset(-2, 0, -3)).is(Blocks.GOLD_BLOCK)) {
            return false;
        }

        // 检查钻石块装饰（可选，但推荐）
        if (!level.getBlockState(center.offset(4, 0, 0)).is(Blocks.DIAMOND_BLOCK) ||
                !level.getBlockState(center.offset(-3, 0, 4)).is(Blocks.DIAMOND_BLOCK) ||
                !level.getBlockState(center.offset(-3, 0, -4)).is(Blocks.DIAMOND_BLOCK)) {
            return false;
        }

        return true;
    }

    private static boolean summonClownBat(Player player, Level level, BlockPos pos) {
        try {
            KivatBatTwoNd clownBat = ModEntityTypes.KIVAT_BAT_II.get().create(level);
            if (clownBat == null) return false;

            // 设置位置在王座上方
            clownBat.setPos(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5);

            // 设置初始状态
            clownBat.setSleeping(false);
            clownBat.setResting(false);
            clownBat.setFlying(true);

            // 驯服并设置主人
            clownBat.tame(player);

            level.addFreshEntity(clownBat);

            // 播放特效
            playSummonEffects(level, pos);

            // 修复：使用更简单的方式获取附近玩家
            AABB aabb = new AABB(pos).inflate(16, 16, 16); // 以王座为中心，半径为16格的范围

            // 获取附近的玩家
            List<Player> nearbyPlayers = level.getEntitiesOfClass(
                    Player.class,
                    aabb,
                    p -> p != null && p.isAlive()
            );

            // 发送召唤消息
            for (Player nearbyPlayer : nearbyPlayers) {
                nearbyPlayer.sendSystemMessage(Component.translatable("message.summon.success"));
            }

            return true;

        } catch (Exception e) {
            player.sendSystemMessage(Component.translatable("message.summon.failed"));
            return false;
        }
    }

    private static void playSummonEffects(Level level, BlockPos pos) {
        // 音效
        level.playSound(null, pos, SoundEvents.ILLUSIONER_PREPARE_MIRROR,
                SoundSource.HOSTILE, 2.0F, 0.5F);

        // 粒子效果
        if (level.isClientSide) {
            for (int i = 0; i < 50; i++) {
                double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 3;
                double y = pos.getY() + 1 + level.random.nextDouble() * 2;
                double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 3;

                level.addParticle(ParticleTypes.FLASH, x, y, z, 0, 0, 0);
                level.addParticle(ParticleTypes.SOUL, x, y, z,
                        (level.random.nextDouble() - 0.5) * 0.1,
                        level.random.nextDouble() * 0.1,
                        (level.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    // 可视化祭坛结构的调试方法
    public static void showAltarStructure(Player player, BlockPos center) {
        if (player.level().isClientSide) return;

        // 显示祭坛轮廓（临时粒子）
        for (BlockPos offset : ALTAR_STRUCTURE) {
            BlockPos showPos = center.offset(offset);
            if (player.level().isEmptyBlock(showPos)) {
                spawnGuideParticle(player.level(), showPos);
            }
        }

        player.sendSystemMessage(Component.translatable("message.summon.show_guide"));
    }

    private static void spawnGuideParticle(Level level, BlockPos pos) {
        if (level.isClientSide) {
            for (int i = 0; i < 5; i++) {
                level.addParticle(ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        0, 0.1, 0);
            }
        }
    }
}