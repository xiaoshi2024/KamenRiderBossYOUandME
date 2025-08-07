package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.EquipmentSlot;

public class HeartCoreEvent {
    public HeartCoreEvent(Player player) {
        // 给玩家装备盔甲
        equipArmor(player);
        // 触发动画
        triggerHenshinAnimation(player);
    }

    private void equipArmor(Player player) {
        // 保存玩家当前的盔甲
        ItemStack[] originalArmor = saveCurrentArmor(player);

        // 给玩家装备变身用的盔甲
        equipTransformationArmor(player);

        // 如果玩家背包中有空间，将原来的盔甲存入背包
        restoreOriginalArmorToInventory(player, originalArmor);
    }

    private ItemStack[] saveCurrentArmor(Player player) {
        // 保存玩家当前的盔甲
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armor[i] = player.getInventory().armor.get(i).copy();
        }
        return armor;
    }

    private void equipTransformationArmor(Player player) {
        // 给玩家装备变身用的盔甲
        ItemStack helmet = new ItemStack(ModItems.RIDER_BARONS_HELMET.get());
        ItemStack chestplate = new ItemStack(ModItems.RIDER_BARONS_CHESTPLATE.get());
        ItemStack leggings = new ItemStack(ModItems.RIDER_BARONS_LEGGINGS.get());

        player.getInventory().armor.set(3, helmet);
        player.getInventory().armor.set(2, chestplate);
        player.getInventory().armor.set(1, leggings);
    }

    private void restoreOriginalArmorToInventory(Player player, ItemStack[] originalArmor) {
        // 将原来的盔甲存入玩家的背包
        for (int i = 0; i < 4; i++) {
            if (!originalArmor[i].isEmpty()) {
                if (!player.getInventory().add(originalArmor[i])) {
                    // 如果背包没有空间，将盔甲丢在地上
                    player.drop(originalArmor[i], false);
                }
            }
        }
    }

    private void triggerHenshinAnimation(Player player) {
        // 触发变身动画
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.getItem() instanceof rider_baronsItem armor) {
                    // 触发动画
                    armor.triggerHenshin(player, "henshin");
                }
            }
        }
    }
}