package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
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
                    // 停止播放SUPER_BEST_MATCH音效
                    net.minecraft.resources.ResourceLocation soundLoc = new net.minecraft.resources.ResourceLocation(
                            "kamen_rider_boss_you_and_me",
                            "super_best_match"
                    );
                    // 发送停止音效数据包到所有客户端
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket soundStopPacket = new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket(player.getId(), soundLoc);
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToServer(soundStopPacket);
                    // 同时在客户端直接停止音效
                    if (player.level().isClientSide()) {
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        if (mc.getSoundManager() != null) {
                            mc.getSoundManager().stop(soundLoc, net.minecraft.sounds.SoundSource.PLAYERS);
                        }
                    }
                    
                    // 标记玩家正在按住X键
                    isHoldingX.put(player, true);

                // 切换到HAZARD_RT_MOULD模式，触发特效变种模型
                belt.setMode(beltStack, BuildDriver.BeltMode.HAZARD_RT_MOULD);

                // 设置播放mould动画状态
                belt.setIsPlayingMould(beltStack, true);
                belt.setIsPlayingMouldB(beltStack, false);

                // 开始播放mould动画
                belt.triggerAnim(player, "controller", "mould");

                // 触发玩家作动动画
                belt.triggerPlayerAnim(player, "turn");

                // 延迟3秒后循环播放turn动画
                new Thread(() -> {
                    try {
                        Thread.sleep(3000); // 3秒延迟
                        if (isHoldingX.containsKey(player) && isHoldingX.get(player)) {
                            // 设置转动状态
                            belt.setIsTurning(beltStack, true);
                            // 开始播放turn动画
                            belt.triggerAnim(player, "controller", "turn");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
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

                // 检查当前模式是否为HAZARD_RT或HAZARD_RT_MOULD模式
                if (currentMode == BuildDriver.BeltMode.HAZARD_RT || currentMode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
                    // 停止转动状态
                    belt.setIsTurning(beltStack, false);
                    
                    // 设置播放mould_b动画状态
                    belt.setIsPlayingMouldB(beltStack, true);
                    belt.setIsPlayingMould(beltStack, false);
                    //播放音效应该调用腰带里的音效类！
                    player.level().playSound(player, player.blockPosition(), ModBossSounds.HAZARD_HENSHIN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    // 触发mould_b动画，展示变身完成特效
                    belt.triggerAnim(player, "controller", "mould_b");

                    // 触发玩家作动动画
                    belt.triggerPlayerAnim(player, "turn");
                    
                    // 立刻触发build_up动作
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.PlayerAnimationPacket animationPacket = new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.PlayerAnimationPacket(
                            "build_up",
                            player.getId(),
                            true,
                            5,
                            2000
                    );
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToServer(animationPacket);

                    // 延迟14秒穿戴盔甲
                    new Thread(() -> {
                        try {
                            Thread.sleep(8000); // 8秒延迟
                            equipBlackBuildArmor(player);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    
                    // 保持在HAZARD_RT_MOULD模式，不再切换回HAZARD_RT模式
                    // 不需要触发idle动画，让mould_b动画完整播放并停止在最后一帧
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
