package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.event.HenshinEvent;
import com.jpigeon.ridebattlelib.common.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Config;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

@EventBusSubscriber(modid = MODID)
public class BrainHandler {

    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Pre event) {
        Player player = event.getPlayer();
        ResourceLocation riderId = event.getRiderId();

        if (riderId.equals(RiderIds.BRAIN_ID)) {
            handleBrainHenshin(player);
        }
    }

    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Post event) {
        Player player = event.getPlayer();
        ResourceLocation riderId = event.getRiderId();

        if (riderId.equals(RiderIds.BRAIN_ID)) {
            handleBrainUnhenshin(player);
        }
    }

    private static void handleBrainHenshin(Player player) {
        playSound(player, ModBossSounds.BRAINRIDER.get());

        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BrainDriver).ifPresent(slotResult -> {
            var beltStack = slotResult.stack();
            BrainDriver belt = (BrainDriver) beltStack.getItem();

            // 正确的顺序：先设置模式和状态，再触发动画
            // 1. 先设置腰带模式为BRAIN
            belt.setMode(beltStack, BrainDriver.BeltMode.BRAIN);
            // 2. 设置其他状态
            belt.setShowing(beltStack, false);
            belt.setActive(beltStack, true);

            if (player instanceof ServerPlayer serverPlayer) {
                belt.triggerHenshinAnimation(serverPlayer, beltStack);
            }

            RideBattleAPI.completeIn(40, player);
        });
    }

    private static void handleBrainUnhenshin(Player player) {
        playSound(player, ModBossSounds.LOCKOFF.get());

        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BrainDriver).ifPresent(slotResult -> {
            var beltStack = slotResult.stack();
            BrainDriver belt = (BrainDriver) beltStack.getItem();

            // 正确的顺序：先设置模式，再触发解除动画
            // 1. 先设置模式为DEFAULT
            belt.setMode(beltStack, BrainDriver.BeltMode.DEFAULT);
            // 2. 设置其他状态
            belt.setActive(beltStack, false);

            if (player instanceof ServerPlayer serverPlayer) {
                belt.triggerCancelAnimation(serverPlayer, beltStack);
            }
        });
    }

    public static void playSound(Player player, SoundEvent soundEvent) {
        RideBattleAPI.playPublicSound(player, soundEvent, ((float) Config.RIDER_SOUNDS_VOLUME.get() / 100));
    }
}