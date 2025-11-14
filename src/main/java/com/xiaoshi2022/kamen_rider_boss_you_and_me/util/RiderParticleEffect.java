package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ParticleEffectPacket;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.Random;

/**
 * 骑士解除变身粒子效果工具类
 */
public class RiderParticleEffect {
    private static final Random RANDOM = new Random();
    
    /**
     * 斩月-真解除变身时的绿色粒子效果
     */
    public static void spawnZangetsuShinReleaseParticles(ServerPlayer player) {
        spawnColoredParticles(player, 0.0f, 1.0f, 0.0f); // 绿色
    }
    
    /**
     * Sigurd解除变身时的红色粒子效果
     */
    public static void spawnSigurdReleaseParticles(ServerPlayer player) {
        spawnColoredParticles(player, 1.0f, 0.0f, 0.0f); // 红色
    }
    
    /**
     * 玛丽卡解除变身时的粉色粒子效果
     */
    public static void spawnMarikaReleaseParticles(ServerPlayer player) {
        spawnColoredParticles(player, 1.0f, 0.4f, 0.7f); // 粉色
    }
    
    /**
     * 暴君解除变身时的深红色粒子效果
     */
    public static void spawnTyrantReleaseParticles(ServerPlayer player) {
        spawnColoredParticles(player, 0.8f, 0.1f, 0.1f); // 深红色
    }
    
    /**
     * Duke解除变身时的黄色粒子效果
     */
    public static void spawnDukeReleaseParticles(ServerPlayer player) {
        spawnColoredParticles(player, 1.0f, 1.0f, 0.0f); // 黄色
    }
    
    /**
     * 根据骑士类型生成对应的解除变身粒子效果
     */
    public static void spawnReleaseParticlesByRiderType(ServerPlayer player, String riderType) {
        switch (riderType) {
            case "ZANGETSU_SHIN":
                spawnZangetsuShinReleaseParticles(player);
                break;
            case "SIGURD":
                spawnSigurdReleaseParticles(player);
                break;
            case "MARIKA":
                spawnMarikaReleaseParticles(player);
                break;
            case "TYRANT":
                spawnTyrantReleaseParticles(player);
                break;
            case "DUKE":
                spawnDukeReleaseParticles(player);
                break;
        }
    }
    
    /**
     * 生成彩色粒子效果
     * 粒子会从玩家身上向四周散落
     */
    private static void spawnColoredParticles(ServerPlayer player, float r, float g, float b) {
        ServerLevel level = player.serverLevel();
        Vec3 position = player.getEyePosition();
        
        // 创建一个粒子数据包，发送给所有在玩家周围的客户端
        ParticleEffectPacket packet = new ParticleEffectPacket(position.x, position.y, position.z, r, g, b);
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
        
        // 在服务器端也生成粒子，确保所有玩家都能看到
        for (int i = 0; i < 100; i++) {
            // 生成随机方向和速度的粒子
            double motionX = (RANDOM.nextDouble() - 0.5) * 0.8;
            double motionY = (RANDOM.nextDouble() - 0.3) * 0.8;
            double motionZ = (RANDOM.nextDouble() - 0.5) * 0.8;
            
            // 生成随机位置偏移，确保粒子从玩家周围散发
            double offsetX = position.x + (RANDOM.nextDouble() - 0.5) * 1.5;
            double offsetY = position.y + (RANDOM.nextDouble() - 0.5) * 2.0;
            double offsetZ = position.z + (RANDOM.nextDouble() - 0.5) * 1.5;
            
            DustParticleOptions particleOptions = new DustParticleOptions(new Vector3f(r, g, b), 1.0F);
            level.sendParticles(
                    particleOptions,
                    offsetX, offsetY, offsetZ,
                    1, // 每个位置生成1个粒子
                    motionX, motionY, motionZ,
                    0.1 // 速度
            );
        }
    }
}