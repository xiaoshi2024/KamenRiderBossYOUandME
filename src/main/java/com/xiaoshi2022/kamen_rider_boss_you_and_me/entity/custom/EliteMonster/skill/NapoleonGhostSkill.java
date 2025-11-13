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
 * Napoleon Ghost技能的实现
 * 体现拿破仑军事风格、领导力和历史感
 * 技能特点是军事策略、指挥和历史共鸣
 */
public class NapoleonGhostSkill {
    
    /**
     * 设置Napoleon Ghost的盔甲
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟拿破仑军帽风格）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.NAPOLEON_GHOST_HELMET.get()));
        // 设置胸甲（模拟拿破仑军装风格）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.NAPOLEON_GHOST_CHESTPLATE.get()));
        // 设置护腿（模拟拿破仑军裤风格）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.NAPOLEON_GHOST_LEGGINGS.get()));

        // 设置主手武器（模拟拿破仑指挥剑）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.GANGUNSABER.get()));
    }
    
    /**
     * 执行Napoleon Ghost的技能
     * 释放军事策略、指挥和历史共鸣能量
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
        
        // 生成军事指挥能量蓄力效果
        createCommandEnergyChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 展现军事阵型效果
        createMilitaryFormationEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 计算从当前位置到目标的方向
        Vec3 direction = new Vec3(
            target.getX() - entity.getX(),
            target.getY() - entity.getY(),
            target.getZ() - entity.getZ()
        ).normalize();
        
        // 释放指挥能量冲击
        createCommandEnergyWaveEffect(serverLevel, entity, target, direction);
        
        // 创建一个任务，在延迟后对目标造成伤害
        entity.level().getServer().execute(() -> {
            if (entity.isAlive() && target.isAlive()) {
                // 计算最终伤害（基于基础攻击力，Napoleon Ghost技能有高伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double napoleonDamage = baseDamage * 2.4; // 140%伤害加成
                
                // 对目标造成伤害
                boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) napoleonDamage);
                
                // 中等击退效果
                if (damaged) {
                    target.knockback(1.6, direction.x, direction.z);
                    
                    // 生成军事命令标记效果
                    createCommandMarkEffect(serverLevel, target);
                }
                
                // 生成军事胜利仪式效果
                createVictoryCeremonyEffect(serverLevel, target.getX(), target.getY(), target.getZ());
            }
        });
    }
    
    /**
     * 创建军事指挥能量蓄力效果粒子
     */
    private static void createCommandEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 35; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.8;
            double offsetY = level.random.nextDouble() * 2.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.8;
            
            // 金色粒子模拟拿破仑军事指挥能量
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.12,
                    0,
                    0.1
            );
            
            // 军事红色粒子增强效果
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.08,
                    0,
                    0.07
            );
        }
    }
    
    /**
     * 创建军事阵型效果
     */
    private static void createMilitaryFormationEffect(ServerLevel level, double x, double y, double z) {
        // 模拟军事方阵
        int formationSize = 3;
        double spacing = 1.2;
        
        for (int dx = -formationSize; dx <= formationSize; dx++) {
            for (int dz = -formationSize; dz <= formationSize; dz++) {
                if (Math.abs(dx) <= formationSize && Math.abs(dz) <= formationSize) {
                    double posX = x + dx * spacing;
                    double posY = y - 0.1;
                    double posZ = z + dz * spacing;
                    
                    // 金色粒子代表军人士兵位置
                    level.sendParticles(
                            ParticleTypes.GLOW,
                            posX,
                            posY,
                            posZ,
                            1,
                            0,
                            0.05,
                            0,
                            0.08
                    );
                }
            }
        }
        
        // 指挥旗帜效果
        for (int i = 0; i < 15; i++) {
            double angle = i * Math.PI * 2 / 15;
            double radius = 3.0;
            
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x + Math.cos(angle) * radius,
                    y + 1.8,
                    z + Math.sin(angle) * radius,
                    1,
                    Math.cos(angle) * 0.15,
                    0.1,
                    Math.sin(angle) * 0.15,
                    0.1
            );
        }
    }
    
    /**
     * 创建指挥能量冲击波效果
     */
    private static void createCommandEnergyWaveEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target, Vec3 direction) {
        double startX = entity.getX();
        double startY = entity.getY() + 1.0;
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
            
            // 冲击波周围效果
            for (int j = -2; j <= 2; j++) {
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
     * 创建军事命令标记效果粒子
     */
    private static void createCommandMarkEffect(ServerLevel level, LivingEntity target) {
        double targetX = target.getX();
        double targetY = target.getY() - 0.1;
        double targetZ = target.getZ();
        
        // 目标脚下的军事指挥标记
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2 / 12;
            double radius = 1.0;
            
            level.sendParticles(
                    ParticleTypes.GLOW,
                    targetX + Math.cos(angle) * radius,
                    targetY,
                    targetZ + Math.sin(angle) * radius,
                    1,
                    Math.cos(angle) * 0.2,
                    0.05,
                    Math.sin(angle) * 0.2,
                    0.08
            );
        }
        
        // 标记中心点
        level.sendParticles(
                ParticleTypes.GLOW,
                targetX,
                targetY,
                targetZ,
                5,
                0,
                0.1,
                0,
                0.05
        );
    }
    
    /**
     * 创建军事胜利仪式效果粒子
     */
    private static void createVictoryCeremonyEffect(ServerLevel level, double x, double y, double z) {
        // 胜利旗帜效果
        for (int i = 0; i < 40; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.5;
            double offsetY = level.random.nextDouble() * 3.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.5;
            
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    offsetX * 0.3,
                    0.3 + level.random.nextDouble() * 0.2,
                    offsetZ * 0.3,
                    0.12
            );
        }
        
        // 军事胜利礼炮效果
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            double radius = 2.0;
            double cannonX = x + Math.cos(angle) * radius;
            double cannonY = y + 2.0;
            double cannonZ = z + Math.sin(angle) * radius;
            
            for (int j = 0; j < 5; j++) {
                level.sendParticles(
                        ParticleTypes.FLAME,
                        cannonX,
                        cannonY,
                        cannonZ,
                        1,
                        Math.cos(angle + Math.PI/2) * 0.5,
                        0.3 + j * 0.1,
                        Math.sin(angle + Math.PI/2) * 0.5,
                        0.08
                );
            }
        }
        
        // 历史共鸣效果
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2 / 12;
            double radius = 3.5;
            
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x + Math.cos(angle) * radius,
                    y + 2.2,
                    z + Math.sin(angle) * radius,
                    1,
                    Math.cos(angle) * 0.2,
                    0.25,
                    Math.sin(angle) * 0.2,
                    0.1
            );
        }
    }
}