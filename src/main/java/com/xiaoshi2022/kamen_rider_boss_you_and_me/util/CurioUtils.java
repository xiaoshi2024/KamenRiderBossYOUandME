package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Predicate;

public class CurioUtils {

    /* ------------------ 通用工具 ------------------ */

    /**
     * 安全更新 Curios 槽位物品（5.3.1+ 兼容）
     *
     * @return true 如果更新成功，false 如果更新失败
     */
    public static boolean updateCurioSlot(Player player, String slotId, int index, ItemStack stack) {
        try {
            return CuriosApi.getCuriosInventory(player).map(inv ->
                    inv.getStacksHandler(slotId).map(h -> {
                        if (index >= 0 && index < h.getSlots()) {
                            // 确保在服务器端执行
                            if (!player.level().isClientSide()) {
                                h.getStacks().setStackInSlot(index, stack);
                                h.getCosmeticStacks().setStackInSlot(index, ItemStack.EMPTY);
                                h.update();
                                // 强制同步到所有客户端
                                if (player instanceof ServerPlayer serverPlayer) {
                                    serverPlayer.inventoryMenu.broadcastChanges();
                                }
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }).orElse(false)
            ).orElse(false);
        } catch (Exception e) {
            // 记录异常信息，确保更新过程不会因异常而中断
            e.printStackTrace();
            return false;
        }
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
    
    /**
     * 把物品强制塞进玩家的 back 槽；槽满则塞主手
     */
    public static void forceEquipToBack(ServerPlayer player, ItemStack item) {
        boolean ok = CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(inv -> inv.getStacksHandler("back"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (handler.getStacks().getStackInSlot(i).isEmpty()) {
                            handler.getStacks().setStackInSlot(i, item);
                            handler.update();
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);

        if (!ok) {
            // Curios 槽全满：直接塞主手
            player.getInventory().setItem(player.getInventory().selected, item);
        }
    }
}