package com.xiaoshi2022.kamenriderbossyouandme.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;
import java.util.function.Predicate;

public class CurioUtils {

    /**
     * 查找第一个符合条件的饰品
     */
    public static Optional<SlotResult> findFirstCurio(Player player, Predicate<ItemStack> predicate) {
        if (player == null) return Optional.empty();

        var curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isEmpty()) return Optional.empty();

        return curiosInventory.get().findFirstCurio(predicate);
    }

    /**
     * 强制装备腰带（原有方法保持不变）
     */
    public static void forceEquipBelt(Player player, ItemStack beltStack) {
        if (player == null || beltStack == null || beltStack.isEmpty()) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.setEquippedCurio("belt", 0, beltStack);
        });
    }

    /**
     * 强制移除腰带
     */
    public static void forceUnequipBelt(Player player, SlotContext slotContext) {
        if (player == null || slotContext == null) return;

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.getStacksHandler(slotContext.identifier()).ifPresent(stackHandler -> {
                // 清空指定槽位
                stackHandler.getStacks().setStackInSlot(slotContext.index(), ItemStack.EMPTY);
                stackHandler.update();
            });
        });
    }
}