package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class PreventArmorEquipHandler {

    @SubscribeEvent
    public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        
        // 检查物品是否是盔甲
        if (itemStack.getItem() instanceof ArmorItem) {
            // 检查玩家是否已经装备了变身盔甲
            if (PlayerDeathHandler.hasTransformationArmor(player)) {
                // 取消事件
                event.setCanceled(true);
                // 向玩家显示消息
                player.displayClientMessage(Component.literal("变身状态下无法装备其他盔甲！"), true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack itemStack = player.getMainHandItem();
        
        // 检查物品是否是盔甲
        if (itemStack.getItem() instanceof ArmorItem) {
            // 检查玩家是否已经装备了变身盔甲
            if (PlayerDeathHandler.hasTransformationArmor(player)) {
                // 取消事件
                event.setCanceled(true);
                // 向玩家显示消息
                player.displayClientMessage(Component.literal("变身状态下无法装备其他盔甲！"), true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack itemStack = player.getMainHandItem();
        
        // 检查物品是否是盔甲
        if (itemStack.getItem() instanceof ArmorItem) {
            // 检查玩家是否已经装备了变身盔甲
            if (PlayerDeathHandler.hasTransformationArmor(player)) {
                // 取消事件
                event.setCanceled(true);
                // 向玩家显示消息
                player.displayClientMessage(Component.literal("变身状态下无法装备其他盔甲！"), true);
            }
        }
    }
}