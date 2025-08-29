package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;
import java.util.function.Predicate;

public class CurioUtils {

    /* ------------------ 通用工具 ------------------ */

    /**
     * 安全更新 Curios 槽位物品（5.3.1+ 兼容）
     */
    public static void updateCurioSlot(Player player, String slotId, int index, ItemStack stack) {
        CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                inv.getStacksHandler(slotId).ifPresent(h -> {
                    h.getStacks().setStackInSlot(index, stack);
                    h.getCosmeticStacks().setStackInSlot(index, ItemStack.EMPTY);
                    h.update();
                })
        );
    }

    /**
     * 查找第一个满足条件的 Curio
     */
    public static Optional<SlotResult> findFirstCurio(Player player, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(predicate));
    }

    /* ------------------ 新增：一键装备腰带 ------------------ */

    /**
     * 把腰带强制塞进玩家的 belt 槽；槽满则塞主手
     */
    public static void forceEquipBelt(ServerPlayer player, ItemStack belt) {
        boolean ok = CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(inv -> inv.getStacksHandler("belt"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (handler.getStacks().getStackInSlot(i).isEmpty()) {
                            handler.getStacks().setStackInSlot(i, belt);
                            handler.update();
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);

        if (!ok) {
            // Curios 槽全满：直接塞主手
            player.getInventory().setItem(player.getInventory().selected, belt);
        }
    }
}