package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.evil;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.evil.LeftClickShiftPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public class LeftClickListener {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player == null || !player.isShiftKeyDown()) return;

        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof TwoWeaponItem)) return;

        // 通知服务端
        PacketHandler.INSTANCE.sendToServer(new LeftClickShiftPacket(player.getUUID()));
    }
}
