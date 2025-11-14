package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.PlayerAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class DarkGhostRiderHenshin {

    private DarkGhostRiderHenshin() {}

    /** 穿3件黑暗Ghost装甲：头盔、胸甲、护腿 */
    public static void trigger(Player player) {
        // 保存原盔甲（4个槽位）
        ItemStack[] original = saveArmor(player);

        // 穿上黑暗Ghost装甲
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.DARK_RIDER_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.DARK_RIDER_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.DARK_RIDER_LEGGINGS.get()));
        // 清空鞋子槽位
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);

        // 把旧盔甲塞回背包
        giveBack(player, original);
        
        // 播放玩家动画
        if (player instanceof ServerPlayer serverPlayer) {
            // 同时发送给玩家自己和所有跟踪该玩家的客户端
            PlayerAnimationPacket packet = new PlayerAnimationPacket(Component.literal("ghost"), player.getId(), true);
            // 发送给玩家自己
            PacketHandler.sendToClient(packet, serverPlayer);
            // 发送给所有跟踪该玩家的客户端
            PacketHandler.sendToAllTracking(packet, serverPlayer);
        }
    }

    private static ItemStack[] saveArmor(Player p) {
        return new ItemStack[] {
                p.getInventory().armor.get(3).copy(), // 头盔
                p.getInventory().armor.get(2).copy(), // 胸甲
                p.getInventory().armor.get(1).copy(), // 护腿
                p.getInventory().armor.get(0).copy()  // 鞋子
        };
    }

    private static void giveBack(Player p, ItemStack[] stacks) {
        // 返还原始盔甲到背包
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            if (!stack.isEmpty()) {
                if (!p.getInventory().add(stack)) {
                    p.spawnAtLocation(stack);
                }
            }
        }
    }
}