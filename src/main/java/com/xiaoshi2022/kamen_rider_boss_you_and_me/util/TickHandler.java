package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.QueenBeeTransformationRequestPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickHandler {
    // 女王蜂延迟装备队列（从QueenBeeTransformationRequestPacket移过来）
    private static final Map<UUID, Integer> QUEENBEE_ARMOR_DELAY_MAP = new ConcurrentHashMap<>();

    // 设置延迟装备（外部调用）
    public static void setQueenBeeArmorDelay(UUID playerId, int delayTicks) {
        QUEENBEE_ARMOR_DELAY_MAP.put(playerId, delayTicks);
    }

    // 移除延迟（如果需要取消）
    public static void removeQueenBeeArmorDelay(UUID playerId) {
        QUEENBEE_ARMOR_DELAY_MAP.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;
        UUID uuid = player.getUUID();

        // 处理女王蜂延迟装备
        if (QUEENBEE_ARMOR_DELAY_MAP.containsKey(uuid)) {
            int ticksLeft = QUEENBEE_ARMOR_DELAY_MAP.get(uuid) - 1;

            if (ticksLeft <= 0) {
                QUEENBEE_ARMOR_DELAY_MAP.remove(uuid);

                // 重新获取玩家实例，确保安全
                ServerPlayer actualPlayer = (ServerPlayer) player.level().getPlayerByUUID(uuid);
                if (actualPlayer == null || !actualPlayer.isAlive()) {
                    return;
                }

                // 装备女王蜂装甲
                equipQueenBeeArmor(actualPlayer);

                // 在盔甲装备时播放一个特殊音效
                actualPlayer.level().playSound(null, actualPlayer.getX(), actualPlayer.getY(), actualPlayer.getZ(),
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds.QUEENBE_COM.get(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
            } else {
                QUEENBEE_ARMOR_DELAY_MAP.put(uuid, ticksLeft);
            }
        }
    }

    // 装备女王蜂装甲的方法
    private static void equipQueenBeeArmor(ServerPlayer player) {
        // 保存原装备
        net.minecraft.world.item.ItemStack[] originalArmor = new net.minecraft.world.item.ItemStack[4];
        for (int i = 0; i < 4; i++) {
            originalArmor[i] = player.getInventory().armor.get(i).copy();
        }

        // 装备女王蜂装甲
        player.getInventory().armor.set(3, new net.minecraft.world.item.ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.QUINBEE_HELMET.get()));
        player.getInventory().armor.set(2, new net.minecraft.world.item.ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.QUINBEE_CHESTPLATE.get()));
        player.getInventory().armor.set(1, new net.minecraft.world.item.ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.QUINBEE_LEGGINGS.get()));
        player.getInventory().armor.set(0, net.minecraft.world.item.ItemStack.EMPTY); // 清空鞋子

        // 恢复原装备到背包
        for (int i = 0; i < 4; i++) {
            if (!originalArmor[i].isEmpty()) {
                if (!player.getInventory().add(originalArmor[i])) {
                    player.drop(originalArmor[i], false);
                }
            }
        }

        // 输出调试信息
        System.out.println("女王蜂装甲已装备给玩家: " + player.getName().getString());
    }
}