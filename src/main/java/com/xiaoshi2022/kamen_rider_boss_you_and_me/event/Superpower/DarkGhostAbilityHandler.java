package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DarkRiderGhost.DarkRiderGhostItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.RiderEnergyHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class DarkGhostAbilityHandler {
    
    // 生命恢复冷却计时器
    private static final Map<UUID, Integer> regenerationCooldown = new HashMap<>();
    // 岩浆能量恢复常量
    private static final int LAVA_ENERGY_RECOVERY_RANGE = 3; // 检测岩浆的范围
    private static final double LAVA_ENERGY_RECOVERY_AMOUNT = 5.0D; // 每秒恢复的能量值
    private static final int LAVA_ENERGY_RECOVERY_INTERVAL = 10; // 恢复间隔(tick)，约0.5秒

    /* ---------------- 数值常量 ---------------- */
    private static final int INVISIBILITY_DURATION = 100; // 隐身效果持续时间(tick)
    private static final int NIGHT_VISION_DURATION = 200; // 夜视效果持续时间(tick)
    private static final int SPEED_DURATION = 60; // 速度提升持续时间(tick)
    private static final int STRENGTH_DURATION = 60; // 力量提升持续时间(tick)
    private static final int REGENERATION_DURATION = 10; // 生命恢复持续时间(tick) - 大幅降低持续时间
    private static final int JUMP_BOOST_DURATION = 60; // 跳跃提升持续时间(tick)
    private static final int REGENERATION_COOLDOWN = 60; // 生命恢复冷却时间(tick) - 增加冷却时间
    private static final int JUMP_BOOST_AMPLIFIER = 2; // 跳跃提升等级(降低为2级，约4格跳跃高度)
    private static final UUID HEALTH_BOOST_UUID = UUID.fromString("f8e3a7b2-1c5d-4e6f-8a9b-0d1e2f3a4b5c");
    private static final String HEALTH_BOOST_NAME = "DarkGhostHealthBoost";

    /* ---------------- 常驻被动 ---------------- */

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        if (isWearingDarkGhostArmor(player)) {
            // 1. 隐身能力 - 符合Ghost系列骑士的幽灵主题
            addEffectIfBetterOrAbsent(player, MobEffects.INVISIBILITY, INVISIBILITY_DURATION, 0);
            
            // 注：夜视能力已由GhostDriver腰带提供，此处不再重复添加
            
            // 3. 速度提升 - 模拟幽灵的敏捷性
            addEffectIfBetterOrAbsent(player, MobEffects.MOVEMENT_SPEED, SPEED_DURATION, 0);
            
            // 4. 力量提升 - 由于使用初期型Ghost Driver内置输出增幅装置
            addEffectIfBetterOrAbsent(player, MobEffects.DAMAGE_BOOST, STRENGTH_DURATION, 0);
            
            // 5. 生命恢复 - 增强生存能力，但大幅降低恢复速度和频率
            UUID playerId = player.getUUID();
            int cooldown = regenerationCooldown.getOrDefault(playerId, 0);
            
            if (cooldown <= 0) {
                // 只有在冷却结束时才应用生命恢复效果
                // -1级的再生效果已经非常弱，持续时间也很短
                addEffectIfBetterOrAbsent(player, MobEffects.REGENERATION, REGENERATION_DURATION, -1);
                // 设置冷却时间
                regenerationCooldown.put(playerId, REGENERATION_COOLDOWN);
            } else {
                // 减少冷却时间
                regenerationCooldown.put(playerId, cooldown - 1);
            }
            
            // 6. 跳跃提升 - 代替飞行的高机动性能力
            addEffectIfBetterOrAbsent(player, MobEffects.JUMP, JUMP_BOOST_DURATION, JUMP_BOOST_AMPLIFIER);
            
            // 7. 缓降效果 - 防止高处坠落伤害
            addEffectIfBetterOrAbsent(player, MobEffects.SLOW_FALLING, NIGHT_VISION_DURATION, 0);
            
            // 8. 生命值提升 - 黑暗灵骑的强大生命力
            applyHealthBoost(player);
            
            // 标记为Dark Ghost状态
            markDarkGhostState(player);
            
            // 岩浆附近快速恢复骑士能量
            if (player instanceof ServerPlayer serverPlayer) {
                // 每10tick(约0.5秒)检测一次岩浆并恢复能量
                if (player.tickCount % LAVA_ENERGY_RECOVERY_INTERVAL == 0) {
                    if (isNearLava(player)) {
                        RiderEnergyHandler.addRiderEnergy(serverPlayer, LAVA_ENERGY_RECOVERY_AMOUNT);
                    }
                }
            }
        } else {
            // 当玩家不再穿着盔甲时，重置飞行状态
            resetFlightState(player);
            
            // 移除生命值提升
            removeHealthBoost(player);
            
            // 移除Dark Ghost状态标记
            removeDarkGhostState(player);
        }
    }

    /* ---------------- 工具方法 ---------------- */
    
    /**
     * 检查玩家是否穿着黑暗灵骑盔甲
     */
    public static boolean isWearingDarkGhostArmor(Player player) {
        // 需要穿戴完整的黑暗灵骑盔甲（头盔、胸甲、护腿）
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof DarkRiderGhostItem &&
                player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof DarkRiderGhostItem &&
                player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof DarkRiderGhostItem;
    }
    
    /**
     * 标记玩家为Dark Ghost状态
     */
    private static void markDarkGhostState(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            if (!variables.isDarkGhostActive) {
                variables.isDarkGhostActive = true;
                variables.syncPlayerVariables(serverPlayer);
                
                // 播放变身音效（如果需要）
                serverPlayer.playNotifySound(
                        SoundEvents.WITHER_SPAWN,
                        SoundSource.PLAYERS, 0.5F, 1.2F);
            }
        }
    }
    
    /**
     * 移除玩家的Dark Ghost状态标记
     */
    private static void removeDarkGhostState(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            if (variables.isDarkGhostActive) {
                variables.isDarkGhostActive = false;
                variables.syncPlayerVariables(serverPlayer);
            }
        }
    }
    
    /**
     * 辅助方法：只在玩家没有特定效果或现有效果等级低于我们提供的等级时才添加效果
     */
    private static void addEffectIfBetterOrAbsent(Player player, MobEffect effect, int duration, int amplifier) {
        // 对于某些效果，降低持续时间，增加刷新频率，使玩家更容易受到伤害
        int adjustedDuration = duration;
        if (effect.equals(MobEffects.REGENERATION) || effect.equals(MobEffects.MOVEMENT_SPEED) || effect.equals(MobEffects.DAMAGE_BOOST)) {
            adjustedDuration = duration / 2; // 将这些效果的持续时间减半
        }
        
        if (!player.hasEffect(effect) || player.getEffect(effect).getAmplifier() < amplifier) {
            player.addEffect(new MobEffectInstance(effect, adjustedDuration, amplifier, false, false));
        }
    }
    
    /**
     * 重置玩家的飞行状态
     */
    private static void resetFlightState(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            
            // 如果当前的飞行控制器是DarkGhost，则清除所有Dark Ghost效果
            if (variables.currentFlightController != null && variables.currentFlightController.equals("DarkGhost")) {
                // 清除Dark Ghost状态的所有效果
                removeDarkGhostEffects(player);
                
                // 重置飞行控制器
                variables.currentFlightController = null;
                variables.syncPlayerVariables(serverPlayer);
                
                // 禁用飞行能力，除非玩家是创造模式或旁观者模式
                if (!serverPlayer.isCreative() && !serverPlayer.isSpectator()) {
                    serverPlayer.getAbilities().mayfly = false;
                    serverPlayer.getAbilities().flying = false;
                }
                
                // 确保玩家状态同步到客户端
                serverPlayer.onUpdateAbilities();
            }
        }
    }
    
    /**
     * 移除Dark Ghost的所有效果
     */
    private static void removeDarkGhostEffects(Player player) {
        // 清除所有Dark Ghost提供的效果
        // 注意：不清除夜视效果，因为它由GhostDriver腰带提供
        player.removeEffect(MobEffects.INVISIBILITY);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        player.removeEffect(MobEffects.DAMAGE_BOOST);
        player.removeEffect(MobEffects.REGENERATION);
        player.removeEffect(MobEffects.JUMP);
        player.removeEffect(MobEffects.SLOW_FALLING);
    }
    
    /**
     * 检查玩家是否在岩浆附近
     */
    private static boolean isNearLava(Player player) {
        BlockPos playerPos = player.blockPosition();
        
        // 检查玩家周围的方块是否有岩浆
        for (int x = -LAVA_ENERGY_RECOVERY_RANGE; x <= LAVA_ENERGY_RECOVERY_RANGE; x++) {
            for (int y = -LAVA_ENERGY_RECOVERY_RANGE; y <= LAVA_ENERGY_RECOVERY_RANGE; y++) {
                for (int z = -LAVA_ENERGY_RECOVERY_RANGE; z <= LAVA_ENERGY_RECOVERY_RANGE; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    // 检查方块是否为岩浆（使用getBlock() == Blocks.LAVA即可匹配所有岩浆类型）
                    if (player.level().getBlockState(checkPos).getBlock() == Blocks.LAVA) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 应用黑暗灵骑生命值提升
     */
    private static void applyHealthBoost(Player player) {
        // 保存当前生命值百分比，确保应用生命值提升时不会自动恢复到满状态
        float healthPercentage = player.getHealth() / player.getMaxHealth();
        
        // 从KRBVariables获取玩家变量
        KRBVariables.PlayerVariables playerVars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        
        AttributeInstance maxHealth = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            // 计算额外生命值提升 - 降低生命值提升幅度，最多只提升到原来的1.5倍
            double healthBoost = Math.min(playerVars.darkGhostMaxHealth - 20.0D, 35.0D); // 最多额外35点生命值，进一步降低强度
            
            // 移除旧的修改器（如果存在）
            maxHealth.removeModifier(HEALTH_BOOST_UUID);
            
            // 添加新的生命值提升修改器
            maxHealth.addTransientModifier(new AttributeModifier(
                    HEALTH_BOOST_UUID,
                    HEALTH_BOOST_NAME,
                    healthBoost,
                    AttributeModifier.Operation.ADDITION
            ));
            
            // 确保当前生命值按照百分比保持，不自动恢复
            // 重要：这是防止受伤后立即回满生命值的关键
            float newHealth = (float) (player.getMaxHealth() * healthPercentage);
            player.setHealth(newHealth);
        }
    }
    
    /**
     * 移除黑暗灵骑生命值提升
     */
    private static void removeHealthBoost(Player player) {
        AttributeInstance maxHealth = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            // 保存当前生命值百分比
            float healthPercentage = player.getHealth() / player.getMaxHealth();
            
            // 移除生命值提升修改器
            maxHealth.removeModifier(HEALTH_BOOST_UUID);
            
            // 调整当前生命值以保持百分比
            float newHealth = (float) (player.getMaxHealth() * healthPercentage);
            // 不再强制保留1点生命值，允许生命值自然降至0（死亡状态）
            player.setHealth(newHealth);
        }
    }
}