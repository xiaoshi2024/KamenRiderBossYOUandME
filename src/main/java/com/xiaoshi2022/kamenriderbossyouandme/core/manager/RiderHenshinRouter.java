package com.xiaoshi2022.kamenriderbossyouandme.core.manager;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.config.RiderConfig;
import com.jpigeon.ridebattlelib.common.registry.RiderRegistry;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler.BrainHandler;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler.TyrantHandler;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 变身键路由器
 * <p>
 * ridebattlelib 1.3.3 的服务端按键流程无法识别 Curio 槽位中的驱动器
 * （{@code findActiveDriverConfig} 只扫描原版装备槽，且其内部的
 * {@code FindRiderConfigEvent} 从未被发布），因此 HenshinEvent.Pre
 * 对本模组的 Curio 骑士永远不会触发。
 * <p>
 * 此路由器在模组自己的按键包（BuildHenshinKeyPacket）到达服务端时，
 * 用 {@link RiderConfig#isEquippedByPlayer}（已重写支持 Curio）解析玩家
 * 当前装备的骑士，并直接路由到对应骑士的自定义变身流程：
 * <ul>
 *   <li>Brain → {@link BrainHandler#triggerBrainHenshin}</li>
 *   <li>Tyrant → {@link TyrantHandler#tryTriggerHenshin}</li>
 *   <li>Blood（BuildDriver）→ 不处理，由长按摇动流程变身</li>
 * </ul>
 */
public class RiderHenshinRouter {

    private static final long COOLDOWN_TICKS = 100;
    private static final Map<UUID, Long> lastHenshinTime = new HashMap<>();

    /**
     * 变身键按下时调用（服务端）
     */
    public static void onHenshinKeyPressed(Player player) {
        if (player == null || player.level().isClientSide()) return;
        if (RideBattleAPI.isTransformed(player)) return;

        for (RiderConfig config : RiderRegistry.getRegisteredRiders()) {
            if (!config.isEquippedByPlayer(player)) continue;

            route(player, config.getRiderId());
            return;
        }
    }

    private static void route(Player player, ResourceLocation riderId) {
        if (riderId.equals(RiderIds.BRAIN_ID)) {
            triggerBrain(player);
        } else if (riderId.equals(RiderIds.TYRANT_ID)) {
            TyrantHandler.tryTriggerHenshin(player);
        }
        // Blood（BuildDriver）：短按不变身，由长按摇动流程（BuildHenshinKeyHandler）处理
    }

    private static void triggerBrain(Player player) {
        UUID playerId = player.getUUID();
        long currentTick = player.level().getGameTime();
        Long lastTime = lastHenshinTime.get(playerId);
        if (lastTime != null && currentTick - lastTime < COOLDOWN_TICKS) {
            KamenRiderBossYOUandME.LOGGER.info("Brain变身冷却中: {}", player.getName().getString());
            return;
        }
        lastHenshinTime.put(playerId, currentTick);

        BrainHandler.triggerBrainHenshin(player);
    }
}
