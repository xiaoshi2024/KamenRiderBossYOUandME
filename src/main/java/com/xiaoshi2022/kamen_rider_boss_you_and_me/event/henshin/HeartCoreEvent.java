package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.EquipmentSlot;

public class HeartCoreEvent {
    private final String riderType; // 新增：骑士类型字段

    // 原有构造函数保持不变，向后兼容
    public HeartCoreEvent(Player player) {
        this(player, "BARONS"); // 默认使用BARONS形态
    }

    // 新增构造函数，支持指定骑士类型
    public HeartCoreEvent(Player player, String riderType) {
        this.riderType = riderType;

        // 检查是否佩戴创世纪驱动器
        if (isGenesisDriverUser(player)) {
            handleGenesisTransformation(player);
        } else {
            // 原有变身逻辑
            equipArmor(player);
            triggerHenshinAnimation(player);
        }
    }

    // 检查玩家是否佩戴创世纪驱动器
    private boolean isGenesisDriverUser(Player player) {
        return CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver).isPresent();
    }

    // 处理创世纪驱动器变身
    private void handleGenesisTransformation(Player player) {
        // 保存当前盔甲
        ItemStack[] originalArmor = saveCurrentArmor(player);

        // 根据骑士类型装备不同装甲
        switch(riderType) {
            case "LEMON_ENERGY":
                equipLemonEnergyArmor(player);
                break;
            case "BARONS":
            default:
                equipBaronsArmor(player);
        }

        // 恢复原有盔甲到背包
        restoreOriginalArmorToInventory(player, originalArmor);

        // 触发创世纪驱动器动画
        triggerGenesisHenshinAnimation(player);
    }

    // 装备柠檬能量装甲
    private void equipLemonEnergyArmor(Player player) {
        ItemStack helmet = new ItemStack(ModItems.BARON_LEMON_HELMET.get());
        ItemStack chestplate = new ItemStack(ModItems.BARON_LEMON_CHESTPLATE.get());
        ItemStack leggings = new ItemStack(ModItems.BARON_LEMON_LEGGINGS.get());

        player.getInventory().armor.set(3, helmet);
        player.getInventory().armor.set(2, chestplate);
        player.getInventory().armor.set(1, leggings);
    }

    // 触发创世纪驱动器变身动画
    private void triggerGenesisHenshinAnimation(Player player) {
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver)
                .ifPresent(curio -> {
                    Genesis_driver belt = (Genesis_driver) curio.stack().getItem();

                    // 根据腰带当前模式触发不同动画
                    if (belt.getMode(curio.stack()) == Genesis_driver.BeltMode.LEMON) {
                        belt.startActionAnimation(player); // 柠檬能量变身动画
                    } else {
                        belt.startShowAnimation(player); // 默认变身动画
                    }
                });
    }

    // 原有方法保持不变
    private void equipArmor(Player player) {
        ItemStack[] originalArmor = saveCurrentArmor(player);
        equipTransformationArmor(player);
        restoreOriginalArmorToInventory(player, originalArmor);
    }

    private ItemStack[] saveCurrentArmor(Player player) {
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armor[i] = player.getInventory().armor.get(i).copy();
        }
        return armor;
    }

    private void equipTransformationArmor(Player player) {
        ItemStack helmet = new ItemStack(ModItems.RIDER_BARONS_HELMET.get());
        ItemStack chestplate = new ItemStack(ModItems.RIDER_BARONS_CHESTPLATE.get());
        ItemStack leggings = new ItemStack(ModItems.RIDER_BARONS_LEGGINGS.get());

        player.getInventory().armor.set(3, helmet);
        player.getInventory().armor.set(2, chestplate);
        player.getInventory().armor.set(1, leggings);
    }

    private void restoreOriginalArmorToInventory(Player player, ItemStack[] originalArmor) {
        for (int i = 0; i < 4; i++) {
            if (!originalArmor[i].isEmpty()) {
                if (!player.getInventory().add(originalArmor[i])) {
                    player.drop(originalArmor[i], false);
                }
            }
        }
    }

    private void triggerHenshinAnimation(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.getItem() instanceof rider_baronsItem armor) {
                    armor.triggerHenshin(player, "henshin");
                }
            }
        }
    }

    // 新增：装备Barons装甲(与原有equipTransformationArmor相同，但更通用)
    private void equipBaronsArmor(Player player) {
        equipTransformationArmor(player);
    }
}