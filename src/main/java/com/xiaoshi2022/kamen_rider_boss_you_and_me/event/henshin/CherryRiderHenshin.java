package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class CherryRiderHenshin {

    private CherryRiderHenshin() {}

    /** 穿 3 件樱桃装甲：头盔、胸甲、护腿 */
    public static void trigger(Player player) {
        // 保存原盔甲（3 个槽位）
        ItemStack[] original = saveTop3(player);

        // 穿上樱桃装甲
        player.setItemSlot(EquipmentSlot.HEAD,  new ItemStack(ModItems.SIGURD_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.SIGURD_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS,  new ItemStack(ModItems.SIGURD_LEGGINGS.get()));

        // 把旧盔甲塞回背包
        giveBack(player, original);
    }

    private static ItemStack[] saveTop3(Player p) {
        return new ItemStack[] {
                p.getInventory().armor.get(3).copy(), // 头盔
                p.getInventory().armor.get(2).copy(), // 胸甲
                p.getInventory().armor.get(1).copy()  // 护腿
        };
    }

    private static void giveBack(Player p, ItemStack[] old) {
        for (ItemStack s : old) {
            if (!s.isEmpty() && !p.getInventory().add(s)) {
                p.drop(s, false);
            }
        }
    }
}