package com.xiaoshi2022.kamenriderbossyouandme.event;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
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

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class UnhenshinDelayHandler {

    // 存储需要延迟解除变身的玩家和剩余时间（tick）
    private static final Map<UUID, Integer> pendingUnhenshin = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        boolean wasBrainDriver = from.getItem() instanceof BrainDriver;
        boolean isBrainDriver = to.getItem() instanceof BrainDriver;

        if (wasBrainDriver && !isBrainDriver) {
            handleBeltRemoval(player, from);
        }
    }

    private static void handleBeltRemoval(ServerPlayer player, ItemStack beltStack) {
        UUID playerUUID = player.getUUID();

        // 如果已经有待处理的解除，先取消旧的
        cancelPendingUnhenshin(playerUUID);

        // 触发BrainDriver的解除动画
        if (beltStack.getItem() instanceof BrainDriver driver) {
            driver.startReleaseAnimation(player, beltStack);
        }

        // 存储延迟解除信息（3秒 = 60 ticks，假设20 ticks/秒）
        pendingUnhenshin.put(playerUUID, 60);

        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("腰带已取下，将在3秒后解除变身...")
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
                // ✅ 修复：直接在服务端执行解除变身，而不是发送包
                ServerPlayer player = getPlayerByUUID(playerUUID);
                if (player != null) {
                    // 直接调用 API 解除变身
                    RideBattleAPI.unTransform(player);
                    player.sendSystemMessage(
                            Component.literal("变身已解除")
                    );
                }
                iterator.remove();
            } else {
                entry.setValue(remainingTicks);
            }
        }
    }

    /**
     * 根据 UUID 获取 ServerPlayer
     */
    private static ServerPlayer getPlayerByUUID(UUID uuid) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getPlayerList().getPlayer(uuid);
        }
        return null;
    }

    @SubscribeEvent
    public static void onCurioChangeForReequip(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack to = event.getTo();

        if (to.getItem() instanceof BrainDriver) {
            UUID playerUUID = player.getUUID();
            if (hasPendingUnhenshin(playerUUID)) {
                cancelPendingUnhenshin(playerUUID);
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("取消变身解除")
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelPendingUnhenshin(player.getUUID());
        }
    }

    public static void cancelPendingUnhenshin(UUID playerUUID) {
        pendingUnhenshin.remove(playerUUID);
    }

    public static boolean hasPendingUnhenshin(UUID playerUUID) {
        return pendingUnhenshin.containsKey(playerUUID);
    }
}