package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.armorevnt;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class ClearArmorWhenRemovedHandler {

    /**
     * 每 tick 检查一遍所有在线玩家：
     * 如果处于变身状态，但盔甲不在盔甲槽，就清除它。
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        // 检查玩家是否穿着变身盔甲
        boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);

        // 收集所有“变身盔甲”实例
        List<ItemStack> toRemove = new ArrayList<>();

        // 1. 盔甲槽：检查变身盔甲是否应该在盔甲槽中
        for (int i = 0; i < 4; i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (isTransformationArmor(armorStack)) {
                // 检查玩家是否应该穿着这套盔甲
                if (!isTransformed) {
                    // 非变身玩家穿着变身盔甲，应该清除
                    toRemove.add(armorStack);
                }
            }
        }

        // 2. 背包、副手、鼠标上物品：发现变身盔甲就清掉
        for (ItemStack stack : player.getInventory().items) {
            if (isTransformationArmor(stack)) {
                toRemove.add(stack);
            }
        }
        if (isTransformationArmor(player.getInventory().offhand.get(0))) {
            toRemove.add(player.getInventory().offhand.get(0));
        }

        // 鼠标上的物品
        if (isTransformationArmor(player.containerMenu.getCarried())) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        // 清理背包/副手里的盔甲
        for (ItemStack stack : toRemove) {
            stack.setCount(0);          // 直接删除
        }

        if (!toRemove.isEmpty()) {
            player.displayClientMessage(Component.literal("变身盔甲无法被取下或被非变身玩家持有，已自动清除！"), true);
        }
    }

    public static boolean isTransformationArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem ||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem ||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.brain.Brain ||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke ||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem ||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem ||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd ||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels ||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DarkRiderGhost.DarkRiderGhostItem||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.NapoleonGhost.NapoleonGhostItem||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem||
                stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight.NoxKnight;
    }
}