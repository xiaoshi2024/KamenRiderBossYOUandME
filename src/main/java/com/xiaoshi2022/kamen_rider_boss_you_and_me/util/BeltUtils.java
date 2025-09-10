package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public class BeltUtils {
    /**
     * 检查玩家是否已经装备了非默认模式的腰带
     * @param player 要检查的玩家
     * @return 如果玩家已经装备了非默认模式的腰带，则返回true
     */
    public static boolean hasActiveLockseed(Player player) {
        // 检查创世纪腰带
        Optional<SlotResult> genesisBelt = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

        if (genesisBelt.isPresent()) {
            ItemStack beltStack = genesisBelt.get().stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            if (belt.getMode(beltStack) != Genesis_driver.BeltMode.DEFAULT) {
                return true;
            }
        }

        // 检查战极腰带
        Optional<SlotResult> sengokuBelt = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof sengokudrivers_epmty));

        if (sengokuBelt.isPresent()) {
            ItemStack beltStack = sengokuBelt.get().stack();
            sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();
            if (belt.getMode(beltStack) != sengokudrivers_epmty.BeltMode.DEFAULT) {
                return true;
            }
        }

        // 检查黑暗Kiva腰带
        Optional<SlotResult> drakKivaBelt = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof DrakKivaBelt));

        if (drakKivaBelt.isPresent()) {
            ItemStack beltStack = drakKivaBelt.get().stack();
            DrakKivaBelt belt = (DrakKivaBelt) beltStack.getItem();
            // 假设DrakKivaBelt没有模式变化，始终返回false
            return false;
        }

        // 检查 Two_sidriver 腰带
        Optional<SlotResult> twoSidriverBelt = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Two_sidriver));

        if (twoSidriverBelt.isPresent()) {
            ItemStack beltStack = twoSidriverBelt.get().stack();
            Two_sidriver belt = (Two_sidriver) beltStack.getItem();
            if (belt.getDriverType(beltStack) != Two_sidriver.DriverType.DEFAULT) {
                return true;
            }
        }

        return false;
    }


    /**
     * 获取腰带模式字符串，用于提示信息
     * @param player 要检查的玩家
     * @return 腰带模式的字符串表示
     */
    public static String getCurrentBeltMode(Player player) {
        // 检查创世纪腰带
        Optional<SlotResult> genesisBelt = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

        if (genesisBelt.isPresent()) {
            ItemStack beltStack = genesisBelt.get().stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            return belt.getMode(beltStack).name();
        }

        // 检查战极腰带
        Optional<SlotResult> sengokuBelt = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof sengokudrivers_epmty));

        if (sengokuBelt.isPresent()) {
            ItemStack beltStack = sengokuBelt.get().stack();
            sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();
            return belt.getMode(beltStack).name();
        }

        // 检查黑暗Kiva腰带
        Optional<SlotResult> drakKivaBelt = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof DrakKivaBelt));

        if (drakKivaBelt.isPresent()) {
            // 假设DrakKivaBelt没有模式变化，始终返回DEFAULT
            return "DEFAULT";
        }

        // 检查 Two_sidriver 腰带
        Optional<SlotResult> twoSidriverBelt = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Two_sidriver));

        if (twoSidriverBelt.isPresent()) {
            ItemStack beltStack = twoSidriverBelt.get().stack();
            Two_sidriver belt = (Two_sidriver) beltStack.getItem();
            return belt.getDriverType(beltStack).name();
        }

        return "DEFAULT";
    }
}