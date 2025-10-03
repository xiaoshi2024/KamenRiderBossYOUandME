package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DukeAbilityHandler {

    // 处理玩家tick事件，保持基本兼容性
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 超能力已修改为召唤Duke骑士实体，相关逻辑在DukeKnightAbilityHandler中处理
        // 保留此方法以保持API兼容性
    }

    // 检查单个盔甲是否装备
    public static boolean isArmorEquipped(ServerPlayer player, ItemStack armorStack) {
        for (ItemStack stack : player.getInventory().armor) {
            if (stack == armorStack) {
                return true;
            }
        }
        return false;
    }
}