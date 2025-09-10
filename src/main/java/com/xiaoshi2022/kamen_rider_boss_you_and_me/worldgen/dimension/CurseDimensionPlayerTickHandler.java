package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CurseDimensionPlayerTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            if (player.level().dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {
                // 使用持久化数据存储检查基夫DNA
                CompoundTag persistentData = player.getPersistentData();
                CompoundTag modData = persistentData.getCompound("kamen_rider_boss_you_and_me");
                boolean hasFactor = modData.getBoolean("hasGiifuDna");

                if (!hasFactor && player.tickCount % 40 == 0) {
                    player.hurt(player.damageSources().magic(), 2.0F);
                }
            }
        }
    }

    // 新增：设置基夫DNA状态的方法
    public static void setGiifuDna(Player player, boolean hasDna) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound("kamen_rider_boss_you_and_me");
        modData.putBoolean("hasGiifuDna", hasDna);
        persistentData.put("kamen_rider_boss_you_and_me", modData);
    }

    // 新增：获取基夫DNA状态的方法
    public static boolean hasGiifuDna(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound("kamen_rider_boss_you_and_me");
        return modData.getBoolean("hasGiifuDna");
    }
}