package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.TransformationRequestPacket;
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
                Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
                        .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty));

                if (beltOptional.isPresent()) {
                    // 只发送请求到服务端，不处理任何逻辑
                    PacketHandler.INSTANCE.sendToServer(
                            new TransformationRequestPacket(player.getId())
                    );
                }
            }
        }
    }
}