package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
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

import java.util.ArrayList;
import java.util.List;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.ORANGEFRUIT;

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
                List<ItemStack> lockseeds = getLockseedStacksByType(lockseedType);
                for (ItemStack stack : lockseeds) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
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

        // --- 3. DrakKivaBelt（新增） ---
        CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof DrakKivaBelt)
                .ifPresent(curio -> {
                    ItemStack belt = curio.stack().copy();
                    // 如果有模式需要重置可以在这里调用
                    // ((DrakKivaBelt) belt.getItem()).xxxx(belt, ...);
                    if (!player.getInventory().add(belt)) player.drop(belt, false);
                    CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                            inv.getStacksHandler(curio.slotContext().identifier()).ifPresent(handler ->
                                    handler.getStacks().setStackInSlot(curio.slotContext().index(), ItemStack.EMPTY)));
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
                        stack.getItem() instanceof TyrantItem ||
                    stack.getItem() instanceof Sigurd ||
                        stack.getItem() instanceof Dark_orangels ||
                    stack.getItem() instanceof Marika ||
                        stack.getItem() instanceof DarkKivaItem
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
                armorStack.getItem() instanceof TyrantItem ||
                    armorStack.getItem() instanceof Dark_orangels ||
                    armorStack.getItem() instanceof DarkKivaItem ||
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
        }else if (helmet.getItem() == ModItems.DARK_ORANGELS_HELMET.get()){
            return "ORANGE";
        } else if (helmet.getItem() == ModItems.ZANGETSU_SHIN_HELMET.get()) {
            return "MELON";
        } else if (helmet.getItem() == ModItems.TYRANT_HELMET.get()) {
            return "DRAGONFRUIT";
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
        } else if (helmet.getItem() == ModItems.DARK_ORANGELS_CHESTPLATE.get()){
            return "ORANGE";
        } else if (chestplate.getItem() == ModItems.ZANGETSU_SHIN_CHESTPLATE.get()) {
            return "MELON";
        } else if (helmet.getItem() == ModItems.TYRANT_CHESTPLATE.get()) {
            return "DRAGONFRUIT";
        }

        return ""; // 没有找到匹配的盔甲类型
    }

    // 根据类型获取锁种物品栈
    private static List<ItemStack> getLockseedStacksByType(String type) {
        List<ItemStack> list = new ArrayList<>();
        switch (type) {
            case "PEACH":
                list.add(new ItemStack(ModItems.PEACH_ENERGY.get()));
                break;
            case "LEMON":
                list.add(new ItemStack(ModItems.LEMON_ENERGY.get()));
                break;
            case "BANANA":
                list.add(new ItemStack(ModItems.BANANAFRUIT.get()));
                break;
            case "CHERRY":
                list.add(new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY.get()));
                break;

                case "DRAGONFRUIT":
                    list.add(new ItemStack(ModItems.DRAGONFRUIT.get()));
            case "ORANGE":
                // 返回两个锁种
                list.add(new ItemStack(ModItems.LEMON_ENERGY.get()));
                ItemStack darkOrange = new ItemStack(ORANGEFRUIT.get());
                darkOrange.getOrCreateTag().putBoolean("isDarkVariant", true);
                list.add(darkOrange);
                break;
            case "MELON":
                list.add(new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.MELON.get()));
                break;
        }
        return list;
    }
}