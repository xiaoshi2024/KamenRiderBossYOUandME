package com.xiaoshi2022.kamenriderbossyouandme.event;

import com.jpigeon.ridebattlelib.core.system.event.UnhenshinEvent;
import com.jpigeon.ridebattlelib.core.system.network.packet.UnhenshinPacket;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class UnhenshinDelayHandler {

    // 存储需要延迟解除变身的玩家和剩余时间（tick）
    private static final Map<UUID, DelayInfo> pendingUnhenshin = new ConcurrentHashMap<>();

    // 用于存储延迟信息的内部类
    private static class DelayInfo {
        int remainingTicks;
        UnhenshinPacket packet;

        DelayInfo(int ticks, UnhenshinPacket packet) {
            this.remainingTicks = ticks;
            this.packet = packet;
        }

        void tick() {
            remainingTicks--;
        }

        boolean isFinished() {
            return remainingTicks <= 0;
        }
    }

    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        // 检查是否是饰品槽变化
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();

        // 检查是否取下了BrainDriver（从有到无）
        boolean wasBrainDriver = from.getItem() instanceof BrainDriver;
        boolean isBrainDriver = to.getItem() instanceof BrainDriver;

        // 如果取下了腰带（原来有腰带，现在没有）
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

        // 创建解除变身包
        // 注意：根据您的UnhenshinPacket构造函数调整参数
        UnhenshinPacket packet = new UnhenshinPacket(playerUUID);

        // 存储延迟解除信息（3秒 = 60 ticks，假设20 ticks/秒）
        pendingUnhenshin.put(playerUUID, new DelayInfo(60, packet));

        // 发送提示消息
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("腰带已取下，将在3秒后解除变身...")
        );
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (pendingUnhenshin.isEmpty()) return;

        // 使用迭代器安全地遍历和修改
        var iterator = pendingUnhenshin.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            UUID playerUUID = entry.getKey();
            DelayInfo info = entry.getValue();

            // 减少剩余时间
            info.tick();

            if (info.isFinished()) {
                // 时间到，执行解除变身
                UnhenshinPacket packet = info.packet;
                if (packet != null) {
                    // 发送解除变身包到服务器
                    PacketDistributor.sendToServer(packet);
                }

                // 移除这个条目
                iterator.remove();
            }
        }
    }

    /**
     * 当玩家重新装备腰带时，取消延迟解除
     */
    @SubscribeEvent
    public static void onCurioChangeForReequip(CurioChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack to = event.getTo();

        // 如果重新装备了BrainDriver
        if (to.getItem() instanceof BrainDriver) {
            UUID playerUUID = player.getUUID();

            // 如果有待处理的延迟解除，取消它
            if (hasPendingUnhenshin(playerUUID)) {
                cancelPendingUnhenshin(playerUUID);
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("取消变身解除")
                );
            }
        }
    }

    /**
     * 当玩家登出时，清理数据
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelPendingUnhenshin(player.getUUID());
        }
    }

    /**
     * 取消玩家的延迟解除
     */
    public static void cancelPendingUnhenshin(UUID playerUUID) {
        pendingUnhenshin.remove(playerUUID);
    }

    /**
     * 检查玩家是否有待处理的解除变身
     */
    public static boolean hasPendingUnhenshin(UUID playerUUID) {
        return pendingUnhenshin.containsKey(playerUUID);
    }
}