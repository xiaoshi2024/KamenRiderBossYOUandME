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
 * 假面骑士Marika的技能实现
 * Marika是《假面骑士铠武》中的女性骑士，由凑耀子变身而成
 * 使用蜜桃能量锁种，擅长敏捷的战斗风格和迷惑敌人的技能
 * 技能特点是释放蜜桃能量光环和高速移动
 */
public class MarikaSkill {
    
    /**
     * 设置Marika的盔甲和对应的武器
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟Marika的头饰）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.MARIKA_HELMET.get()));
        // 设置胸甲（模拟Marika的胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.MARIKA_CHESTPLATE.get()));
        // 设置护腿（模拟Marika的护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.MARIKA_LEGGINGS.get()));

        // 即时替换主手武器为Marika的武器：音速弓（根据用户要求保留音速弓）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.SONICARROW.get()));
    }
    
    /**
     * 执行Marika的技能
     * 释放蜜桃能量光环，高速移动并对周围敌人造成伤害
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
        
        // 生成蜜桃能量蓄力效果
        createPeachEnergyChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 计算从当前位置到目标的方向
        Vec3 direction = new Vec3(
            target.getX() - entity.getX(),
            target.getY() - entity.getY(),
            target.getZ() - entity.getZ()
        ).normalize();
        
        // 高速移动效果
        createHighSpeedMoveEffect(serverLevel, entity, target, direction);
        
        // 生成蜜桃能量光环
        createPeachEnergyAuraEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 创建一个任务，在延迟后对目标造成伤害
        entity.level().getServer().execute(() -> {
            if (entity.isAlive() && target.isAlive()) {
                // 计算最终伤害（基于基础攻击力，Marika技能有中等伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double marikaDamage = baseDamage * 1.5; // 50%伤害加成
                
                // 对目标造成伤害
                boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) marikaDamage);
                
                // 高速攻击击退效果
                if (damaged) {
                    target.knockback(1.8, direction.x, direction.z);
                }
                
                // 生成命中效果
                createImpactEffect(serverLevel, target.getX(), target.getY(), target.getZ());
            }
        });
    }
    
    /**
     * 创建蜜桃能量蓄力效果粒子
     */
    private static void createPeachEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.0;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.0;
            
            // 粉色粒子模拟蜜桃能量
            level.sendParticles(
                    ParticleTypes.HEART,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.08,
                    0,
                    0.06
            );
            
            // 金色粒子增强效果
            level.sendParticles(
                    ParticleTypes.GLOW,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.06,
                    0,
                    0.04
            );
        }
    }
    
    /**
     * 创建高速移动效果粒子
     */
    private static void createHighSpeedMoveEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target, Vec3 direction) {
        double startX = entity.getX();
        double startY = entity.getY() + 0.5;
        double startZ = entity.getZ();
        
        // 模拟高速移动轨迹
        for (int t = 1; t <= 15; t++) {
            double progress = t / 15.0;
            double moveX = startX + direction.x * 5.0 * progress;
            double moveY = startY + direction.y * 5.0 * progress;
            double moveZ = startZ + direction.z * 5.0 * progress;
            
            // 轨迹上的粉色粒子
            level.sendParticles(
                    ParticleTypes.HEART,
                    moveX,
                    moveY,
                    moveZ,
                    1,
                    direction.x * 0.3,
                    direction.y * 0.3,
                    direction.z * 0.3,
                    0.02
            );
            
            // 轨迹上的光效
            level.sendParticles(
                    ParticleTypes.GLOW,
                    moveX,
                    moveY,
                    moveZ,
                    1,
                    direction.x * 0.2,
                    direction.y * 0.2,
                    direction.z * 0.2,
                    0.01
            );
        }
        
        // 高速移动带起的烟雾
        for (int i = 0; i < 10; i++) {
            double moveX = startX + direction.x * level.random.nextDouble() * 4.0;
            double moveY = startY + level.random.nextDouble() * 0.5;
            double moveZ = startZ + direction.z * level.random.nextDouble() * 4.0;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    moveX,
                    moveY,
                    moveZ,
                    1,
                    direction.x * -0.3,
                    0.1,
                    direction.z * -0.3,
                    0.1
            );
        }
    }
    
    /**
     * 创建蜜桃能量光环效果
     */
    private static void createPeachEnergyAuraEffect(ServerLevel level, double x, double y, double z) {
        // 光环核心效果
        for (int i = 0; i < 20; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double radius = 1.0 + level.random.nextDouble() * 0.5;
            double auraX = x + Math.cos(angle) * radius;
            double auraY = y + 0.5 + level.random.nextDouble() * 1.0;
            double auraZ = z + Math.sin(angle) * radius;
            
            level.sendParticles(
                    ParticleTypes.HEART,
                    auraX,
                    auraY,
                    auraZ,
                    1,
                    -Math.cos(angle) * 0.3,
                    level.random.nextDouble() * 0.1 - 0.05,
                    -Math.sin(angle) * 0.3,
                    0.05
            );
        }
    }
    
    /**
     * 创建命中效果粒子
     */
    private static void createImpactEffect(ServerLevel level, double x, double y, double z) {
        // 核心命中效果
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetY = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.2;
            
            level.sendParticles(
                    ParticleTypes.HEART,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    offsetX * 0.4,
                    offsetY * 0.4 + 0.1,
                    offsetZ * 0.4,
                    0.08
            );
        }
        
        // 光效爆发
        level.sendParticles(
                ParticleTypes.GLOW,
                x,
                y + 0.5,
                z,
                15,
                0,
                0,
                0,
                0.15
        );
        
        // 命中周围的粉色光晕
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            double radius = 1.5;
            
            level.sendParticles(
                    ParticleTypes.HEART,
                    x + Math.cos(angle) * radius,
                    y + 1.0,
                    z + Math.sin(angle) * radius,
                    1,
                    Math.cos(angle) * 0.2,
                    0.1,
                    Math.sin(angle) * 0.2,
                    0.04
            );
        }
    }
}