package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.skill.aidcdskill;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Decade;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AnotherDimensionKick {
    private static final int COOLDOWN = 100; // 5秒冷却
    private static final float DAMAGE_MULTIPLIER = 1.5F; // 伤害倍数
    private static final float RANGE = 10.0F; // 攻击范围

    public static boolean canUse(Another_Decade user) {
        // 只有在目标距离适中且不在冷却中时才能使用
        if (user.getTarget() == null || user.getPersistentData().getInt("AnotherDimensionKickCooldown") > 0) {
            return false;
        }
        
        // 检查距离
        double horizontalDistance = Math.sqrt(user.distanceToSqr(user.getTarget().getX(), user.getY(), user.getTarget().getZ()));
        return horizontalDistance <= RANGE;
    }

    public static void use(Another_Decade user) {
        if (!canUse(user) || user.level().isClientSide()) {
            return;
        }

        LivingEntity target = user.getTarget();
        if (target == null) return;

        // 播放准备音效
        user.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.0F, 0.8F);

        // 计算目标与user的高度差
        double heightDifference = target.getY() - user.getY();
        
        // 跳跃到空中并向目标冲刺
        Vec3 direction = target.position().subtract(user.position()).normalize();
        
        // 根据高度差调整跳跃力度，目标越高跳得越高
        double jumpStrength = 1.2D;
        if (heightDifference > 0) {
            // 目标在高处，增加跳跃力度
            jumpStrength += heightDifference * 0.5D;
        }
        
        user.push(direction.x * 2.0D, jumpStrength, direction.z * 2.0D);

        // 生成紫色卡片状光墙粒子效果
        spawnPurpleWallParticles(user);

        // 对目标造成伤害
        float damage = (float) user.getAttributeValue(com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
        damage *= DAMAGE_MULTIPLIER;

        // 如果处于狂暴状态，伤害额外增加
        if (user.isEnraged()) {
            damage *= 1.2F;
        }

        // 修复：使用正确的DamageSource创建方法
        target.hurt(user.damageSources().mobAttack(user), damage);

        // 击退目标
        target.push(direction.x * 1.5D, 0.5D, direction.z * 1.5D);

        // 给予目标短暂的减速效果
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));

        // 设置冷却
        user.getPersistentData().putInt("AnotherDimensionKickCooldown", COOLDOWN);

        // 播放击中音效
        user.playSound(SoundEvents.IRON_GOLEM_HURT, 1.0F, 0.9F);
    }

    private static void spawnPurpleWallParticles(Another_Decade user) {
        if (user.level() instanceof ServerLevel serverLevel) {
            // 生成十面紫色卡片状光墙效果
            for (int i = 0; i < 10; i++) {
                double angle = (i * 36) * Math.PI / 180;
                double xOffset = Math.cos(angle) * 3.0D;
                double zOffset = Math.sin(angle) * 3.0D;

                for (int y = 0; y < 5; y++) {
                    serverLevel.sendParticles(
                            ParticleTypes.ENCHANTED_HIT,
                            user.getX() + xOffset,
                            user.getY() + y * 0.4D,
                            user.getZ() + zOffset,
                            1,
                            0, 0, 0,
                            0.1D
                    );
                }
            }
        }
    }

    public static void updateCooldown(Another_Decade user) {
        int cooldown = user.getPersistentData().getInt("AnotherDimensionKickCooldown");
        if (cooldown > 0) {
            user.getPersistentData().putInt("AnotherDimensionKickCooldown", cooldown - 1);
        }
    }
}