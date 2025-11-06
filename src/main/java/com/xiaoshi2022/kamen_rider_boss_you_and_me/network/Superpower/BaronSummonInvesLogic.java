package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.armorevnt.PlayerDeathHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.helheim_crack;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class BaronSummonInvesLogic {
    
    private static final Random RANDOM = new Random();
    private static final int CRACKS_COUNT = 2; // 召唤的裂缝数量
    private static final long CRACK_TIMEOUT = 100; // 裂缝超时时间（刻）
    
    // 存储裂缝参数的列表
    private static final List<CrackParam> crackParams = new ArrayList<>();
    
    public static void summonInvesAroundPlayer(ServerPlayer player) {
        Level level = player.level();
        
        // 检查玩家是否为Overlord状态
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        if (!variables.isOverlord) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("你还没有获得召唤异域者的权力！"));
            return;
        }
        long currentTime = level.getGameTime();
        
        // 检查玩家是否处于变身状态
        boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);
        
        // 如果是巴隆柠檬形态，使用不同的冷却时间
        boolean isBaronLemon = isBaronLemonForm(player);
        
        // 根据玩家状态设置不同的冷却时间
        long cooldownTime;
        if (isTransformed) {
            // 变身状态下的冷却时间
            cooldownTime = isBaronLemon ? 100 : 150; // 柠檬形态5秒冷却，普通形态7.5秒冷却
        } else {
            // 非变身状态下的冷却时间（更长）
            cooldownTime = 600; // 30秒冷却时间
        }
        
        // 检查技能冷却时间
        if (variables.baron_banana_energy_cooldown > currentTime) {
            long remainingSeconds = (variables.baron_banana_energy_cooldown - currentTime) / 20;
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("技能冷却中，还剩 " + remainingSeconds + " 秒"));
            return;
        }
        
        // 设置冷却时间
        variables.baron_banana_energy_cooldown = currentTime + cooldownTime;
        variables.syncPlayerVariables(player);
        
        // 1. 播放召唤画
        if (!level.isClientSide()) {
            PacketHandler.sendAnimationToAll(Component.literal("shows"), player.getId(), false);
        }
        
        // 创建赫尔海姆裂缝并召唤异域者
        openHelheimCracksAndSpawnInves(player);
        
        // 发送提示消息
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("已召唤异域者为你而战！"));
    }
    
    private static boolean isBaronLemonForm(ServerPlayer player) {
        // 检查玩家是否穿着巴隆柠檬形态的盔甲
        // 这里可以根据需要调整判断逻辑
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        return variables.lemon_ready; // 假设lemon_ready表示柠檬形态激活
    }
    
    private static void openHelheimCracksAndSpawnInves(ServerPlayer player) {
        Level level = player.level();
        
        // 创建裂缝
        for (int i = 0; i < CRACKS_COUNT; i++) {
            double crackX = player.getX() + (RANDOM.nextDouble() - 0.5) * 12.0;
            double crackZ = player.getZ() + (RANDOM.nextDouble() - 0.5) * 12.0;
            int crackY = findGroundLevel(level, crackX, crackZ);
            
            BlockPos randomPos = new BlockPos((int)crackX, crackY, (int)crackZ);
            
            // 在空地上放置裂缝方块
            if (level.isEmptyBlock(randomPos)) {
                BlockState state = ModBlocks.HELHEIM_CRACK_BLOCK.get().defaultBlockState();
                level.setBlockAndUpdate(randomPos, state);
                simulateRightClickEffect(level, randomPos);
            }
            
            // 记录裂缝参数
            crackParams.add(new CrackParam(crackX, crackY, crackZ, level.getGameTime()));
            
            // 播放粒子效果
            spawnCrackParticles(level, crackX, crackY, crackZ);
        }
        
        // 生成异域者
        spawnInvesWithMathLink(player);
    }
    
    private static void simulateRightClickEffect(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof helheim_crack) {
            level.setBlockAndUpdate(pos, state.setValue(helheim_crack.ANIMATION, 1));
            level.playSound(null, pos, com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds.OPENDLOCK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
    
    private static int findGroundLevel(Level level, double x, double z) {
        int startY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int)x, (int)z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos((int)x, startY, (int)z);
        
        // 向下寻找可放置的位置
        while (pos.getY() > level.getMinBuildHeight() && !level.getBlockState(pos).isSolid()) {
            pos.move(Direction.DOWN);
        }
        
        // 向上移动一格，确保在地面之上
        if (pos.getY() < level.getMaxBuildHeight()) {
            pos.move(Direction.UP);
        }
        
        // 确保目标位置可放置
        while (pos.getY() < level.getMaxBuildHeight() && !level.isEmptyBlock(pos)) {
            pos.move(Direction.UP);
        }
        
        return pos.getY();
    }
    
    private static void spawnCrackParticles(Level level, double x, double y, double z) {
        // 烟雾粒子
        for (int j = 0; j < 20; j++) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    x + (RANDOM.nextDouble() - 0.5) * 2.0,
                    y + 1.0 + RANDOM.nextDouble() * 2.0,
                    z + (RANDOM.nextDouble() - 0.5) * 2.0,
                    0, 0.1, 0);
        }
        
        // 火焰粒子
        for (int j = 0; j < 15; j++) {
            level.addParticle(ParticleTypes.FLAME,
                    x + (RANDOM.nextDouble() - 0.5) * 2.0,
                    y + 1.0 + RANDOM.nextDouble() * 2.0,
                    z + (RANDOM.nextDouble() - 0.5) * 2.0,
                    0, 0.1, 0);
        }
    }
    
    private static void spawnInvesWithMathLink(ServerPlayer player) {
        Level level = player.level();
        long currentTime = level.getGameTime();
        
        // 获取有效裂缝
        List<CrackParam> validCracks = crackParams.stream()
                .filter(c -> currentTime - c.spawnTime < CRACK_TIMEOUT)
                .collect(Collectors.toList());
        
        // 为每个裂缝生成异域者
        validCracks.forEach(crack -> {
            // 极坐标偏移
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double offset = 0.5 + RANDOM.nextDouble() * 1.5;
            
            // 计算异域者位置
            double invesX = crack.x + Math.cos(angle) * offset;
            double invesZ = crack.z + Math.sin(angle) * offset;
            double invesY = crack.y + 0.2;
            
            // 生成异域者
            ElementaryInvesHelheim inves = new ElementaryInvesHelheim(
                    ModEntityTypes.INVES_HEILEHIM.get(),
                    level
            );
            inves.moveTo(invesX, invesY, invesZ, 0, 0);
            inves.setMaster(player); // 设置玩家为主人
            inves.setTarget(null); // 初始不设置目标
            
            // 添加强化效果
            inves.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            inves.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            
            level.addFreshEntity(inves);
            
            // 生成连接粒子
            spawnLinkParticles(level, crack.x, crack.y, crack.z, invesX, invesY, invesZ);
        });
        
        // 清理过期裂缝
        crackParams.removeIf(c -> currentTime - c.spawnTime >= CRACK_TIMEOUT);
    }
    
    private static void spawnLinkParticles(Level level, double x1, double y1, double z1, double x2, double y2, double z2) {
        for (int i = 0; i < 8; i++) {
            float progress = i / 8f;
            double px = x1 + (x2 - x1) * progress;
            double py = y1 + (y2 - y1) * progress + 0.5 * Math.sin(progress * Math.PI);
            double pz = z1 + (z2 - z1) * progress;
            
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    px, py, pz,
                    0, 0.02, 0);
        }
    }
    
    // 存储裂缝参数的内部类
    private static class CrackParam {
        final double x, y, z;
        final long spawnTime;
        
        public CrackParam(double x, double y, double z, long spawnTime) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.spawnTime = spawnTime;
        }
    }
    
    /**
     * 召回所有以玩家为主人的异域者
     * 当玩家按下shift+技能3键时调用
     */
    public static void recallSummonedInves(ServerPlayer player) {
        Level level = player.level();
        
        // 检查玩家是否为Overlord状态
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        if (!variables.isOverlord) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("你还没有获得召回异域者的权力！"));
            return;
        }
        
        UUID playerUUID = player.getUUID();
        int recalledCount = 0;
        
        // 查找并召回所有以该玩家为主人的异域者
        for (ElementaryInvesHelheim inves : level.getEntitiesOfClass(ElementaryInvesHelheim.class, player.getBoundingBox().inflate(100.0))) {
            if (inves.getMaster() != null && inves.getMaster().getUUID().equals(playerUUID)) {
                // 播放消失粒子效果
                spawnRecallParticles(level, inves.getX(), inves.getY(), inves.getZ());
                
                // 播放召回音效
                level.playSound(null, inves.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.5F);
                
                // 移除异域者实体
                inves.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                recalledCount++;
            }
        }
        
        // 发送提示消息
        if (recalledCount > 0) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("已召回 " + recalledCount + " 个异域者！"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("没有可召回的异域者！"));
        }
    }
    
    /**
     * 生成召回粒子效果
     */
    private static void spawnRecallParticles(Level level, double x, double y, double z) {
        for (int i = 0; i < 20; i++) {
            level.addParticle(ParticleTypes.PORTAL, 
                x + (RANDOM.nextDouble() - 0.5) * 1.0, 
                y + 0.5 + RANDOM.nextDouble() * 1.0, 
                z + (RANDOM.nextDouble() - 0.5) * 1.0, 
                0, 0, 0);
            
            level.addParticle(ParticleTypes.SMOKE, 
                x + (RANDOM.nextDouble() - 0.5) * 1.0, 
                y + 0.5 + RANDOM.nextDouble() * 1.0, 
                z + (RANDOM.nextDouble() - 0.5) * 1.0, 
                0, 0.1, 0);
        }
    }
}