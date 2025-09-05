package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.stream.Stream;

public final class RiderInvisibilityManager {

    private static final int OUR_DURATION = 3 * 20;   // 3ms

    /** 每 tick 调用：齐甲就补 3 秒隐身，缺一立刻扒掉“我们发的” */
    public static void updateInvisibility(Player player) {
        boolean wearingRider = Stream.of(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmor)
                .map(player::getItemBySlot)
                .anyMatch(s -> s.getItem() instanceof KamenBossArmor);

        if (wearingRider) {
            // 刷新成 3 秒，带“签名”
            player.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    OUR_DURATION,
                    0, false, false));
        } else {
            // 只扒掉“我们发的”那一段
            MobEffectInstance active = player.getEffect(MobEffects.INVISIBILITY);
            if (active != null && active.getDuration() == OUR_DURATION) {
                player.removeEffect(MobEffects.INVISIBILITY);
            }
        }
    }
}