package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

@EventBusSubscriber(modid = MODID)
public class BuildHenshinKeyHandler {

    private static final Map<UUID, Integer> pressStartTick = new HashMap<>();
    private static final Map<UUID, Boolean> isKeyDown = new HashMap<>();
    private static final Map<UUID, Boolean> hasTriggeredShake = new HashMap<>();
    // ✅ 改为 public 或者提供 getter
    public static final Map<UUID, Boolean> isShakingActive = new HashMap<>();
    private static final int LONG_PRESS_THRESHOLD = 10;

    // ✅ 或者提供公共方法
    public static boolean isShakingActive(UUID playerId) {
        return isShakingActive.getOrDefault(playerId, false);
    }


    public static void handleKeyPress(Player player, boolean pressed) {
        if (player == null || player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        if (pressed) {
            isKeyDown.put(playerId, true);
            if (!pressStartTick.containsKey(playerId)) {
                pressStartTick.put(playerId, player.tickCount);
                KamenRiderBossYOUandME.LOGGER.info("变身键按下: {}", player.getName().getString());
            }
        } else {
            // 按键松开
            isKeyDown.remove(playerId);
            pressStartTick.remove(playerId);

            KamenRiderBossYOUandME.LOGGER.info("🔄 按键松开: player={}, isShakingActive={}",
                    player.getName().getString(), isShakingActive.getOrDefault(playerId, false));

            // ✅ 如果摇动状态标记为 true，停止摇动（stopShaking 会处理后续所有逻辑）
            if (isShakingActive.getOrDefault(playerId, false)) {
                var beltOpt = CurioUtils.findFirstCurio(player,
                        stack -> stack.getItem() instanceof BuildDriver);
                if (beltOpt.isPresent()) {
                    ItemStack beltStack = beltOpt.get().stack();
                    BuildDriver belt = (BuildDriver) beltStack.getItem();

                    // ✅ 只调用 stopShaking，它会处理音效、动画、延迟和变身
                    KamenRiderBossYOUandME.LOGGER.info("🎬 按键松开，停止摇动!");
                    belt.stopShaking(player, beltStack);
                } else {
                    KamenRiderBossYOUandME.LOGGER.warn("⚠️ 找不到腰带!");
                }

                // 清理状态
                isShakingActive.remove(playerId);
                hasTriggeredShake.remove(playerId);
            } else {
                KamenRiderBossYOUandME.LOGGER.info("❌ 摇动未激活，忽略松开事件");
                if (hasTriggeredShake.getOrDefault(playerId, false)) {
                    hasTriggeredShake.remove(playerId);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        UUID playerId = player.getUUID();

        if (RideBattleAPI.isTransformed(player)) {
            cleanupState(playerId);
            return;
        }

        if (!isKeyDown.getOrDefault(playerId, false)) {
            return;
        }

        var beltOpt = CurioUtils.findFirstCurio(player,
                stack -> stack.getItem() instanceof BuildDriver);

        if (beltOpt.isEmpty()) {
            cleanupState(playerId);
            return;
        }

        ItemStack beltStack = beltOpt.get().stack();
        BuildDriver belt = (BuildDriver) beltStack.getItem();
        BuildDriver.BeltMode mode = belt.getMode(beltStack);

        if (mode != BuildDriver.BeltMode.HAZARD_GD) {
            cleanupState(playerId);
            return;
        }

        if (hasTriggeredShake.getOrDefault(playerId, false)) {
            return;
        }

        int startTick = pressStartTick.getOrDefault(playerId, serverPlayer.tickCount);
        int pressedTicks = serverPlayer.tickCount - startTick;

        if (pressedTicks >= LONG_PRESS_THRESHOLD) {
            KamenRiderBossYOUandME.LOGGER.info("长按检测到 ({} ticks)，开始摇动!", pressedTicks);
            belt.startShaking(player, beltStack);
            hasTriggeredShake.put(playerId, true);
            isShakingActive.put(playerId, true);
        }
    }

    private static void cleanupState(UUID playerId) {
        isKeyDown.remove(playerId);
        pressStartTick.remove(playerId);
        hasTriggeredShake.remove(playerId);
        isShakingActive.remove(playerId);
    }
}