package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.api.RiderManager;
import com.jpigeon.ridebattlelib.core.system.event.HenshinEvent;
import com.jpigeon.ridebattlelib.core.system.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Config;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.DriverSyncPacket;
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

            // 3. 最后设置变身状态并触发动画
            if (player instanceof ServerPlayer serverPlayer) {
                // 这个方法内部会设置henshin状态并触发动画
                belt.triggerHenshinAnimation(serverPlayer, beltStack);
            }

            // 发送网络包同步状态
            PacketHandler.sendToTrackingAndSelf(
                    (ServerPlayer) player,
                    new BeltAnimationPacket(
                            player.getId(),
                            "henshin",
                            BrainDriver.BeltMode.BRAIN
                    )
            );

            PacketHandler.sendToTrackingAndSelf(
                    (ServerPlayer) player,
                    new DriverSyncPacket(
                            player.getId(),
                            BrainDriver.BeltMode.BRAIN
                    )
            );

            // 延迟完成变身 - 增加时间以确保动画播放完成
            RiderManager.scheduleTicks(40, () -> {
                RiderManager.completeHenshin(player);
            });
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

            // 3. 触发解除动画（这个方法内部会设置release状态）
            if (player instanceof ServerPlayer serverPlayer) {
                belt.triggerCancelAnimation(serverPlayer, beltStack);
            }

            // 发送网络包同步状态
            PacketHandler.sendToTrackingAndSelf(
                    (ServerPlayer) player,
                    new BeltAnimationPacket(
                            player.getId(),
                            "cancel",
                            BrainDriver.BeltMode.DEFAULT
                    )
            );

            PacketHandler.sendToTrackingAndSelf(
                    (ServerPlayer) player,
                    new DriverSyncPacket(
                            player.getId(),
                            BrainDriver.BeltMode.DEFAULT
                    )
            );
        });
    }

    public static void playSound(Player player, SoundEvent soundEvent) {
        RiderManager.playPublicSound(player, soundEvent, ((float) Config.RIDER_SOUNDS_VOLUME.get() / 100));
    }
}