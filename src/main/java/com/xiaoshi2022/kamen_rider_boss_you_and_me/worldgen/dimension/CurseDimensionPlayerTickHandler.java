package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

public class CurseDimensionPlayerTickHandler {
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {
            boolean hasFactor = player.getPersistentData().getBoolean("hasGiifuDna");
            if (!hasFactor && player.tickCount % 40 == 0) {
                player.hurt(player.damageSources().magic(), 2.0F);
            }
        }
    }
}