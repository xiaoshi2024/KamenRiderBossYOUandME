package com.xiaoshi2022.kamen_rider_boss_you_and_me.events;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BaronEventHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LordBaronEntity baron) {
            Player player = event.getEntity();

            // 根据手持物品触发不同对话
            if (player.getMainHandItem().is(ModItems.BANANAFRUIT.get())) {
                baron.challengePlayer(player);
            } else if (player.getMainHandItem().is(Items.BOOK)) {
                baron.askAboutPower(player);
            } else {
                baron.interactWith(player);
            }

            event.setCanceled(true);
        }
    }
}