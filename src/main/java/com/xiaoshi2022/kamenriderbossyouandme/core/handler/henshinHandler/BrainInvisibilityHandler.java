package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.common.event.HenshinEvent;
import com.jpigeon.ridebattlelib.common.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

@EventBusSubscriber(modid = MODID)
public class BrainInvisibilityHandler {

    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Post event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ResourceLocation riderId = event.getRiderId();
        if (riderId.equals(RiderIds.BRAIN_ID)) {
            setBrainInvisible(serverPlayer, true);
        }
    }

    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Post event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ResourceLocation riderId = event.getRiderId();
        if (riderId.equals(RiderIds.BRAIN_ID)) {
            setBrainInvisible(serverPlayer, false);
        }
    }

    private static void setBrainInvisible(ServerPlayer player, boolean invisible) {
        player.setInvisible(invisible);
        KamenRiderBossYOUandME.LOGGER.debug("Brain隐身状态同步: {} -> {}", player.getName().getString(), invisible);
    }
}