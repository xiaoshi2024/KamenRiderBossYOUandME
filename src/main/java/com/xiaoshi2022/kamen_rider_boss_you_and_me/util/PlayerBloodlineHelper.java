package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.BloodlineManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class PlayerBloodlineHelper {

    /** 获取玩家“牙血鬼”血脉纯度（0~1）。 */
    public static float getFangPurity(Player player) {
        return BloodlineManager.get(player).getFangPurity();
    }

    /** 设置玩家“牙血鬼”血脉纯度（0~1）。 */
    public static void setFangPurity(Player player, float purity) {
        BloodlineManager.get(player).setFangPurity(purity);
        // 只在服务端同步
        if (!player.level().isClientSide && player instanceof ServerPlayer sp) {
            BloodlineManager.sync(sp);
            // 在设置纯度后检查是否需要觉醒牙血鬼血脉
            com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.TamedKivatTrigger.checkAndAwakenFangBloodline(sp);
        }
    }

    public static float getVampirismPurity(Player player) {
        if (!isVampirismLoaded()) return 0F;

        try {
            // 1. 拿到 VampirismAPI 类
            Class<?> apiClazz = Class.forName("de.teamlapen.vampirism.api.VampirismAPI");
            // 2. 调用 VampirismAPI.factionRegistry()
            Object reg = apiClazz.getMethod("factionRegistry").invoke(null);
            // 3. 调用 IFactionRegistry#getFaction
            Optional<?> factionOpt = (Optional<?>) reg.getClass()
                    .getMethod("getFaction", Player.class)
                    .invoke(reg, player);
            if (factionOpt.isPresent()) {
                Object faction = factionOpt.get();
                int lvl = (int) faction.getClass()
                        .getMethod("getLevel", Player.class)
                        .invoke(faction, player);
                int maxLvl = (int) faction.getClass()
                        .getMethod("getHighestReachableLevel")
                        .invoke(faction);
                return lvl / (float) maxLvl;
            }
        } catch (Exception ignored) {}
        return 0F;
    }

    private static boolean isVampirismLoaded() {
        return net.minecraftforge.fml.ModList.get().isLoaded("vampirism");
    }

    private PlayerBloodlineHelper() {}
}