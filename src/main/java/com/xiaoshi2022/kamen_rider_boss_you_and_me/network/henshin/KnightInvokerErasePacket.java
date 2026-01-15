package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class KnightInvokerErasePacket {
    private final int playerId;

    public KnightInvokerErasePacket(int playerId) {
        this.playerId = playerId;
    }

    public static void encode(KnightInvokerErasePacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
    }

    public static KnightInvokerErasePacket decode(FriendlyByteBuf buffer) {
        return new KnightInvokerErasePacket(buffer.readInt());
    }

    /**
     * 判断方块是否可以被擦除
     */
    private static boolean isErasableBlock(Block block) {
        // 获取方块的注册表名称
        String blockName = block.getName().toString();
        
        // 可擦除的方块类型：拉杆、按钮、压力板、红石相关等
        return blockName.contains("lever") ||      // 拉杆
               blockName.contains("button") ||     // 按钮
               blockName.contains("pressure_plate") || // 压力板
               blockName.contains("redstone") ||   // 红石相关
               blockName.contains("tripwire") ||   // 绊线
               blockName.contains("hopper") ||     // 漏斗
               blockName.contains("dispenser") ||  // 发射器
               blockName.contains("dropper") ||    // 投掷器
               blockName.contains("piston") ||     // 活塞
               blockName.contains("lectern") ||    // 讲台
               blockName.contains("bell") ||       // 钟
               blockName.contains("command_block") || // 命令方块
               blockName.contains("tnt") ||        // TNT
               blockName.contains("trapped_chest"); // 陷阱箱
    }

    public static void handle(KnightInvokerErasePacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            Level level = player.level();
            
            // 消耗骑士能量（例如消耗30点能量）
            if (!com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler.consumeRiderEnergy(player, 30.0D)) {
                return; // 能量不足，取消技能执行
            }
            
            // 播放Breakam Cannon音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModBossSounds.ERASE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 创建黑灰色光波效果（使用AreaEffectCloud模拟）
            AreaEffectCloud cloud = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, level);
            cloud.setPos(player.getX(), player.getY() + 1.0D, player.getZ());
            cloud.setRadius(3.0F);
            cloud.setRadiusOnUse(-0.5F);
            cloud.setWaitTime(0);
            cloud.setDuration(60);
            cloud.setParticle(ParticleTypes.SMOKE);
            
            // 设置云的所有者为施法者，这样云就不会影响到施法者本人
            cloud.setOwner(player);
            
            // 给敌人添加虚弱效果（模拟擦除能力）
            cloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    MobEffects.WEAKNESS, 100, 2, false, false, true));
            
            level.addFreshEntity(cloud);
            
            // 对范围内的敌人造成伤害
            AABB aabb = new AABB(player.getX() - 3.0D, player.getY() - 1.0D, player.getZ() - 3.0D,
                    player.getX() + 3.0D, player.getY() + 3.0D, player.getZ() + 3.0D);
            
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);
            for (LivingEntity entity : entities) {
                if (entity != player && !entity.isAlliedTo(player)) {
                    // 造成伤害
                    entity.hurt(level.damageSources().magic(), 10.0F);
                    
                    // 添加擦除效果：移除正面效果
                    entity.removeAllEffects();
                    
                    // 生成额外的粒子效果
                    for (int i = 0; i < 10; i++) {
                        double dx = (level.random.nextDouble() - 0.5D) * 2.0D;
                        double dy = level.random.nextDouble();
                        double dz = (level.random.nextDouble() - 0.5D) * 2.0D;
                        level.addParticle(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 1.0D, entity.getZ(), dx, dy, dz);
                    }
                }
            }
            
            // 擦除范围内的方块（拉杆、按钮、压力板等）
            for (int x = (int) Math.floor(player.getX() - 3.0D); x <= Math.ceil(player.getX() + 3.0D); x++) {
                for (int y = (int) Math.floor(player.getY() - 1.0D); y <= Math.ceil(player.getY() + 3.0D); y++) {
                    for (int z = (int) Math.floor(player.getZ() - 3.0D); z <= Math.ceil(player.getZ() + 3.0D); z++) {
                        net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(x, y, z);
                        net.minecraft.world.level.block.state.BlockState blockState = level.getBlockState(blockPos);
                        net.minecraft.world.level.block.Block block = blockState.getBlock();
                        
                        // 检查是否是可擦除的方块类型
                        if (isErasableBlock(block)) {
                            // 破坏方块
                            level.destroyBlock(blockPos, true, player);
                            
                            // 生成擦除粒子效果
                            for (int i = 0; i < 5; i++) {
                                double dx = x + level.random.nextDouble();
                                double dy = y + level.random.nextDouble();
                                double dz = z + level.random.nextDouble();
                                level.addParticle(ParticleTypes.SMOKE, dx, dy, dz, 0, 0, 0);
                                level.addParticle(ParticleTypes.CLOUD, dx, dy, dz, 0, 0, 0);
                            }
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
