package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

/**
 * 腰带移除检测处理器
 * 当玩家从Curio槽位取下腰带时，记录移除时间，并在3秒后自动解除变身
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class BeltRemovalHandler {
    // 3秒的tick数 (20 tick = 1秒)
    private static final int BELT_REMOVAL_TIMEOUT = 60;
    
    /**
     * 每tick检查玩家的腰带状态
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Player player = event.player;
        if (player.level().isClientSide) return; // 只在服务器端执行
        
        ServerPlayer serverPlayer = (ServerPlayer) player;
        KRBVariables.PlayerVariables variables = serverPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());
        
        // 检查玩家是否处于变身状态
        if (PlayerDeathHandler.hasTransformationArmor(serverPlayer)) {
            // 检查Curio槽位中是否有腰带
            boolean hasBelt = hasAnyRiderBelt(serverPlayer);
            
            // 如果没有腰带但之前记录了腰带移除时间，检查是否超过3秒
            if (!hasBelt && variables.beltRemovedTime > 0) {
                checkBeltRemovalTimeout(serverPlayer, variables);
            } else if (!hasBelt && variables.beltRemovedTime == 0) {
                // 如果没有腰带且之前没有记录移除时间，记录当前时间和腰带类型
                recordBeltRemoval(serverPlayer, variables);
            } else if (hasBelt && variables.beltRemovedTime > 0) {
                // 如果又重新装备了腰带，重置移除时间
                resetBeltRemoval(serverPlayer, variables);
            }
        } else {
            // 如果玩家不在变身状态，重置腰带移除时间
            if (variables.beltRemovedTime > 0) {
                resetBeltRemoval(serverPlayer, variables);
            }
        }
    }
    
    /**
     * 检查玩家的Curio槽位中是否有任何骑士腰带
     */
    private static boolean hasAnyRiderBelt(ServerPlayer player) {
        try {
            // 检查所有类型的骑士腰带
            boolean hasDrakKivaBelt = hasSpecificBelt(player, "DrakKivaBelt");
            boolean hasGenesisDriver = hasSpecificBelt(player, "Genesis_driver");
            boolean hasMegaUiorder = hasSpecificBelt(player, "Mega_uiorder");
            boolean hasSengokuDriver = hasSpecificBelt(player, "sengokudrivers_epmty");
            boolean hasTwoSidriver = hasSpecificBelt(player, "Two_sidriver");
            boolean hasGhostDriver = hasSpecificBelt(player, "GhostDriver");
            
            return hasDrakKivaBelt || hasGenesisDriver || hasMegaUiorder || hasSengokuDriver || hasTwoSidriver || hasGhostDriver;
        } catch (Exception e) {
            // 发生异常时返回true，避免错误的解除变身
            return true;
        }
    }
    
    /**
     * 检查玩家的Curio槽位中是否有特定类型的腰带
     */
    private static boolean hasSpecificBelt(ServerPlayer player, String beltClassName) {
        try {
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> {
                        String itemClassName = item.getItem().getClass().getSimpleName();
                        return itemClassName.equals(beltClassName);
                    }));
            return beltOptional.isPresent();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 记录腰带移除事件
     */
    private static void recordBeltRemoval(ServerPlayer player, KRBVariables.PlayerVariables variables) {
        try {
            // 记录当前游戏时间作为腰带移除时间
            variables.beltRemovedTime = player.level().getGameTime();
            
            // 确定腰带类型
            determineBeltType(player, variables);
            
            // 同步变量
            variables.syncPlayerVariables(player);
        } catch (Exception e) {
            // 发生异常时重置变量
            resetBeltRemoval(player, variables);
        }
    }
    
    /**
     * 确定玩家之前佩戴的腰带类型
     */
    private static void determineBeltType(ServerPlayer player, KRBVariables.PlayerVariables variables) {
        // 检查玩家是否佩戴了各种特定的变身盔甲，以确定腰带类型
        
        // Dark Ghost变身检测
        if (variables.isDarkGhostTransformed) {
            variables.removedBeltType = "DARK_GHOST";
            return;
        }
        
        // Dark Kiva变身检测
        if (variables.isDarkKivaBeltEquipped) {
            variables.removedBeltType = "DARK_KIVA";
            return;
        }
        
        // 柠檬变身检测
        if (variables.isGenesisDriverEquipped && variables.lemon_ready) {
            variables.removedBeltType = "GENESIS";
            return;
        }
        
        // 蜜瓜变身检测
        if (variables.isGenesisDriverEquipped && variables.melon_ready) {
            variables.removedBeltType = "GENESIS_MELON";
            return;
        }
        
        // 樱桃变身检测
        if (variables.isGenesisDriverEquipped && variables.cherry_ready) {
            variables.removedBeltType = "GENESIS_CHERRY";
            return;
        }
        
        // 桃子变身检测
        if (variables.isGenesisDriverEquipped && variables.peach_ready) {
            variables.removedBeltType = "GENESIS_PEACH";
            return;
        }
        
        // 火龙果变身检测
        if (variables.isGenesisDriverEquipped && variables.dragonfruit_ready) {
            variables.removedBeltType = "GENESIS_DRAGONFRUIT";
            return;
        }
        
        // 香蕉男爵变身检测
        if (variables.isSengokuDriverFilledEquipped && variables.banana_ready) {
            variables.removedBeltType = "BARONS";
            return;
        }
        
        // Dark Orange变身检测
        if (variables.isDarkOrangelsTransformed) {
            variables.removedBeltType = "DARK_ORANGE";
            return;
        }
        
        // Evil Bats变身检测
        if (variables.isEvilBatsTransformed) {
            variables.removedBeltType = "EVIL_BATS";
            return;
        }
        
        // Necrom变身检测
        if (variables.isMegaUiorderTransformed) {
            variables.removedBeltType = "RIDERNECROM";
            return;
        }
        
        // 如果无法确定类型，默认设为BARONS
        variables.removedBeltType = "BARONS";
    }
    
    /**
     * 检查腰带移除是否超时（3秒），如果超时则解除变身
     */
    private static void checkBeltRemovalTimeout(ServerPlayer player, KRBVariables.PlayerVariables variables) {
        long currentTime = player.level().getGameTime();
        
        // 检查是否超过3秒
        if (currentTime - variables.beltRemovedTime > BELT_REMOVAL_TIMEOUT) {
            // 直接清除玩家的盔甲来解除变身
            clearPlayerArmor(player);
            
            // 根据移除的腰带类型重置对应的变身状态变量
            resetTransformationState(variables);
            
            // 播放解除音效
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 重置腰带移除相关变量
            resetBeltRemoval(player, variables);
        }
    }
    
    /**
     * 根据移除的腰带类型重置对应的变身状态变量
     */
    private static void resetTransformationState(KRBVariables.PlayerVariables variables) {
        // 根据之前记录的腰带类型重置对应的变身状态
        switch (variables.removedBeltType) {
            case "DARK_GHOST":
                variables.isDarkGhostTransformed = false;
                break;
            case "DARK_KIVA":
                variables.isDarkKivaBeltEquipped = false;
                break;
            case "GENESIS":
            case "GENESIS_MELON":
            case "GENESIS_CHERRY":
            case "GENESIS_PEACH":
            case "GENESIS_DRAGONFRUIT":
                variables.isGenesisDriverEquipped = false;
                variables.lemon_ready = false;
                variables.melon_ready = false;
                variables.cherry_ready = false;
                variables.peach_ready = false;
                variables.dragonfruit_ready = false;
                break;
            case "BARONS":
                variables.isSengokuDriverFilledEquipped = false;
                variables.banana_ready = false;
                break;
            case "DARK_ORANGE":
                variables.isDarkOrangelsTransformed = false;
                break;
            case "EVIL_BATS":
                variables.isEvilBatsTransformed = false;
                break;
            case "RIDERNECROM":
                variables.isMegaUiorderTransformed = false;
                break;
        }
    }
    
    /**
     * 清除玩家的盔甲（仅用于腰带移除时的解除变身）
     */
    private static void clearPlayerArmor(ServerPlayer player) {
        for (int i = 0; i < 4; i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
        
        // 解除变身后立即移除隐身效果
        player.removeEffect(MobEffects.INVISIBILITY);
        
        // 通知客户端更新盔甲状态
        player.inventoryMenu.broadcastChanges();
    }
    
    /**
     * 重置腰带移除相关变量
     */
    private static void resetBeltRemoval(ServerPlayer player, KRBVariables.PlayerVariables variables) {
        variables.beltRemovedTime = 0L;
        variables.removedBeltType = "";
        variables.syncPlayerVariables(player); // 同步到客户端
    }
}