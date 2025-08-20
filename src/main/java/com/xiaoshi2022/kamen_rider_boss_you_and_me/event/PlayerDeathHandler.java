package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class PlayerDeathHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (hasTransformationArmor(player)) {

                // 1. 先确定锁种类型（盔甲还在）
                String lockseedType = determineLockseedType(player);

                // 2. 再清空盔甲
                clearTransformationArmor(player);

                // 3. 返还腰带
                returnBelt(player);

                // 4. 返还刚才记下来的锁种
                if (!lockseedType.isEmpty()) {
                    ItemStack lockseed = getLockseedStackByType(lockseedType);
                    if (!lockseed.isEmpty()) {
                        if (!player.getInventory().add(lockseed)) {
                            player.drop(lockseed, false);
                        }
                    }
                }
            }
        }
    }

    private static void returnBelt(Player player) {
        // 1. 创世纪驱动器 ----------------------------------------------------------
        CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof Genesis_driver)
                .ifPresent(curio -> {
                    ItemStack belt = curio.stack().copy();
                    ((Genesis_driver) belt.getItem()).setMode(belt, Genesis_driver.BeltMode.DEFAULT);

                    if (!player.getInventory().add(belt)) player.drop(belt, false);

                    // 真正清槽
                    CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                            inv.getStacksHandler(curio.slotContext().identifier()).ifPresent(handler ->
                                    handler.getStacks().setStackInSlot(curio.slotContext().index(), ItemStack.EMPTY)
                            )
                    );
                });

// 2. 战国驱动器 ------------------------------------------------------------
        CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof sengokudrivers_epmty)
                .ifPresent(curio -> {
                    ItemStack belt = curio.stack().copy();
                    ((sengokudrivers_epmty) belt.getItem()).setMode(belt, sengokudrivers_epmty.BeltMode.DEFAULT);

                    if (!player.getInventory().add(belt)) player.drop(belt, false);

                    // 真正清槽
                    CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                            inv.getStacksHandler(curio.slotContext().identifier()).ifPresent(handler ->
                                    handler.getStacks().setStackInSlot(curio.slotContext().index(), ItemStack.EMPTY)
                            )
                    );
                });

    }

    // 检查玩家是否装备了变身盔甲
    public static boolean hasTransformationArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.getItem() instanceof rider_baronsItem ||
                    stack.getItem() instanceof baron_lemonItem ||
                    stack.getItem() instanceof Duke ||
                    stack.getItem() instanceof RidernecromItem ||
                    stack.getItem() instanceof ZangetsuShinItem ||
                    stack.getItem() instanceof Sigurd ||
                    stack.getItem() instanceof Marika
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    // 清空变身盔甲
    private static void clearTransformationArmor(Player player) {
        for (int i = 0; i < 4; i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (armorStack.getItem() instanceof rider_baronsItem ||
                armorStack.getItem() instanceof baron_lemonItem ||
                armorStack.getItem() instanceof Duke ||
                armorStack.getItem() instanceof RidernecromItem ||
                armorStack.getItem() instanceof ZangetsuShinItem ||
                armorStack.getItem() instanceof Sigurd ||
                armorStack.getItem() instanceof Marika) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }
    }

    // 根据玩家装备的盔甲类型确定锁种类型
    private static String determineLockseedType(Player player) {
        // 检查头盔类型
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() == ModItems.MARIKA_HELMET.get()) {
            return "PEACH";
        } else if (helmet.getItem() == ModItems.BARON_LEMON_HELMET.get() ||
                   helmet.getItem() == ModItems.DUKE_HELMET.get()) {
            return "LEMON";
        } else if (helmet.getItem() == ModItems.RIDER_BARONS_HELMET.get()) {
            return "BANANA";
        } else if (helmet.getItem() == ModItems.SIGURD_HELMET.get()) {
            return "CHERRY";
        } else if (helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get()) {
            return "MELON";
        }

        // 如果没有匹配的头盔，检查胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.getItem() == ModItems.MARIKA_CHESTPLATE.get()) {
            return "PEACH";
        } else if (chestplate.getItem() == ModItems.BARON_LEMON_CHESTPLATE.get() ||
                   chestplate.getItem() == ModItems.DUKE_CHESTPLATE.get()) {
            return "LEMON";
        } else if (chestplate.getItem() == ModItems.RIDER_BARONS_CHESTPLATE.get()) {
            return "BANANA";
        } else if (chestplate.getItem() == ModItems.SIGURD_CHESTPLATE.get()) {
            return "CHERRY";
        } else if (chestplate.getItem() == ModItems.ZANGETSU_SHIN_CHESTPLATE.get()) {
            return "MELON";
        }

        return ""; // 没有找到匹配的盔甲类型
    }

    // 根据类型获取锁种物品栈
    private static ItemStack getLockseedStackByType(String type) {
        switch (type) {
            case "PEACH":
                return new ItemStack(ModItems.PEACH_ENERGY.get());
            case "LEMON":
                return new ItemStack(ModItems.LEMON_ENERGY.get());
            case "BANANA":
                return new ItemStack(ModItems.BANANAFRUIT.get());
            case "CHERRY":
                // 检查是否有樱桃锁种，如果没有可以返回空或者创建一个默认的
                return new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY.get());
            case "MELON":
                // 检查是否有蜜瓜锁种，如果没有可以返回空或者创建一个默认的
                return new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.MELON.get());
            default:
                return ItemStack.EMPTY;
        }
    }
}