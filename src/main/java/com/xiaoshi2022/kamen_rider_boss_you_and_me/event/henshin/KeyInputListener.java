package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.TransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public class KeyInputListener {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBinding.CHANGE_KEY.isDown()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                // 检查玩家是否已经变身或正在解除变身
                if (isPlayerTransformed(player)) {
                    // 如果已经变身或正在解除变身，忽略此次按键事件
                    return;
                }

                // 检查是否佩戴创世纪驱动器
                Optional<SlotResult> genesisDriver = CurioUtils.findFirstCurio(player,
                        stack -> stack.getItem() instanceof Genesis_driver);

                if (genesisDriver.isPresent()) {
                    // 获取当前模式
                    Genesis_driver belt = (Genesis_driver) genesisDriver.get().stack().getItem();
                    String riderType = (belt.getMode(genesisDriver.get().stack()) == Genesis_driver.BeltMode.LEMON)
                            ? "LEMON_ENERGY" : "BARONS";

                    // 发送变身请求
                    PacketHandler.INSTANCE.sendToServer(
                            new TransformationRequestPacket(player.getUUID(), riderType, false)
                    );
                } else {
                    // 原有战极逻辑
                    Optional<SlotResult> sengku = CurioUtils.findFirstCurio(player,
                            stack -> stack.getItem() instanceof sengokudrivers_epmty);

                    if (sengku.isPresent()) {
                        sengokudrivers_epmty belt = (sengokudrivers_epmty) sengku.get().stack().getItem();
                        if (belt.getMode(sengku.get().stack()) == sengokudrivers_epmty.BeltMode.BANANA) {
                            String riderType = "BANANA";
                            PacketHandler.INSTANCE.sendToServer(
                                    new TransformationRequestPacket(player.getUUID(), riderType, false)
                            );
                        } else {
                            // 如果腰带模式不是 BANANA，可以在这里添加日志或提示
                            System.out.println("玩家佩戴的腰带模式不是 BANANA");
                        }
                    } else {
                        // 如果玩家没有佩戴 sengokudrivers_epmty 腰带，可以在这里添加日志或提示
                        System.out.println("玩家没有佩戴 sengokudrivers_epmty 腰带");
                    }
                }
            }
        }
    }

    // 新增方法：检查玩家是否已经变身或正在解除变身
    private static boolean isPlayerTransformed(LocalPlayer player) {
        // 检查玩家是否佩戴了创世纪驱动器或战极腰带
        Optional<SlotResult> genesisDriver = CurioUtils.findFirstCurio(player,
                stack -> stack.getItem() instanceof Genesis_driver);
        Optional<SlotResult> baronsDriver = CurioUtils.findFirstCurio(player,
                stack -> stack.getItem() instanceof sengokudrivers_epmty);

        // 检查腰带的状态
        if (genesisDriver.isPresent()) {
            Genesis_driver belt = (Genesis_driver) genesisDriver.get().stack().getItem();
            return belt.isHenshining;
        } else if (baronsDriver.isPresent()) {
            sengokudrivers_epmty belt = (sengokudrivers_epmty) baronsDriver.get().stack().getItem();
            return belt.isHenshining;
        }

        // 如果没有佩戴任何腰带，返回 false
        return false;
    }
}