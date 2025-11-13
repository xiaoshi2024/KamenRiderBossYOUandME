package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Dark Kiva技能的实现
 * 体现吸血鬼风格、黑暗力量和贵族气质
 * 技能特点是释放暗能量光束和吸血能力
 */
public class DarkKivaSkill {
    
    /**
     * 设置Dark Kiva的盔甲
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟Dark Kiva头盔）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.DARK_KIVA_HELMET.get()));
        // 设置胸甲（模拟Dark Kiva胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.DARK_KIVA_CHESTPLATE.get()));
        // 设置护腿（模拟Dark Kiva护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.DARK_KIVA_LEGGINGS.get()));
        
        // 设置主手武器（模拟Dark Kiva长剑）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.ZANVAT_SWORD.get()));
    }
    
    /**
     * 执行Dark Kiva的技能
     * 释放暗能量光束和吸血能力
     */
    public static void perform(EliteMonsterNpc entity) {
        // 只在服务器端处理
        if (entity.level().isClientSide) {
            return;
        }
        
        LivingEntity target = entity.getTarget();
        if (target == null) {
            return;
        }
        
        ServerLevel serverLevel = (ServerLevel) entity.level();
        
        // 生成黑暗能量蓄力效果
        createDarkEnergyChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 展现吸血鬼贵族气质的光环
        createVampireNobleAuraEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 计算从当前位置到目标的方向
        Vec3 direction = new Vec3(
            target.getX() - entity.getX(),
            target.getY() - entity.getY(),
            target.getZ() - entity.getZ()
        ).normalize();
        
        // 释放暗能量光束
        createDarkBeamEffect(serverLevel, entity, target, direction);
        
        // 创建一个任务，在延迟后对目标造成伤害
        entity.level().getServer().execute(() -> {
            if (entity.isAlive() && target.isAlive()) {
                // 计算最终伤害（基于基础攻击力，Dark Kiva技能有高伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double darkKivaDamage = baseDamage * 2.3; // 130%伤害加成
                
                // 对目标造成伤害
                boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) darkKivaDamage);
                
                // 中等击退效果
                if (damaged) {
                    target.knockback(1.8, direction.x, direction.z);
                    
                    // 吸血效果 - 恢复对目标造成伤害的30%
                    double healAmount = darkKivaDamage * 0.3;
                    entity.heal((float) healAmount);
                    
                    // 生成吸血粒子效果
                    createBloodSuckingEffect(serverLevel, target, entity, direction);
                }
                
                // 生成暗能量爆炸效果
                createDarkExplosionEffect(serverLevel, target.getX(), target.getY(), target.getZ());
            }
        });
    }
    
    /**
     * 创建黑暗能量蓄力效果粒子
     */
    private static void createDarkEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 2.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            
            // 暗紫色粒子模拟Dark Kiva能量
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.15,
                    0,
                    0.12
            );
            
            // 红色粒子增强吸血鬼风格
            level.sendParticles(
                    ParticleTypes.SMOKE,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.1,
                    0,
                    0.08
            );
        }
    }
    
    /**
     * 创建吸血鬼贵族气质的光环
     */
    private static void createVampireNobleAuraEffect(ServerLevel level, double x, double y, double z) {
        // 光环旋转效果
        for (int i = 0; i < 20; i++) {
            double angle = i * Math.PI * 2 / 20;
            double radius = 2.0;
            double auraX = x + Math.cos(angle) * radius;
            double auraY = y + 0.8;
            double auraZ = z + Math.sin(angle) * radius;
            
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    auraX,
                    auraY,
                    auraZ,
                    1,
                    Math.cos(angle) * 0.2,
                    0.02,
                    Math.sin(angle) * 0.2,
                    0.15
            );
            
            // 添加金色粒子点缀，体现贵族气质
            level.sendParticles(
                    ParticleTypes.GLOW,
                    auraX,
                    auraY,
                    auraZ,
                    1,
                    Math.cos(angle) * 0.15,
                    0.01,
                    Math.sin(angle) * 0.15,
                    0.1
            );
        }
    }
    
    /**
     * 创建暗能量光束效果
     */
    private static void createDarkBeamEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target, Vec3 direction) {
        double startX = entity.getX();
        double startY = entity.getY() + 1.2;
        double startZ = entity.getZ();
        
        // 光束传播效果
        for (int t = 1; t <= 30; t++) {
            double progress = t / 30.0;
            double beamX = startX + direction.x * 7.0 * progress;
            double beamY = startY + level.random.nextDouble() * 0.5;
            double beamZ = startZ + direction.z * 7.0 * progress;
            
            // 光束核心
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    beamX,
                    beamY,
                    beamZ,
                    1,
                    direction.x * 0.5,
                    0,
                    direction.z * 0.5,
                    0.05
            );
            
            // 光束周围效果
            for (int j = -2; j <= 2; j++) {
                double perpendicularX = -direction.z * j * 0.2;
                double perpendicularZ = direction.x * j * 0.2;
                
                level.sendParticles(
                        ParticleTypes.SMOKE,
                        beamX + perpendicularX,
                        beamY,
                        beamZ + perpendicularZ,
                        1,
                        direction.x * 0.3,
                        0,
                        direction.z * 0.3,
                        0.03
                );
            }
        }
    }
    
    /**
     * 创建吸血效果粒子
     */
    private static void createBloodSuckingEffect(ServerLevel level, LivingEntity target, EliteMonsterNpc entity, Vec3 direction) {
        double targetX = target.getX();
        double targetY = target.getY() + 1.0;
        double targetZ = target.getZ();
        
        Vec3 reversedDirection = new Vec3(
            entity.getX() - targetX,
            entity.getY() + 1.0 - targetY,
            entity.getZ() - targetZ
        ).normalize();
        
        // 血液从目标流向Dark Kiva的效果
        for (int t = 1; t <= 20; t++) {
            double progress = t / 20.0;
            double bloodX = targetX + reversedDirection.x * 7.0 * progress;
            double bloodY = targetY + reversedDirection.y * 7.0 * progress;
            double bloodZ = targetZ + reversedDirection.z * 7.0 * progress;
            
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    bloodX,
                    bloodY,
                    bloodZ,
                    1,
                    reversedDirection.x * 0.8,
                    reversedDirection.y * 0.8,
                    reversedDirection.z * 0.8,
                    0.05
            );
        }
    }
    
    /**
     * 创建暗能量爆炸效果粒子
     */
    private static void createDarkExplosionEffect(ServerLevel level, double x, double y, double z) {
        // 爆炸核心效果
        for (int i = 0; i < 40; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 2.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.5;
            
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    offsetX * 0.6,
                    offsetY * 0.6 + 0.2,
                    offsetZ * 0.6,
                    0.15
            );
        }
        
        // 爆炸外围效果
        level.sendParticles(
                ParticleTypes.SMOKE,
                x,
                y + 0.5,
                z,
                15,
                0,
                0,
                0,
                0.3
        );
        
        // Dark Kiva标志性光环残留效果
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2 / 12;
            double radius = 2.5;
            
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    x + Math.cos(angle) * radius,
                    y + 1.2,
                    z + Math.sin(angle) * radius,
                    1,
                    Math.cos(angle) * 0.4,
                    0.2,
                    Math.sin(angle) * 0.4,
                    0.08
            );
        }
    }
}