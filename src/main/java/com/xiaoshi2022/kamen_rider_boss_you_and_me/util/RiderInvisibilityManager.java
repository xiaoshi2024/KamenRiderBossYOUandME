package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.stream.Stream;

public final class RiderInvisibilityManager {

    /** 给玩家加/移除隐身 Buff */
    public static void updateInvisibility(Player player) {
        boolean wearingRider = Stream.of(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmor)
                .map(player::getItemBySlot)
                .anyMatch(s -> s.getItem() instanceof KamenBossArmor);

        // 无限时长 + 无粒子
        if (wearingRider) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    Integer.MAX_VALUE,
                    0,
                    false, false
            ));
        } else {
            player.removeEffect(MobEffects.INVISIBILITY);
        }
    }
}