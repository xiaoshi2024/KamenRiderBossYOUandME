package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;
import java.util.function.Predicate;

public class CurioUtils {
    /**
     * 安全更新Curios槽位物品 (兼容5.3.1)
     */
    public static void updateCurioSlot(Player player, String slotId, int index, ItemStack stack) {
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
            // 5.3.1版本必须通过getStacksHandler获取处理器
            Optional<ICurioStacksHandler> handler = inventory.getStacksHandler(slotId);
            handler.ifPresent(h -> {
                h.getStacks().setStackInSlot(index, stack);
                h.getCosmeticStacks().setStackInSlot(index, ItemStack.EMPTY);
                h.update(); // 必须手动调用更新
            });
        });
    }

    /**
     * 查找Curio物品 (兼容5.3.1)
     */
    public static Optional<SlotResult> findFirstCurio(Player player, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(predicate));
    }
}