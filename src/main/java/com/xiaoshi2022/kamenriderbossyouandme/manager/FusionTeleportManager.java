// manager/FusionTeleportManager.java
package com.xiaoshi2022.kamenriderbossyouandme.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FusionTeleportManager {

    private static final Map<UUID, Long> TELEPORT_COOLDOWN = new ConcurrentHashMap<>();
    private static final Map<UUID, BlockPos> HELL_POSITIONS = new ConcurrentHashMap<>();
    private static final long TELEPORT_COOLDOWN_TIME = 30000;

    public static boolean teleportToHell(Player partner, Player transformer) {
        if (partner == null || partner.level().isClientSide()) return false;

        ServerPlayer player = (ServerPlayer) partner;

        UUID uuid = player.getUUID();
        if (TELEPORT_COOLDOWN.containsKey(uuid)) {
            long lastTeleport = TELEPORT_COOLDOWN.get(uuid);
            if (System.currentTimeMillis() - lastTeleport < TELEPORT_COOLDOWN_TIME) {
                player.sendSystemMessage(
                        Component.literal("§c⏳ 传送冷却中！请等待 "
                                + ((TELEPORT_COOLDOWN_TIME - (System.currentTimeMillis() - lastTeleport)) / 1000)
                                + " 秒")
                );
                return false;
            }
        }

        ServerLevel hellLevel = player.getServer().getLevel(Level.NETHER);
        if (hellLevel == null) {
            player.sendSystemMessage(Component.literal("§c❌ 无法进入地狱！"));
            return false;
        }

        BlockPos hellPos = getHellSpawnPosition(hellLevel, player.blockPosition());

        TELEPORT_COOLDOWN.put(uuid, System.currentTimeMillis());
        HELL_POSITIONS.put(uuid, hellPos);

        // 修复: 使用正确的 teleportTo 方法 (1.21.1)
        // 使用 EnumSet.noneOf(RelativeMovement.class) 表示没有任何相对移动
        player.teleportTo(
                hellLevel,
                hellPos.getX() + 0.5,
                hellPos.getY() + 1.0,
                hellPos.getZ() + 0.5,
                EnumSet.noneOf(RelativeMovement.class),
                player.getYRot(),
                player.getXRot()
        );

        player.sendSystemMessage(
                Component.literal("§c🔥 你被 §6" + transformer.getName().getString()
                        + " §c融合了！被流放到了地狱！")
        );

        transformer.sendSystemMessage(
                Component.literal("§a🔥 融合者 §6" + player.getName().getString()
                        + " §a被流放到了地狱！")
        );

        applyHellPunishment(player);

        return true;
    }

    private static BlockPos getHellSpawnPosition(ServerLevel hellLevel, BlockPos overworldPos) {
        int hellX = overworldPos.getX() / 8;
        int hellZ = overworldPos.getZ() / 8;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(hellX, 64, hellZ);

        for (int y = 64; y < 120; y++) {
            pos.setY(y);
            if (isSafePosition(hellLevel, pos)) {
                return pos.immutable();
            }
        }

        for (int y = 64; y > 10; y--) {
            pos.setY(y);
            if (isSafePosition(hellLevel, pos)) {
                return pos.immutable();
            }
        }

        return new BlockPos(hellX, 70, hellZ);
    }

    private static boolean isSafePosition(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).isSolid()
                && !level.getBlockState(pos.above()).isSolid()
                && level.getBlockState(pos.below()).isSolid();
    }

    private static void applyHellPunishment(ServerPlayer player) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS,
                20 * 60,
                1
        ));

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.CONFUSION,
                20 * 30,
                0
        ));
    }

    public static BlockPos getHellPosition(Player player) {
        return HELL_POSITIONS.get(player.getUUID());
    }

    public static boolean teleportBackFromHell(Player player) {
        if (player == null || player.level().isClientSide()) return false;

        ServerPlayer serverPlayer = (ServerPlayer) player;

        if (player.level().dimension() != Level.NETHER) {
            player.sendSystemMessage(Component.literal("§e你不在地狱！"));
            return false;
        }

        ServerLevel overworld = serverPlayer.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return false;

        BlockPos hellPos = player.blockPosition();
        BlockPos overworldPos = new BlockPos(
                hellPos.getX() * 8,
                64,
                hellPos.getZ() * 8
        );

        BlockPos safePos = findSafePosition(overworld, overworldPos);

        // 修复: 使用正确的 teleportTo 方法
        serverPlayer.teleportTo(
                overworld,
                safePos.getX() + 0.5,
                safePos.getY() + 1.0,
                safePos.getZ() + 0.5,
                EnumSet.noneOf(RelativeMovement.class),
                serverPlayer.getYRot(),
                serverPlayer.getXRot()
        );

        TELEPORT_COOLDOWN.remove(player.getUUID());
        HELL_POSITIONS.remove(player.getUUID());

        player.sendSystemMessage(
                Component.literal("§a✅ 你成功从地狱返回！但请记住被融合的代价！")
        );

        return true;
    }

    private static BlockPos findSafePosition(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int y = 64; y < 120; y++) {
            mutable.set(pos.getX(), y, pos.getZ());
            if (isSafePosition(level, mutable)) {
                return mutable.immutable();
            }
        }

        return new BlockPos(pos.getX(), 70, pos.getZ());
    }

    public static int getRemainingCooldown(Player player) {
        if (player == null) return 0;
        Long last = TELEPORT_COOLDOWN.get(player.getUUID());
        if (last == null) return 0;
        long remaining = (last + TELEPORT_COOLDOWN_TIME - System.currentTimeMillis()) / 1000;
        return (int) Math.max(0, remaining);
    }

    public static boolean isInCooldown(Player player) {
        return getRemainingCooldown(player) > 0;
    }
}