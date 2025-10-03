package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.GhostEyeDimensionHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 眼魔状态管理器 - 统一管理眼魔状态和相关的buff效果
 * 实现眼魔状态和buff的合并管理，确保状态和效果的一致性
 */
public class GhostEyeManager {
    
    /**
     * 将玩家设置为眼魔状态并添加所有相关buff效果
     * @param player 要设置的玩家
     * @param isFirstTime 是否是第一次设置（用于触发成就）
     */
    public static void setGhostEyeState(ServerPlayer player, boolean isFirstTime) {
        // 更新持久化变量
        LazyOptional<KRBVariables.PlayerVariables> variablesOptional = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null);
        variablesOptional.ifPresent(variables -> {
            variables.isGhostEye = true;
            variables.syncPlayerVariables(player);
        });
        
        // 如果是第一次设置，触发成就
        if (isFirstTime) {
            // 触发成就：成为眼魔
            com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.GHOST_EYE_TRIGGER.trigger(player);
        }
        
        // 添加眼魔特性效果
        addGhostEyeEffects(player);
        
        // 给予玩家消息提示
        player.displayClientMessage(Component.literal("你感受到了眼魔之力在体内流动..."), true);
    }
    
    /**
     * 移除玩家的眼魔状态并清除所有相关buff效果
     * @param player 要移除的玩家
     */
    public static void removeGhostEyeState(ServerPlayer player) {
        // 更新持久化变量
        LazyOptional<KRBVariables.PlayerVariables> variablesOptional = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null);
        variablesOptional.ifPresent(variables -> {
            variables.isGhostEye = false;
            variables.syncPlayerVariables(player);
        });
        
        // 直接从运行时集合中移除，不再调用可能导致循环的方法
        if (GhostEyeDimensionHandler.ghostEyePlayers.contains(player.getUUID().toString())) {
            GhostEyeDimensionHandler.ghostEyePlayers.remove(player.getUUID().toString());
        }
        
        // 移除眼魔特性效果
        removeGhostEyeEffects(player);
    }
    
    /**
     * 检查玩家是否处于眼魔状态
     * @param player 要检查的玩家
     * @return 是否处于眼魔状态
     */
    public static boolean isGhostEye(ServerPlayer player) {
        // 同时检查持久化变量和运行时集合，确保状态一致性
        boolean hasPersistentFlag = false;
        LazyOptional<KRBVariables.PlayerVariables> variablesOptional = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null);
        if (variablesOptional.isPresent()) {
            hasPersistentFlag = variablesOptional.orElseThrow(() -> new IllegalStateException("PlayerVariables not found")).isGhostEye;
        }
        
        boolean inRuntimeCollection = GhostEyeDimensionHandler.isGhostEyePlayer(player);
        
        // 如果两个状态不一致，优先使用持久化变量的状态
        if (hasPersistentFlag != inRuntimeCollection) {
            if (hasPersistentFlag) {
                GhostEyeDimensionHandler.transformToGhostEye(player);
            } else {
                GhostEyeDimensionHandler.restoreOriginalRace(player);
            }
        }
        
        return hasPersistentFlag;
    }
    
    /**
     * 单独添加眼魔特性效果（不改变状态）
     * @param player 要添加效果的玩家
     */
    public static void addGhostEyeEffects(ServerPlayer player) {
        // 添加所有四个眼魔特性效果
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 1, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, Integer.MAX_VALUE, 1, false, false));
    }
    
    /**
     * 单独移除眼魔特性效果（不改变状态）
     * @param player 要移除效果的玩家
     */
    public static void removeGhostEyeEffects(ServerPlayer player) {
        // 移除所有四个眼魔特性效果
        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.SLOW_FALLING);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        player.removeEffect(MobEffects.JUMP);
    }
    
    /**
     * 同步眼魔状态和buff效果
     * 确保状态和效果的一致性
     * @param player 要同步的玩家
     */
    public static void syncGhostEyeState(ServerPlayer player) {
        boolean isGhostEye = false;
        LazyOptional<KRBVariables.PlayerVariables> variablesOptional = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null);
        if (variablesOptional.isPresent()) {
            isGhostEye = variablesOptional.orElseThrow(() -> new IllegalStateException("PlayerVariables not found")).isGhostEye;
        }
        
        boolean hasAllEffects = player.hasEffect(MobEffects.NIGHT_VISION) && 
                               player.hasEffect(MobEffects.SLOW_FALLING) && 
                               player.hasEffect(MobEffects.MOVEMENT_SPEED) && 
                               player.hasEffect(MobEffects.JUMP);
        
        // 如果状态和效果不一致，进行同步
        if (isGhostEye && !hasAllEffects) {
            addGhostEyeEffects(player);
        } else if (!isGhostEye && hasAllEffects) {
            removeGhostEyeEffects(player);
        }
    }
}