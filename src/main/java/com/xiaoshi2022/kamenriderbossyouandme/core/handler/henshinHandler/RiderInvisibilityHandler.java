package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.server.event.HenshinEvent;
import com.jpigeon.ridebattlelib.server.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

@EventBusSubscriber(modid = MODID)
public class RiderInvisibilityHandler {

    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Post event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // ✅ 所有骑士变身后隐身
        setRiderInvisible(serverPlayer, true);
    }

    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Post event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // ✅ 所有骑士解除变身后取消隐身
        setRiderInvisible(serverPlayer, false);
    }

    private static void setRiderInvisible(ServerPlayer player, boolean invisible) {
        player.setInvisible(invisible);
        KamenRiderBossYOUandME.LOGGER.debug("骑士隐身状态同步: {} -> {}", player.getName().getString(), invisible);
    }
}