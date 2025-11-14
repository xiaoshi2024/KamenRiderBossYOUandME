package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.skill.aidcdskill;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Decade;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BlastPrison {
    private static final int COOLDOWN = 80; // 4秒冷却
    private static final float DAMAGE_MULTIPLIER = 1.2F; // 伤害倍数
    private static final double EXPLOSION_RADIUS = 5.0D; // 爆炸半径
    private static final int EXPLOSION_COUNT = 3; // 连续爆炸次数

    public static boolean canUse(Another_Decade user) {
        // 在有多个敌人或血量较低时优先使用
        int nearbyEnemies = getNearbyHostileCount(user);
        return (nearbyEnemies >= 2 || user.getHealth() <= user.getMaxHealth() * 0.3) &&
                user.getPersistentData().getInt("BlastPrisonCooldown") <= 0;
    }

    public static void use(Another_Decade user) {
        if (!canUse(user) || user.level().isClientSide()) {
            return;
        }

        // 播放蓄力音效
        user.playSound(SoundEvents.LARGE_AMETHYST_BUD_BREAK, 1.0F, 0.7F);

        // 双手凝聚紫黑色能量
        spawnEnergyChargeParticles(user);

        // 延迟生成连续爆炸
        user.getPersistentData().putInt("BlastPrisonStage", 1);
        user.getPersistentData().putInt("BlastPrisonTimer", 20); // 1秒后开始爆炸

        // 设置冷却
        user.getPersistentData().putInt("BlastPrisonCooldown", COOLDOWN);
    }

    public static void update(Another_Decade user) {
        // 更新冷却
        int cooldown = user.getPersistentData().getInt("BlastPrisonCooldown");
        if (cooldown > 0) {
            user.getPersistentData().putInt("BlastPrisonCooldown", cooldown - 1);
        }

        // 更新爆炸阶段
        if (user.getPersistentData().getInt("BlastPrisonStage") > 0) {
            int timer = user.getPersistentData().getInt("BlastPrisonTimer");
            int stage = user.getPersistentData().getInt("BlastPrisonStage");

            if (timer > 0) {
                user.getPersistentData().putInt("BlastPrisonTimer", timer - 1);
            } else if (stage <= EXPLOSION_COUNT) {
                // 生成爆炸
                createExplosion(user, stage);

                // 准备下一次爆炸
                user.getPersistentData().putInt("BlastPrisonStage", stage + 1);
                user.getPersistentData().putInt("BlastPrisonTimer", 10); // 0.5秒间隔
            } else {
                // 重置状态
                user.getPersistentData().putInt("BlastPrisonStage", 0);
            }
        }
    }

    private static void createExplosion(Another_Decade user, int stage) {
        if (user.level() instanceof ServerLevel serverLevel) {
            // 计算爆炸位置（向外扩散）
            double offset = (stage - 1) * 1.5D;
            double x = user.getX() + (user.getRandom().nextDouble() - 0.5) * offset;
            double y = user.getY() + 1.0D;
            double z = user.getZ() + (user.getRandom().nextDouble() - 0.5) * offset;

            // 生成爆炸粒子
            for (int i = 0; i < 20; i++) {
                double motionX = (user.getRandom().nextDouble() - 0.5) * 0.5D;
                double motionY = user.getRandom().nextDouble() * 0.5D;
                double motionZ = (user.getRandom().nextDouble() - 0.5) * 0.5D;

                serverLevel.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        x, y, z,
                        1,
                        motionX, motionY, motionZ,
                        0.1D
                );
            }

            // 播放爆炸音效
            user.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 0.8F + stage * 0.1F);

            // 对范围内敌人造成伤害
            List<LivingEntity> entities = user.level().getEntitiesOfClass(
                    LivingEntity.class,
                    user.getBoundingBox().inflate(EXPLOSION_RADIUS),
                    entity -> entity != user && !entity.isAlliedTo(user)
            );

            float damage = (float) user.getAttributeValue(
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes.CUSTOM_ATTACK_DAMAGE.get());
            damage *= DAMAGE_MULTIPLIER * (0.8F + stage * 0.1F); // 爆炸威力递增

            if (user.isEnraged()) {
                damage *= 1.1F;
            }

            for (LivingEntity entity : entities) {
                // 根据距离计算伤害衰减
                double distanceRatio = Math.min(1.0D, entity.distanceTo(user) / EXPLOSION_RADIUS);
                float finalDamage = damage * (1.0F - (float) distanceRatio * 0.5F);

                // 修复：使用正确的DamageSource创建方法
                entity.hurt(user.damageSources().mobAttack(user), finalDamage);

                // 击退效果
                Vec3 direction = entity.position().subtract(user.position()).normalize();
                entity.push(direction.x * 0.8D, 0.4D, direction.z * 0.8D);
            }
        }
    }

    private static void spawnEnergyChargeParticles(Another_Decade user) {
        if (user.level() instanceof ServerLevel serverLevel) {
            // 双手凝聚紫黑色能量效果
            for (int i = 0; i < 30; i++) {
                double offsetX = (user.getRandom().nextDouble() - 0.5) * 0.8D;
                double offsetY = user.getRandom().nextDouble() * 1.2D;
                double offsetZ = (user.getRandom().nextDouble() - 0.5) * 0.8D;

                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        user.getX() + offsetX,
                        user.getY() + offsetY,
                        user.getZ() + offsetZ,
                        1,
                        0, 0, 0,
                        0.05D
                );

                if (user.getRandom().nextBoolean()) {
                    serverLevel.sendParticles(
                            ParticleTypes.ENCHANTED_HIT,
                            user.getX() + offsetX,
                            user.getY() + offsetY,
                            user.getZ() + offsetZ,
                            1,
                            0, 0, 0,
                            0.05D
                    );
                }
            }
        }
    }

    private static int getNearbyHostileCount(Another_Decade user) {
        return user.level().getEntitiesOfClass(
                LivingEntity.class,
                user.getBoundingBox().inflate(10.0D),
                entity -> entity != user && !entity.isAlliedTo(user) &&
                        (entity instanceof net.minecraft.world.entity.player.Player ||
                                entity instanceof net.minecraft.world.entity.monster.Monster)
        ).size();
    }
}