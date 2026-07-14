package com.xiaoshi2022.kamenriderbossyouandme.core.handler;

import com.jpigeon.ridebattlelib.client.key.KeyBindings;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.BuildHenshinKeyPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID, value = Dist.CLIENT)
public class ClientKeyHandler {

    private static boolean lastKeyState = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // ✅ 使用 RideBattleLib 的 DRIVER_KEY (默认 G 键)
        boolean keyDown = KeyBindings.DRIVER_KEY.isDown();

        // 只在状态变化时发送包
        if (keyDown != lastKeyState) {
            lastKeyState = keyDown;
            Player player = mc.player;
            UUID playerId = player.getUUID();

            PacketDistributor.sendToServer(new BuildHenshinKeyPacket(playerId, keyDown));

            KamenRiderBossYOUandME.LOGGER.debug("发送按键状态: player={}, pressed={}",
                    player.getName().getString(), keyDown);
        }
    }
}