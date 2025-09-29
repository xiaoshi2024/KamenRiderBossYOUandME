package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.darkKiva.DarkKivaItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.evilbats.EvilBatsArmor;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.tyrant.TyrantItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.zangetsu_shin.ZangetsuShinItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sigurd.Sigurd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

                // 2. 检查是否是蝙蝠形态
                boolean isBatForm = isInBatForm(player);

                // 3. 检查是否需要补偿
                if (isBatForm && !hasBatFormWeaponInInventory(player)) {
                    // 如果是蝙蝠形态且背包中没有蝙蝠形态的武器，则进行补偿
                    returnBatFormItems(player);
                }

                // 4. 再清空盔甲
                clearTransformationArmor(player);

                // 5. 返还腰带
                returnBelt(player, isBatForm);

                // 6. 返还眼魂
                returnNecromEye(player);

                // 7. 返还刚才记下来的锁种
                List<ItemStack> lockseeds = getLockseedStacksByType(lockseedType);
                for (ItemStack stack : lockseeds) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }

        }
    }

    private static void returnNecromEye(Player player) {
        /* 只在服务端跑 */
        if (player.level().isClientSide) return;

        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
            /* ===== 取证开始 ===== */
            boolean mega = vars.isMegaUiorderTransformed;
            boolean standby = vars.isNecromStandby;
//            String msg = String.format(
//                    "[returnNecromEye] mega=%s, standby=%s, hasArmor=%s",
//                    mega, standby, hasTransformationArmor(player)
//            );
//            System.out.println(msg);                       // 写在服务端 latest.log
//            player.sendSystemMessage(Component.literal(msg)); // 聊天栏
            /* ===== 取证结束 ===== */

            if (mega || standby) {
                ItemStack eye = new ItemStack(ModItems.NECROM_EYE.get());
                if (!player.getInventory().add(eye)) {
                    player.drop(eye, false);
                }
//                player.sendSystemMessage(
//                        Component.literal("§e[眼魂] 已返还！").withStyle(ChatFormatting.YELLOW)
//                );
                vars.isMegaUiorderTransformed = false;
                vars.isNecromStandby = false;
            }
        });
    }


    // 修改：检查玩家是否处于蝙蝠形态
    private static boolean isInBatForm(Player player) {
        // 检查是否装备了邪恶蝙蝠盔甲
        if (hasEvilBatsArmor(player)) {
            return true;
        }

        // 检查双面驱动器是否是蝙蝠形态
        return CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof Two_sidriver)
                .map(curio -> {
                    ItemStack belt = curio.stack();
                    Two_sidriver.DriverType driverType = ((Two_sidriver) belt.getItem()).getDriverType(belt);
                    return driverType == Two_sidriver.DriverType.BAT;
                })
                .orElse(false);
    }

    // 修改：检查玩家背包或手中是否有蝙蝠形态的武器
    private static boolean hasBatFormWeaponInInventory(Player player) {
        // 检查主手和副手
        if (isBatWeapon(player.getMainHandItem()) || isBatWeapon(player.getOffhandItem())) {
            return true;
        }

        // 检查背包
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isBatWeapon(stack)) {
                return true;
            }
        }
        return false;
    }

    // 辅助方法检查是否为蝙蝠武器
    private static boolean isBatWeapon(ItemStack stack) {
        // 检查是否为三种武器类型之一
        if (stack.getItem() instanceof TwoWeaponItem ||
            stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem ||
            stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem) {
            
            // 统一使用TwoWeaponItem的静态方法检测变体
            return TwoWeaponItem.getVariant(stack) == TwoWeaponItem.Variant.BAT;
        }
        return false;
    }

    private static void returnBelt(Player player, boolean isBatForm) {
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

        // 4. 双面驱动器 ------------------------------------------------------------
        CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof Two_sidriver)
                .ifPresent(curio ->
                {
                    ItemStack belt = curio.stack().copy();
                    // 如果是蝙蝠形态，重置为默认形态
                    if (isBatForm) {
                        ((Two_sidriver) belt.getItem()).setDriverType(belt, Two_sidriver.DriverType.DEFAULT);
                    }

                    if (!player.getInventory().add(belt)) player.drop(belt, false);

                    // 真正清槽
                    CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                            inv.getStacksHandler(curio.slotContext().identifier()).ifPresent(handler ->
                                    handler.getStacks().setStackInSlot(curio.slotContext().index(), ItemStack.EMPTY)
                            )
                    );
                });

        // 5. 手环驱动器（Mega_uiorder）-------------------------------------------
        CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof Mega_uiorder)
                .ifPresent(curio -> {
                    ItemStack bracelet = curio.stack().copy();
                    Mega_uiorder ui = (Mega_uiorder) bracelet.getItem();
                    ui.switchMode(bracelet, Mega_uiorder.Mode.DEFAULT);

                    // ✅ 优先放进背包，满了才掉落
                    if (!player.getInventory().add(bracelet)) {
                        player.drop(bracelet, false);
                    }

                    // ✅ 清槽
                    if (!player.level().isClientSide) {
                        CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                                inv.getStacksHandler(curio.slotContext().identifier()).ifPresent(handler ->
                                        handler.getStacks().setStackInSlot(curio.slotContext().index(), ItemStack.EMPTY)
                                )
                        );
                    }

                    // ✅ 清除玩家状态变量，防止重复返还
                    player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
                    });
                });
    }

    // 新增：返还蝙蝠形态相关的物品
    private static void returnBatFormItems(Player player) {
        // 检查腰带是否为蝙蝠形态
        boolean isBeltBatForm = CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof Two_sidriver)
                .map(curio -> {
                    ItemStack belt = curio.stack();
                    Two_sidriver.DriverType driverType = ((Two_sidriver) belt.getItem()).getDriverType(belt);
                    return driverType == Two_sidriver.DriverType.BAT;
                })
                .orElse(false);

        // 只有当腰带是蝙蝠形态时才给予补偿
        if (isBeltBatForm) {
            // 返还蝙蝠印章
            ItemStack batStamp = new ItemStack(ModItems.BAT_STAMP.get());
            if (!player.getInventory().add(batStamp)) {
                player.drop(batStamp, false);
            }

            // 返还普通双面武器（非蝙蝠形态的武器）
            ItemStack normalWeapon = new ItemStack(ModItems.TWO_WEAPON.get());
            // 确保武器是默认形态
            if (normalWeapon.getItem() instanceof TwoWeaponItem) {
                ((TwoWeaponItem) normalWeapon.getItem()).setWeaponType(normalWeapon, TwoWeaponItem.Variant.DEFAULT);
            }

            if (!player.getInventory().add(normalWeapon)) {
                player.drop(normalWeapon, false);
            }
        }
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
                        stack.getItem() instanceof EvilBatsArmor ||
                        stack.getItem() instanceof DarkKivaItem
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasEvilBatsArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.getItem() instanceof EvilBatsArmor) {
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
                    armorStack.getItem() instanceof EvilBatsArmor ||
                    armorStack.getItem() instanceof DarkKivaItem ||
                    armorStack.getItem() instanceof Marika) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }
        
        // 玩家死亡时移除隐身效果
        player.removeEffect(MobEffects.INVISIBILITY);
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
        } else if (chestplate.getItem() == ModItems.DARK_ORANGELS_CHESTPLATE.get()){
            return "ORANGE";
        } else if (chestplate.getItem() == ModItems.ZANGETSU_SHIN_CHESTPLATE.get()) {
            return "MELON";
        } else if (chestplate.getItem() == ModItems.TYRANT_CHESTPLATE.get()) {
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
                break;
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