// ClientTransformationHandler.java
package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.SyncTransformationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientTransformationHandler {

    public static void handleTransformationSync(SyncTransformationPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        Entity entity = minecraft.level.getEntity(packet.getPlayerId());
        if (entity instanceof Player player) {
            // 更新玩家变身状态
            player.getPersistentData().putString("currentForm", packet.getForm());
            player.getPersistentData().putBoolean("isTransforming", packet.isTransforming());

            // 触发客户端特效、音效等
            if (packet.isTransforming()) {
                playTransformationEffects(player, packet.getForm());
            } else {
                playReleaseEffects(player);
            }
        }
    }

    private static void playTransformationEffects(Player player, String form) {
        // 根据形态播放不同的客户端特效
        switch (form) {
            case "DARK_ORANGE":
                // 播放黑暗铠武变身特效
                playParticleEffect(player, "dark_orange_transform");
                playSoundEffect(player, "dark_orange_henshin");
                break;
            // 可以添加其他形态
        }
    }

    private static void playReleaseEffects(Player player) {
        // 播放解除变身特效
        playParticleEffect(player, "transform_release");
        playSoundEffect(player, "lock_off");
    }

    private static void playParticleEffect(Player player, String effectName) {
        Minecraft minecraft = Minecraft.getInstance();
        ParticleEngine particleEngine = minecraft.particleEngine;
        RandomSource random = minecraft.level.random;
        Vec3 playerPos = player.position();

        switch (effectName) {
            case "dark_orange_transform":
                // 播放黑暗铠武变身粒子效果
                for (int i = 0; i < 50; i++) {
                    double x = playerPos.x() + (random.nextDouble() - 0.5) * 2.0;
                    double y = playerPos.y() + random.nextDouble() * 2.0;
                    double z = playerPos.z() + (random.nextDouble() - 0.5) * 2.0;
                    double motionX = (random.nextDouble() - 0.5) * 0.5;
                    double motionY = random.nextDouble() * 0.5 + 0.2;
                    double motionZ = (random.nextDouble() - 0.5) * 0.5;
                    particleEngine.createParticle(ParticleTypes.FLAME, x, y, z, motionX, motionY, motionZ);
                }
                break;
            case "transform_release":
                // 播放解除变身粒子效果
                for (int i = 0; i < 30; i++) {
                    double x = playerPos.x() + (random.nextDouble() - 0.5) * 2.0;
                    double y = playerPos.y() + random.nextDouble() * 2.0;
                    double z = playerPos.z() + (random.nextDouble() - 0.5) * 2.0;
                    double motionX = (random.nextDouble() - 0.5) * 0.5;
                    double motionY = -random.nextDouble() * 0.5;
                    double motionZ = (random.nextDouble() - 0.5) * 0.5;
                    particleEngine.createParticle(ParticleTypes.SMOKE, x, y, z, motionX, motionY, motionZ);
                }
                break;
        }
    }

    private static void playSoundEffect(Player player, String soundName) {
        Minecraft minecraft = Minecraft.getInstance();
        SoundEvent soundEvent = null;

        switch (soundName) {
            case "dark_orange_henshin":
                soundEvent = ModBossSounds.JIMBAR_LEMON.get();
                break;
            case "lock_off":
                soundEvent = ModBossSounds.LOCKOFF.get();
                break;
        }

        if (soundEvent != null) {
            minecraft.level.playSound(
                player,
                player.blockPosition(),
                soundEvent,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
            );
        }
    }
}