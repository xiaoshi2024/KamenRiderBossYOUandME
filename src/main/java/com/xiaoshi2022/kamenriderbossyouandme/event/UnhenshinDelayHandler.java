package com.xiaoshi2022.kamenriderbossyouandme.event;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.Genesis_driver;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler.BloodHandler;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler.BrainHandler;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler.TyrantHandler;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

@EventBusSubscriber(modid = MODID)
public class UnhenshinDelayHandler {

    // 存储需要延迟解除变身的玩家和剩余时间（tick）
    private static final Map<UUID, Integer> pendingUnhenshin = new ConcurrentHashMap<>();
    // 存储玩家对应的腰带类型，用于不同的解除逻辑
    private static final Map<UUID, BeltType> pendingBeltType = new ConcurrentHashMap<>();
    // 存储玩家对应的腰带物品栈（用于返回道具）
    private static final Map<UUID, ItemStack> pendingBeltStack = new ConcurrentHashMap<>();

    public enum BeltType {
        BRAIN,
        TYRANT,
        BLOOD  // Blood骑士 (使用BuildDriver)
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        boolean wasBrainDriver = from.getItem() instanceof BrainDriver;
        boolean wasGenesisDriver = from.getItem() instanceof Genesis_driver;
        boolean wasBuildDriver = from.getItem() instanceof BuildDriver;
        boolean isBrainDriver = to.getItem() instanceof BrainDriver;
        boolean isGenesisDriver = to.getItem() instanceof Genesis_driver;
        boolean isBuildDriver = to.getItem() instanceof BuildDriver;

        // Brain Driver 处理
        if (wasBrainDriver && !isBrainDriver) {
            handleBeltRemoval(player, from, BeltType.BRAIN);
        }
        // Genesis Driver (Tyrant) 处理
        else if (wasGenesisDriver && !isGenesisDriver) {
            handleBeltRemoval(player, from, BeltType.TYRANT);
        }
        // Build Driver (Blood) 处理
        else if (wasBuildDriver && !isBuildDriver) {
            handleBeltRemoval(player, from, BeltType.BLOOD);
        }
        // 重新装备腰带时取消延迟解除
        else if ((isBrainDriver && wasBrainDriver) ||
                (isGenesisDriver && wasGenesisDriver) ||
                (isBuildDriver && wasBuildDriver)) {
            UUID playerUUID = player.getUUID();
            if (hasPendingUnhenshin(playerUUID)) {
                cancelPendingUnhenshin(playerUUID);
                player.sendSystemMessage(
                        Component.literal("§a腰带已重新装备，取消变身解除")
                );
            }
        }
    }

    private static void handleBeltRemoval(ServerPlayer player, ItemStack beltStack, BeltType beltType) {
        UUID playerUUID = player.getUUID();

        // 如果已经有待处理的解除，先取消旧的
        cancelPendingUnhenshin(playerUUID);

        // 根据腰带类型触发不同的解除动画
        switch (beltType) {
            case BRAIN:
                if (beltStack.getItem() instanceof BrainDriver driver) {
                    driver.startReleaseAnimation(player, beltStack);
                    BrainHandler.playSound(player, ModBossSounds.LOCKOFF.get());
                }
                break;
            case TYRANT:
                if (beltStack.getItem() instanceof Genesis_driver driver) {
                    TyrantHandler.handleTyrantPreUnhenshin(player, beltStack);
                }
                break;
            case BLOOD:
                if (beltStack.getItem() instanceof BuildDriver driver) {
                    // ✅ Blood使用BuildDriver的解除动画
                    driver.startReleaseAnimation(player, beltStack);
                    BloodHandler.playSound(player, ModBossSounds.LOCKOFF.get());
                }
                break;
        }

        // 存储延迟解除信息（3秒 = 60 ticks）
        pendingUnhenshin.put(playerUUID, 60);
        pendingBeltType.put(playerUUID, beltType);
        pendingBeltStack.put(playerUUID, beltStack.copy());

        String beltName = switch (beltType) {
            case BRAIN -> "Brain";
            case TYRANT -> "Tyrant";
            case BLOOD -> "Blood";
        };
        player.sendSystemMessage(
                Component.literal("§e" + beltName + "腰带已取下，将在3秒后解除变身...")
        );
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (pendingUnhenshin.isEmpty()) return;

        var iterator = pendingUnhenshin.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            UUID playerUUID = entry.getKey();
            int remainingTicks = entry.getValue() - 1;

            if (remainingTicks <= 0) {
                ServerPlayer player = getPlayerByUUID(playerUUID);
                if (player != null && RideBattleAPI.isTransformed(player)) {
                    // 获取腰带类型和腰带栈
                    BeltType beltType = pendingBeltType.get(playerUUID);
                    ItemStack beltStack = pendingBeltStack.get(playerUUID);

                    try {
                        switch (beltType) {
                            case BRAIN:
                                BrainHandler.performUnhenshin(player);
                                break;
                            case TYRANT:
                                TyrantHandler.performUnhenshin(player);
                                break;
                            case BLOOD:
                                // ✅ Blood的完整解除逻辑 (返回伟大龙 + 危险扳机)
                                BloodHandler.performUnhenshin(player);
                                break;
                            default:
                                RideBattleAPI.unTransform(player);
                                break;
                        }

                        player.sendSystemMessage(
                                Component.literal("§a✅ 变身已解除，道具已返回")
                        );
                    } catch (Exception e) {
                        KamenRiderBossYOUandME.LOGGER.error("解除变身失败: {}", e.getMessage());
                        RideBattleAPI.unTransform(player);
                    }
                }
                iterator.remove();
                pendingBeltType.remove(playerUUID);
                pendingBeltStack.remove(playerUUID);
            } else {
                entry.setValue(remainingTicks);
            }
        }
    }

    private static ServerPlayer getPlayerByUUID(UUID uuid) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getPlayerList().getPlayer(uuid);
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelPendingUnhenshin(player.getUUID());
        }
    }

    public static void cancelPendingUnhenshin(UUID playerUUID) {
        pendingUnhenshin.remove(playerUUID);
        pendingBeltType.remove(playerUUID);
        pendingBeltStack.remove(playerUUID);
    }

    public static boolean hasPendingUnhenshin(UUID playerUUID) {
        return pendingUnhenshin.containsKey(playerUUID);
    }

    public static BeltType getPendingBeltType(UUID playerUUID) {
        return pendingBeltType.get(playerUUID);
    }
}