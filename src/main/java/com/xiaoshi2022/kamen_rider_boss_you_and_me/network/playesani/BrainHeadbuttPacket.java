package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BrainHeadbuttPacket {
    private final int playerId;

    public BrainHeadbuttPacket(int playerId) {
        this.playerId = playerId;
    }

    public static void encode(BrainHeadbuttPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.playerId);
    }

    public static BrainHeadbuttPacket decode(FriendlyByteBuf buffer) {
        return new BrainHeadbuttPacket(buffer.readInt());
    }

    public static void handle(BrainHeadbuttPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 服务器处理头部必杀技
            if (context.getDirection().getReceptionSide().isServer()) {
                handleServerHeadbutt(packet);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleServerHeadbutt(BrainHeadbuttPacket packet) {
        net.minecraft.server.level.ServerLevel world = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (world != null) {
            net.minecraft.world.entity.Entity entity = world.getEntity(packet.playerId);
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                // 检查是否装备了Brain头盔
                net.minecraft.world.item.ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
                if (helmet.is(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.BRAIN_HELMET.get())) {
                    // 检查冷却时间（使用骑士踢的冷却时间）
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables.PlayerVariables variables = player.getCapability(com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables.PlayerVariables());
                    long currentTime = System.currentTimeMillis();
                    long timeSinceLastKick = currentTime - variables.lastKickTime;
                    long cooldownTime = 40000; // 40秒冷却时间
                    
                    if (timeSinceLastKick >= cooldownTime) {
                        // 处理头部必杀技逻辑
                        executeBrainHeadbutt(player);
                        
                        // 更新上次踢击时间（复用骑士踢的冷却时间）
                        variables.lastKickTime = currentTime;
                        variables.syncPlayerVariables(player);
                    }
                }
            }
        }
    }

    private static void executeBrainHeadbutt(net.minecraft.server.level.ServerPlayer player) {
        // 给玩家一个向前的推力
        player.setDeltaMovement(player.getLookAngle().scale(2.0));
        player.hurtMarked = true;

        // 播放头部必杀技动画
        net.minecraft.network.chat.Component animation = net.minecraft.network.chat.Component.literal("brain_headbutt");
        com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendAnimationToAll(animation, player.getId(), true);

        // 处理伤害逻辑
        for (net.minecraft.world.entity.Entity entity : player.level().getEntities(player, player.getBoundingBox().inflate(3.0))) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity && livingEntity != player) {
                float damage = com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KickDamageHelper.getKickDamage(player);
                livingEntity.hurt(livingEntity.damageSources().playerAttack(player), damage);
            }
        }

        // 生成粒子效果
        for (int i = 0; i < 20; i++) {
            double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 2.0;
            double y = player.getY() + player.getRandom().nextDouble() * player.getBbHeight();
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 2.0;
            player.level().addParticle(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    x, y, z,
                    0.0, 0.0, 0.0
            );
        }
    }
}