package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public interface KamenBossArmor {
    void tick(Player player);

    default int getResistanceLevel() {
        return 0;
    }

    default int getStrengthLevel() {
        return 2;
    }

    // 应用力量效果，不干扰原版更高等级的力量
    default void applyStrengthEffect(Player player) {
        if (!player.level().isClientSide()) {
            int strengthLevel = this.getStrengthLevel();
            if (strengthLevel > 0) {
                int targetAmp = strengthLevel - 1;
                MobEffectInstance existing = player.getEffect(MobEffects.DAMAGE_BOOST);
                
                // 只有在玩家没有力量效果，或者现有效果等级低于我们提供的等级时，才添加新效果
                // 这样可以保留玩家通过原版药水获得的更高等级的力量效果
                if (existing == null || existing.getAmplifier() < targetAmp) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            400,
                            targetAmp,
                            false,
                            false // 不显示粒子效果，避免视觉混乱
                    ));
                }
            }
        }
    }

    default void applyResistanceEffect(Player player) {
        if (!player.level().isClientSide()) {
            int resistanceLevel = this.getResistanceLevel();
            if (resistanceLevel > 0) {
                int targetAmp = resistanceLevel - 1;
                MobEffectInstance existing = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
                
                // 只有在玩家没有抗性效果，或者现有效果等级低于我们提供的等级时，才添加新效果
                // 这样可以保留玩家通过原版药水获得的更高等级的抗性效果
                if (existing == null || existing.getAmplifier() < targetAmp) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            400,
                            targetAmp,
                            false,
                            false // 不显示粒子效果，避免视觉混乱
                    ));
                }
            }
        }
    }
}