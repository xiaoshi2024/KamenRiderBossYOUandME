package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmor;

import java.util.stream.Stream;

public final class RiderInvisibilityManager {

    private static final int OUR_DURATION = Integer.MAX_VALUE;   // 无限时间
    private static final int OUR_AMPLIFIER = 31;  // 使用特殊的等级来区分模组添加的隐身效果

    /** 每 tick 调用：齐甲就持续刷新无限时间隐身，缺一不再移除其他来源的隐身效果 */
    public static void updateInvisibility(Player player) {
        boolean wearingRider = Stream.of(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmor)
                .map(player::getItemBySlot)
                .anyMatch(s -> s.getItem() instanceof KamenBossArmor);

        if (wearingRider) {
            // 直接添加新的无限时间隐身效果，覆盖旧效果
            // 参数说明：效果类型、持续时间、等级、是否是来自信标、是否显示图标、是否显示粒子
            player.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    OUR_DURATION,
                    OUR_AMPLIFIER, false, true, false));
        } else {
            // 当玩家不再穿着盔甲时，只移除由本模组添加的隐身效果（特殊等级），保留其他来源的效果
            for (MobEffectInstance effect : player.getActiveEffects()) {
                if (effect.getEffect() == MobEffects.INVISIBILITY && 
                    effect.getAmplifier() == OUR_AMPLIFIER) {
                    player.removeEffect(MobEffects.INVISIBILITY);
                    break;
                }
            }
        }
    }
}