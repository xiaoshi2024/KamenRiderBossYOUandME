package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public interface KamenBossArmor {
    void tick(Player player);
    
    /**
     * 为所有实现KamenBossArmor接口的盔甲添加持续抗性1效果
     * 这是一个默认方法，所有实现类都会自动获得此功能
     */
    default void applyResistanceEffect(Player player) {
        // 检查玩家是否穿着至少一件该类型的盔甲
        boolean isWearingArmor = false;
        for(int i = 0; i < player.getInventory().armor.size(); i++) {
            if(player.getInventory().armor.get(i).getItem() instanceof KamenBossArmor) {
                isWearingArmor = true;
                break;
            }
        }
        
        if(isWearingArmor) {
            // 如果穿着盔甲，添加抗性1效果
            // 只有在玩家没有抗性效果或效果即将结束时才添加，不会覆盖原版药水效果
            if (!player.hasEffect(MobEffects.DAMAGE_RESISTANCE) || player.getEffect(MobEffects.DAMAGE_RESISTANCE).getDuration() < 260) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 320, 0, false, false));
            }
        }
        // 不再无条件移除抗性效果，避免干扰原版药水效果
    }
}