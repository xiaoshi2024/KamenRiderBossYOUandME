package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.armorevnt;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class PreventArmorRemovalHandler {
    // 存储玩家装备的变身盔甲
    private static final Map<Player, Map<EquipmentSlot, ItemStack>> playerArmorMap = new HashMap<>();

    // 监听玩家装备变化事件
    @SubscribeEvent
    public static void onPlayerChangeArmor(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            EquipmentSlot slot = event.getSlot();
            ItemStack oldStack = event.getFrom();
            ItemStack newStack = event.getTo();

            // 如果玩家处于变身状态
            if (PlayerDeathHandler.hasTransformationArmor(player)) {
                // 如果旧物品是变身盔甲而新物品不是
            boolean isOldArmor = oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.brain.Brain ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels ||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DarkRiderGhost.DarkRiderGhostItem||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.NapoleonGhost.NapoleonGhostItem||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika||
                    oldStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight.NoxKnight;

            boolean isNewArmor = newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.brain.Brain ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels ||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DarkRiderGhost.DarkRiderGhostItem||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.NapoleonGhost.NapoleonGhostItem||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika||
                    newStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight.NoxKnight;

                if (isOldArmor && !isNewArmor) {
                    // 恢复原来的盔甲
                    player.setItemSlot(slot, oldStack);
                    player.displayClientMessage(Component.literal("变身状态下无法取下盔甲！"), true);
                }
            }
        }
    }

    // 监听玩家打开容器事件，记录盔甲状态
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (PlayerDeathHandler.hasTransformationArmor(player)) {
            Map<EquipmentSlot, ItemStack> armorState = new HashMap<>();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    armorState.put(slot, player.getItemBySlot(slot).copy());
                }
            }
            playerArmorMap.put(player, armorState);
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (PlayerDeathHandler.hasTransformationArmor(player) && playerArmorMap.containsKey(player)) {
            Map<EquipmentSlot, ItemStack> originalArmorState = playerArmorMap.get(player);

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack currentStack = player.getItemBySlot(slot);
                    ItemStack originalStack = originalArmorState.get(slot);

                    // 如果当前槽位为空或物品与原来不同
                    if (currentStack.isEmpty() || !ItemStack.matches(currentStack, originalStack)) {
                        // 恢复原来的盔甲
                        player.setItemSlot(slot, originalStack);
                        player.displayClientMessage(Component.literal("变身状态下无法取下盔甲！"), true);
                    }
                }
            }

            // 清理记录
            playerArmorMap.remove(player);
        }
    }

    // 监听玩家登出事件，清理记录
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        playerArmorMap.remove(event.getEntity());
    }
}