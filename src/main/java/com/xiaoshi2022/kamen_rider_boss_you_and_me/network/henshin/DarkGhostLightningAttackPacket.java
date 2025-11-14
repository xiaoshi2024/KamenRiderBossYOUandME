package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class DarkGhostLightningAttackPacket {

    public DarkGhostLightningAttackPacket() {
        // 空构造函数，用于数据包解析
    }

    // 从缓冲区读取数据
    public static DarkGhostLightningAttackPacket decode(FriendlyByteBuf buf) {
        return new DarkGhostLightningAttackPacket();
    }

    // 将数据写入缓冲区
    public static void encode(DarkGhostLightningAttackPacket packet, FriendlyByteBuf buf) {
        // 不需要额外数据
    }

    // 处理数据包
    public static void handle(DarkGhostLightningAttackPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level() instanceof ServerLevel) {
                // 处理闪电格斗攻击
                executeLightningAttack(player);
            }
        });
        context.setPacketHandled(true);
    }

    // 执行闪电格斗攻击
    private static void executeLightningAttack(ServerPlayer player) {
        // 消耗骑士能量（20点），如果能量不足则不执行攻击
        if (!RiderEnergyHandler.consumeRiderEnergy(player, 20.0D)) {
            return;
        }
        
        ServerLevel world = (ServerLevel) player.level();
        double range = 3.0D;
        
        // 创建闪电粒子效果
        for (int i = 0; i < 10; i++) {
            double offsetX = player.getRandom().nextGaussian() * 0.5D;
            double offsetY = player.getRandom().nextGaussian() * 0.5D + 1.0D;
            double offsetZ = player.getRandom().nextGaussian() * 0.5D;
            world.sendParticles(ParticleTypes.ELECTRIC_SPARK, 
                player.getX() + offsetX, 
                player.getY() + offsetY, 
                player.getZ() + offsetZ, 
                1, 0, 0, 0, 0.1D);
        }
        
        // 获取攻击范围内的实体
        List<Entity> nearbyEntities = world.getEntities(player, 
            player.getBoundingBox().inflate(range), 
            entity -> entity instanceof LivingEntity && entity != player);
        
        // 对范围内的敌人造成伤害并添加麻痹效果
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                // 对敌人造成伤害
                livingEntity.hurt(player.damageSources().playerAttack(player), 5.0F);
                
                // 添加麻痹效果（缓慢IV，持续3秒）
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
                
                // 在被击中的敌人周围生成闪电粒子
                for (int i = 0; i < 5; i++) {
                    double offsetX = player.getRandom().nextGaussian() * 0.3D;
                    double offsetY = player.getRandom().nextGaussian() * 0.3D;
                    double offsetZ = player.getRandom().nextGaussian() * 0.3D;
                    world.sendParticles(ParticleTypes.ELECTRIC_SPARK, 
                        entity.getX() + offsetX, 
                        entity.getY() + entity.getEyeHeight() + offsetY, 
                        entity.getZ() + offsetZ, 
                        1, 0, 0, 0, 0.05D);
                }
            }
        }
    }
}