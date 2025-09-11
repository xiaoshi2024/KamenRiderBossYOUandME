package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CurseDimensionPlayerTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            if (player.level().dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {
                // 检查玩家是否拥有基夫DNA效果
                if (!player.hasEffect(ModEffects.GIIFU_DNA.get()) && player.tickCount % 40 == 0) {
                    player.hurt(player.damageSources().magic(), 2.0F);
                }
            }
        }
    }
}