package com.xiaoshi2022.kamenriderbossyouandme.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Predicate;

public class CurioUtils {

    /**
     * 查找玩家第一个匹配条件的饰品
     */
    public static Optional<SlotResult> findFirstCurio(Player player, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(player).flatMap(inv -> inv.findFirstCurio(predicate));
    }

    /**
     * 强制装备腰带到腰带槽位
     */
    public static void forceEquipBelt(Player player, ItemStack beltStack) {
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            // 直接使用 setEquippedCurio 方法设置腰带到腰带槽位
            inv.setEquippedCurio("belt", 0, beltStack);
        });
    }
}