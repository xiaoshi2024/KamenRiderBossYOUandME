package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class CherryRiderHenshin {

    private CherryRiderHenshin() {}

    /** 穿 3 件樱桃装甲：头盔、胸甲、护腿 */
    public static void trigger(Player player) {
        // 保存原盔甲（4 个槽位）
        ItemStack[] original = saveArmor(player);

        // 穿上樱桃装甲
        player.setItemSlot(EquipmentSlot.HEAD,  new ItemStack(ModItems.SIGURD_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.SIGURD_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS,  new ItemStack(ModItems.SIGURD_LEGGINGS.get()));
        // 清空鞋子槽位
        player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);

        // 把旧盔甲塞回背包
        giveBack(player, original);
    }

    private static ItemStack[] saveArmor(Player p) {
        return new ItemStack[] {
                p.getInventory().armor.get(3).copy(), // 头盔
                p.getInventory().armor.get(2).copy(), // 胸甲
                p.getInventory().armor.get(1).copy(), // 护腿
                p.getInventory().armor.get(0).copy()  // 鞋子
        };
    }

    private static void giveBack(Player p, ItemStack[] old) {
        // 优先返还鞋子
        if (!old[3].isEmpty() && !p.getInventory().add(old[3])) {
            p.drop(old[3], false);
        }
        
        // 返还其他装备
        for (int i = 0; i < 3; i++) {
            if (!old[i].isEmpty() && !p.getInventory().add(old[i])) {
                p.drop(old[i], false);
            }
        }
    }
}