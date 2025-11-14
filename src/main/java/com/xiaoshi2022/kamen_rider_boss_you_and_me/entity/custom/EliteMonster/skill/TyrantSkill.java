package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 暴君技能的实现
 * 体现强大统治力和破坏力的终极技能
 * 技能特点是释放强大能量波动和震慑敌人
 */
public class TyrantSkill {
    
    /**
     * 设置暴君的盔甲
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟暴君头盔）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.TYRANT_HELMET.get()));
        // 设置胸甲（模拟暴君胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.TYRANT_CHESTPLATE.get()));
        // 设置护腿（模拟暴君护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.TYRANT_LEGGINGS.get()));

        // 设置主手武器（模拟暴君权杖）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.SONICARROW.get()));
    }
    
    /**
     * 执行暴君的技能
     * 释放强大能量波动，震慑敌人并造成高额伤害
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
        
        // 生成暴君能量蓄力效果
        createTyrantEnergyChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 展现统治力光环
        createDominanceAuraEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 计算从当前位置到目标的方向
        Vec3 direction = new Vec3(
            target.getX() - entity.getX(),
            target.getY() - entity.getY(),
            target.getZ() - entity.getZ()
        ).normalize();
        
        // 释放能量冲击波
        createEnergyWaveEffect(serverLevel, entity, target, direction);
        
        // 创建一个任务，在延迟后对目标造成伤害
        entity.level().getServer().execute(() -> {
            if (entity.isAlive() && target.isAlive()) {
                // 计算最终伤害（基于基础攻击力，暴君技能有极高伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double tyrantDamage = baseDamage * 2.8; // 180%伤害加成
                
                // 对目标造成伤害
                boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) tyrantDamage);
                
                // 极强击退效果
                if (damaged) {
                    target.knockback(2.5, direction.x, direction.z);
                }
                
                // 生成毁灭性爆炸效果
                createDestructiveExplosionEffect(serverLevel, target.getX(), target.getY(), target.getZ());
            }
        });
    }
    
    /**
     * 创建暴君能量蓄力效果粒子
     */
    private static void createTyrantEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 50; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 2.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            
            // 金色粒子模拟暴君能量
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.15,
                    0,
                    0.12
            );
            
            // 紫色粒子增强效果
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
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
     * 创建统治力光环效果
     */
    private static void createDominanceAuraEffect(ServerLevel level, double x, double y, double z) {
        // 光环旋转效果
        for (int i = 0; i < 24; i++) {
            double angle = i * Math.PI * 2 / 24;
            double radius = 2.5;
            double auraX = x + Math.cos(angle) * radius;
            double auraY = y + 0.8;
            double auraZ = z + Math.sin(angle) * radius;
            
            level.sendParticles(
                    ParticleTypes.GLOW,
                    auraX,
                    auraY,
                    auraZ,
                    1,
                    Math.cos(angle) * 0.2,
                    0.02,
                    Math.sin(angle) * 0.2,
                    0.15
            );
            
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
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
        
        // 地面震动效果
        for (int i = 0; i < 15; i++) {
            double groundX = x + (level.random.nextDouble() - 0.5) * 3.0;
            double groundY = y - 0.1;
            double groundZ = z + (level.random.nextDouble() - 0.5) * 3.0;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    groundX,
                    groundY,
                    groundZ,
                    1,
                    0,
                    0.05,
                    0,
                    0.12
            );
        }
    }
    
    /**
     * 创建能量冲击波效果
     */
    private static void createEnergyWaveEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target, Vec3 direction) {
        double startX = entity.getX();
        double startY = entity.getY() + 0.5;
        double startZ = entity.getZ();
        
        // 冲击波传播效果
        for (int t = 1; t <= 30; t++) {
            double progress = t / 30.0;
            double waveX = startX + direction.x * 7.0 * progress;
            double waveY = startY + level.random.nextDouble() * 0.5;
            double waveZ = startZ + direction.z * 7.0 * progress;
            
            // 冲击波核心
            level.sendParticles(
                    ParticleTypes.GLOW,
                    waveX,
                    waveY,
                    waveZ,
                    1,
                    direction.x * 0.5,
                    0,
                    direction.z * 0.5,
                    0.05
            );
            
            // 冲击波扩散效果
            for (int j = -3; j <= 3; j++) {
                double perpendicularX = -direction.z * j * 0.2;
                double perpendicularZ = direction.x * j * 0.2;
                
                level.sendParticles(
                        ParticleTypes.INSTANT_EFFECT,
                        waveX + perpendicularX,
                        waveY,
                        waveZ + perpendicularZ,
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
     * 创建毁灭性爆炸效果粒子
     */
    private static void createDestructiveExplosionEffect(ServerLevel level, double x, double y, double z) {
        // 爆炸核心效果
        for (int i = 0; i < 50; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 3.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 3.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 3.0;
            
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    offsetX * 0.7,
                    offsetY * 0.7 + 0.2,
                    offsetZ * 0.7,
                    0.15
            );
        }
        
        // 爆炸外围效果
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                x,
                y + 0.5,
                z,
                20,
                0,
                0,
                0,
                0.3
        );
        
        // 地面破坏效果
        for (int i = 0; i < 15; i++) {
            double groundX = x + (level.random.nextDouble() - 0.5) * 4.0;
            double groundY = y - 0.1;
            double groundZ = z + (level.random.nextDouble() - 0.5) * 4.0;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    groundX,
                    groundY,
                    groundZ,
                    1,
                    0,
                    0.1,
                    0,
                    0.15
            );
        }
        
        // 暴君光环残留效果
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI * 2 / 16;
            double radius = 3.0;
            
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x + Math.cos(angle) * radius,
                    y + 1.5,
                    z + Math.sin(angle) * radius,
                    1,
                    Math.cos(angle) * 0.4,
                    0.25,
                    Math.sin(angle) * 0.4,
                    0.08
            );
        }
    }
}