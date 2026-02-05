package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.SlotResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public class BuildDriverKeyHandler {

    // 存储玩家是否正在按住X键
    private static final Map<Player, Boolean> isHoldingX = new HashMap<>();

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        // 检查是否按下或释放了CHANGE_KEY（X键）
        if (event.getKey() == KeyBinding.CHANGE_KEY.getKey().getValue()) {
            if (event.getAction() == 1) { // 按下X键
                handleXKeyPress(player);
            } else if (event.getAction() == 0) { // 释放X键
                handleXKeyRelease(player);
            }
        }
    }

    /**
     * 处理X键按下事件
     */
    private static void handleXKeyPress(Player player) {
        // 检查玩家是否装备了BuildDriver腰带
        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player, item -> item != null && item.getItem() instanceof BuildDriver);
        if (beltOpt.isPresent()) {
            ItemStack beltStack = beltOpt.get().stack();
            BuildDriver belt = (BuildDriver) beltStack.getItem();
            BuildDriver.BeltMode currentMode = belt.getMode(beltStack);

            // 检查当前模式是否为HAZARD_RT模式
            if (currentMode == BuildDriver.BeltMode.HAZARD_RT) {
                // 标记玩家正在按住X键
                isHoldingX.put(player, true);

                // 设置转动状态
                belt.setIsTurning(beltStack, true);

                // 开始播放turn动画
                belt.triggerAnim(player, "controller", "turn");

                // 触发玩家作动动画
                belt.triggerPlayerAnim(player, "turn");
            }
        }
    }

    /**
     * 处理X键释放事件
     */
    private static void handleXKeyRelease(Player player) {
        // 检查玩家是否正在按住X键
        if (isHoldingX.containsKey(player) && isHoldingX.get(player)) {
            // 清除按住状态
            isHoldingX.put(player, false);

            // 检查玩家是否装备了BuildDriver腰带
            Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player, item -> item != null && item.getItem() instanceof BuildDriver);
            if (beltOpt.isPresent()) {
                ItemStack beltStack = beltOpt.get().stack();
                BuildDriver belt = (BuildDriver) beltStack.getItem();
                BuildDriver.BeltMode currentMode = belt.getMode(beltStack);

                // 检查当前模式是否为HAZARD_RT模式
                if (currentMode == BuildDriver.BeltMode.HAZARD_RT) {
                    // 停止转动状态
                    belt.setIsTurning(beltStack, false);

                    // 停止turn动画，切换到idle动画
                    belt.triggerAnim(player, "controller", "idle");

                    // 取消玩家作动动画
                    belt.cancelPlayerAnim(player);

                    // 触发变身，装备BlackBuild盔甲
                    equipBlackBuildArmor(player);
                }
            }
        }
    }

    /**
     * 装备BlackBuild盔甲
     */
    private static void equipBlackBuildArmor(Player player) {
        // 检查玩家是否为本地玩家
        if (player instanceof LocalPlayer localPlayer) {
            // 发送变身数据包到服务器
            com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToServer(
                    new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.BlackBuildTransformationRequestPacket(player.getUUID())
            );
        }
    }

    /**
     * 检查玩家是否正在按住X键
     */
    public static boolean isPlayerHoldingX(Player player) {
        return isHoldingX.getOrDefault(player, false);
    }
}
