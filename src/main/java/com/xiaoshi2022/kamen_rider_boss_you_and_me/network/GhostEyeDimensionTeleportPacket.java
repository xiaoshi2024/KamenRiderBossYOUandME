package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.GhostEyeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

/**
 * 眼魔维度传送数据包
 * 当玩家手持眼魂按下Shift+V键时发送到服务器，触发主世界和眼魔世界之间的传送
 * 当玩家手持眼魂按下V键时发送到服务器，触发设置眼魔种族
 */
public class GhostEyeDimensionTeleportPacket {
    private final boolean isTeleport; // true表示传送，false表示设置眼魔种族
    
    // 直接在这个类中定义眼魔维度的键
    private static final ResourceKey<Level> GHOST_EYE_DIM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "ghost_eye")
    );

    public GhostEyeDimensionTeleportPacket(boolean isTeleport) {
        this.isTeleport = isTeleport;
    }

    public static void encode(GhostEyeDimensionTeleportPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isTeleport);
    }

    public static GhostEyeDimensionTeleportPacket decode(FriendlyByteBuf buffer) {
        boolean isTeleport = buffer.readBoolean();
        return new GhostEyeDimensionTeleportPacket(isTeleport);
    }

    public static void handle(GhostEyeDimensionTeleportPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (packet.isTeleport) {
                    // 执行传送操作
                    if (player.level().dimension() == Level.OVERWORLD) {
                        // 从主世界传送到眼魔世界
                        transferToGhostEyeDimension(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已传送到眼魔世界！"), true);
                    } else if (player.level().dimension() == GHOST_EYE_DIM) {
                        // 从眼魔世界传送到主世界
                        transferToOverworld(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已传送到主世界！"), true);
                    } else {
                        // 其他维度的情况，默认传送到眼魔世界
                        transferToGhostEyeDimension(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已传送到眼魔世界！"), true);
                    }
                } else {
                    // 执行设置眼魔种族操作
                    if (player.level().dimension() == Level.OVERWORLD && isHoldingGhostEye(player)) {
                        setPlayerAsGhostEye(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已获得眼魂种族！"), true);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
    
    // 将玩家传送到眼魔世界
    private static void transferToGhostEyeDimension(ServerPlayer player) {
        ServerLevel ghostEyeLevel = player.server.getLevel(GHOST_EYE_DIM);
        if (ghostEyeLevel == null) return;

        // 创建一个安全的传送位置
        BlockPos teleportPos = new BlockPos(0, 64, 0); // 使用固定坐标
        BlockPos safeTeleportPos = findOrCreateSafePosition(ghostEyeLevel, teleportPos);
        
        // 设置眼魔世界的重生点
        player.setRespawnPosition(GHOST_EYE_DIM, safeTeleportPos, 0.0F, true, false);

        // 传送到眼魔世界的安全位置
        player.teleportTo(ghostEyeLevel,
                safeTeleportPos.getX() + 0.5D,
                safeTeleportPos.getY() + 1.0D,
                safeTeleportPos.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot());

        // 恢复生命值
        player.setHealth(player.getMaxHealth());

        // 使用GhostEyeManager统一设置眼魔状态和相关buff效果
        // 检查是否是第一次设置眼魔状态
        boolean isFirstTime = !GhostEyeManager.isGhostEye(player);
        GhostEyeManager.setGhostEyeState(player, isFirstTime);
    }
    
    // 将玩家传送到主世界
    private static void transferToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        
        // 使用主世界的出生点或随机位置
        BlockPos spawnPos = overworld.getSharedSpawnPos();
        BlockPos safeSpawnPos = findOrCreateSafePosition(overworld, spawnPos);
        
        // 传送到主世界
        player.teleportTo(overworld,
                safeSpawnPos.getX() + 0.5D,
                safeSpawnPos.getY() + 1.0D,
                safeSpawnPos.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot());
        
        // 使用GhostEyeManager统一移除眼魔状态和相关buff效果
        GhostEyeManager.removeGhostEyeState(player);
    }
    
    // 设置玩家为眼魔种族
    private static void setPlayerAsGhostEye(ServerPlayer player) {
        // 保存玩家当前位置
        BlockPos currentPos = player.blockPosition();
        
        // 使用GhostEyeManager统一设置眼魔状态和相关buff效果
        // 检查是否是第一次设置眼魔状态
        boolean isFirstTime = !GhostEyeManager.isGhostEye(player);
        GhostEyeManager.setGhostEyeState(player, isFirstTime);
    }
    
    // 寻找或创建一个安全的位置
    private static BlockPos findOrCreateSafePosition(ServerLevel level, BlockPos startPos) {
        // 首先检查起始位置是否安全
        if (isPositionSafe(level, startPos)) {
            return startPos;
        }
        
        // 在周围寻找安全位置
        int searchRadius = 10;
        for (int y = -5; y <= 10; y++) {
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
        BlockPos platformPos = new BlockPos(startPos.getX(), 64, startPos.getZ());
        createTemporaryPlatform(level, platformPos);
        return platformPos.above();
    }
    
    // 检查指定位置是否安全
    private static boolean isPositionSafe(ServerLevel level, BlockPos pos) {
        // 检查Y坐标是否在有效范围内
        if (pos.getY() < 1 || pos.getY() > level.getHeight()) {
            return false;
        }
        
        // 检查脚下是否有方块
        BlockPos below = pos.below();
        if (level.isEmptyBlock(below)) {
            return false;
        }
        
        // 检查当前位置和上方是否有足够的空间
        return !level.getBlockState(pos).blocksMotion() && 
               !level.getBlockState(pos.above()).blocksMotion();
    }
    
    // 创建一个临时平台
    private static void createTemporaryPlatform(ServerLevel level, BlockPos center) {
        // 使用石头创建一个3x3的小平台
        net.minecraft.world.level.block.Block platformBlock = net.minecraft.world.level.block.Blocks.STONE;
        
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = center.offset(x, 0, z);
                level.setBlock(pos, platformBlock.defaultBlockState(), 3);
            }
        }
    }
    
    // 检查玩家是否手持眼魂
    private static boolean isHoldingGhostEye(Player player) {
        // 检查主手或副手是否持有眼魂
        if (player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye ||
            player.getOffhandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye) {
            return true;
        }
        return false;
    }
}