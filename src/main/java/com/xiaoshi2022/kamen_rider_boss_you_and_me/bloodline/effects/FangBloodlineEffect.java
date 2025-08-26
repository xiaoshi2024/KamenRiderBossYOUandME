package com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FangBloodlineEffect extends MobEffect {
    public FangBloodlineEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000); // 深红色
        // 每级 +10% 攻击
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                "648D7064-6A3B-4C5B-9B5E-4D9A1E0B1A8D",
                0.1D, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 可以在这里额外写每 tick 的逻辑，比如吸血
    }

    @Override
    public String getDescriptionId() {
        return "kamen_rider_boss_you_and_me.effect.fang_bloodline";
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // 每 tick 都触发
    }
}