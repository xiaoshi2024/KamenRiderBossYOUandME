package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DarkGhostTeleportPacket {

    public DarkGhostTeleportPacket() {
        // 空构造函数，用于数据包解析
    }

    // 从缓冲区读取数据
    public static DarkGhostTeleportPacket decode(FriendlyByteBuf buf) {
        return new DarkGhostTeleportPacket();
    }

    // 将数据写入缓冲区
    public static void encode(DarkGhostTeleportPacket packet, FriendlyByteBuf buf) {
        // 不需要额外数据
    }

    // 处理数据包
    public static void handle(DarkGhostTeleportPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level() instanceof ServerLevel) {
                // 执行短距离瞬移
                executeTeleport(player);
            }
        });
        context.setPacketHandled(true);
    }

    // 执行短距离瞬移
    private static void executeTeleport(ServerPlayer player) {
        // 消耗骑士能量（15点），如果能量不足则不执行瞬移
        if (!RiderEnergyHandler.consumeRiderEnergy(player, 15.0D)) {
            return;
        }
        
        ServerLevel world = (ServerLevel) player.level();
        double teleportDistance = 5.0D;
        
        // 在原位置生成粒子效果
        for (int i = 0; i < 15; i++) {
            double offsetX = player.getRandom().nextGaussian() * 0.3D;
            double offsetY = player.getRandom().nextGaussian() * 0.3D;
            double offsetZ = player.getRandom().nextGaussian() * 0.3D;
            world.sendParticles(ParticleTypes.SMOKE, 
                player.getX() + offsetX, 
                player.getY() + player.getEyeHeight() + offsetY, 
                player.getZ() + offsetZ, 
                1, 0, 0, 0, 0.05D);
        }
        
        // 使用玩家当前的视线方向计算目标位置
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        
        // 计算视线方向向量
        float cosYaw = (float) Math.cos(Math.toRadians(yaw + 90.0F));
        float sinYaw = (float) Math.sin(Math.toRadians(yaw + 90.0F));
        float cosPitch = (float) Math.cos(Math.toRadians(pitch));
        float sinPitch = (float) Math.sin(Math.toRadians(pitch));
        
        double lookX = cosYaw * cosPitch;
        double lookY = -sinPitch;
        double lookZ = sinYaw * cosPitch;
        
        // 计算目标位置（玩家视线方向的短距离）
        double targetX = player.getX() + (lookX * teleportDistance);
        double targetY = player.getY() + (lookY * teleportDistance) + 1.0D; // 稍微向上偏移，避免卡进地面
        double targetZ = player.getZ() + (lookZ * teleportDistance);
        
        // 确保目标位置是可站立的
        if (isValidTeleportPosition(world, targetX, targetY, targetZ)) {
            // 执行瞬移
            player.teleportTo(targetX, targetY, targetZ);
            
            // 在新位置生成粒子效果
            for (int i = 0; i < 15; i++) {
                double offsetX = player.getRandom().nextGaussian() * 0.3D;
                double offsetY = player.getRandom().nextGaussian() * 0.3D;
                double offsetZ = player.getRandom().nextGaussian() * 0.3D;
                world.sendParticles(ParticleTypes.SMOKE, 
                    player.getX() + offsetX, 
                    player.getY() + player.getEyeHeight() + offsetY, 
                    player.getZ() + offsetZ, 
                    1, 0, 0, 0, 0.05D);
                
                // 同时生成一些闪电粒子效果
                world.sendParticles(ParticleTypes.ELECTRIC_SPARK, 
                    player.getX() + offsetX, 
                    player.getY() + player.getEyeHeight() + offsetY, 
                    player.getZ() + offsetZ, 
                    1, 0, 0, 0, 0.05D);
            }
        }
    }
    
    // 检查目标位置是否有效（简单检查是否有足够空间）
    private static boolean isValidTeleportPosition(ServerLevel world, double x, double y, double z) {
        BlockPos pos = new BlockPos((int)x, (int)y, (int)z);
        BlockPos posAbove = new BlockPos((int)x, (int)(y + 1), (int)z);
        // 检查位置是否安全（不是固体方块）
        return !world.getBlockState(pos).isSolid() && !world.getBlockState(posAbove).isSolid();
    }
}