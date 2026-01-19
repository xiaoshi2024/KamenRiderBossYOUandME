package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3f;

import java.util.Random;
import java.util.function.Supplier;

/**
 * 粒子效果数据包，用于从服务器向客户端发送粒子效果指令
 */
public class ParticleEffectPacket {
    private final double x, y, z; // 粒子效果中心点坐标
    private final float r, g, b;  // 粒子颜色 RGB 值
    private static final Random RANDOM = new Random();
    
    public ParticleEffectPacket(double x, double y, double z, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
    }
    
    public static void encode(ParticleEffectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeFloat(packet.r);
        buffer.writeFloat(packet.g);
        buffer.writeFloat(packet.b);
    }
    
    public static ParticleEffectPacket decode(FriendlyByteBuf buffer) {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        float r = buffer.readFloat();
        float g = buffer.readFloat();
        float b = buffer.readFloat();
        return new ParticleEffectPacket(x, y, z, r, g, b);
    }
    
    public static void handle(ParticleEffectPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 只在客户端处理粒子效果
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                spawnParticlesOnClient(packet);
            });
        });
        ctx.get().setPacketHandled(true);
    }
    
    /**
     * 在客户端生成粒子效果
     */
    private static void spawnParticlesOnClient(ParticleEffectPacket packet) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level == null) return;
        
        DustParticleOptions particleOptions = new DustParticleOptions(new Vector3f(packet.r, packet.g, packet.b), 1.0F);
        
        // 生成粒子效果，从中心点向四周扩散
        for (int i = 0; i < 80; i++) {
            double motionX = (RANDOM.nextDouble() - 0.5) * 0.8;
            double motionY = (RANDOM.nextDouble() - 0.3) * 0.8;
            double motionZ = (RANDOM.nextDouble() - 0.5) * 0.8;
            
            double offsetX = packet.x + (RANDOM.nextDouble() - 0.5) * 1.5;
            double offsetY = packet.y + (RANDOM.nextDouble() - 0.5) * 2.0;
            double offsetZ = packet.z + (RANDOM.nextDouble() - 0.5) * 1.5;
            
            minecraft.level.addParticle(
                    particleOptions,
                    offsetX, offsetY, offsetZ,
                    motionX, motionY, motionZ
            );
        }
    }
}