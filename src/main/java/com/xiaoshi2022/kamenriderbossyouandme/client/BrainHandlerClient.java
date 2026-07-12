package com.xiaoshi2022.kamenriderbossyouandme.client;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.event.HenshinEvent;
import com.jpigeon.ridebattlelib.common.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID, value = Dist.CLIENT)
public class BrainHandlerClient {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        boolean hasInvisible = player.getActiveEffects().stream()
                .anyMatch(mobEffectInstance -> mobEffectInstance.is(MobEffects.INVISIBILITY));
        if (hasInvisible) return;

        if (RideBattleAPI.isTransformed(player) && !player.isInvisible()) {
            player.setInvisible(true);
        } else if (!RideBattleAPI.isTransformed(player) && player.isInvisible()) {
            player.setInvisible(false);
        }
    }

    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Post event) {
        Player player = event.getPlayer();
        if (event.getRiderId().equals(RiderIds.BRAIN_ID)) {
            handleBrainHenshinClient(player);
        }
    }

    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Post event) {
        Player player = event.getPlayer();
        if (event.getRiderId().equals(RiderIds.BRAIN_ID)) {
            handleBrainUnhenshinClient(player);
        }
    }

    private static void handleBrainHenshinClient(Player player) {
        player.setInvisible(true);
    }

    private static void handleBrainUnhenshinClient(Player player) {
        player.setInvisible(false);
    }
}