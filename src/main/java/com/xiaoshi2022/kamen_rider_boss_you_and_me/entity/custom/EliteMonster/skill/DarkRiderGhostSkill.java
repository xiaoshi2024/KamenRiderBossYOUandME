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
 * Dark Rider Ghost技能的实现
 * 体现幽灵特性、黑暗力量和瞬移能力
 * 技能特点是幽灵能量、瞬移和幻象
 */
public class DarkRiderGhostSkill {
    
    /**
     * 设置Dark Rider Ghost的盔甲
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟Dark Rider Ghost头盔）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.DARK_RIDER_HELMET.get()));
        // 设置胸甲（模拟Dark Rider Ghost胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.DARK_RIDER_CHESTPLATE.get()));
        // 设置护腿（模拟Dark Rider Ghost护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.DARK_RIDER_LEGGINGS.get()));

        // 设置主手武器（模拟Dark Rider Ghost武器）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.GANGUNSABER.get()));
    }
    
    /**
     * 执行Dark Rider Ghost的技能
     * 释放幽灵能量、瞬移和幻象
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
        
        // 生成幽灵能量蓄力效果
        createGhostEnergyChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 展现幽灵特性的模糊效果
        createGhostBlurEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 模拟瞬间移动效果
        createTeleportEffect(serverLevel, entity, target);
        
        // 计算从当前位置到目标的方向
        Vec3 direction = new Vec3(
            target.getX() - entity.getX(),
            target.getY() - entity.getY(),
            target.getZ() - entity.getZ()
        ).normalize();
        
        // 释放幽灵能量冲击波
        createGhostWaveEffect(serverLevel, entity, target, direction);
        
        // 创建一个任务，在延迟后对目标造成伤害
        entity.level().getServer().execute(() -> {
            if (entity.isAlive() && target.isAlive()) {
                // 计算最终伤害（基于基础攻击力，Dark Rider Ghost技能有高伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double ghostDamage = baseDamage * 2.1; // 110%伤害加成
                
                // 对目标造成伤害
                boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) ghostDamage);
                
                // 中等击退效果
                if (damaged) {
                    target.knockback(1.5, direction.x, direction.z);
                    
                    // 生成灵魂收割效果
                    createSoulReapingEffect(serverLevel, target, entity);
                }
                
                // 生成幽灵爆炸效果
                createGhostExplosionEffect(serverLevel, target.getX(), target.getY(), target.getZ());
            }
        });
    }
    
    /**
     * 创建幽灵能量蓄力效果粒子
     */
    private static void createGhostEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 35; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.8;
            double offsetY = level.random.nextDouble() * 2.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.8;
            
            // 青灰色粒子模拟幽灵能量
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.12,
                    0,
                    0.1
            );
            
            // 紫色粒子增强幽灵神秘感
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
     * 创建幽灵模糊效果
     */
    private static void createGhostBlurEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 25; i++) {
            double blurX = x + (level.random.nextDouble() - 0.5) * 3.0;
            double blurY = y + 0.5 + level.random.nextDouble() * 1.5;
            double blurZ = z + (level.random.nextDouble() - 0.5) * 3.0;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    blurX,
                    blurY,
                    blurZ,
                    1,
                    0,
                    0.05,
                    0,
                    0.15
            );
        }
    }
    
    /**
     * 创建瞬间移动效果
     */
    private static void createTeleportEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target) {
        // 记录原始位置
        double originalX = entity.getX();
        double originalY = entity.getY();
        double originalZ = entity.getZ();
        
        // 创建消失粒子
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    originalX + offsetX,
                    originalY + offsetY,
                    originalZ + offsetZ,
                    1,
                    0,
                    -0.2,
                    0,
                    0.1
            );
        }
        
        // 计算瞬移目标位置（略偏向目标方向）
        Vec3 direction = new Vec3(
            target.getX() - originalX,
            0,
            target.getZ() - originalZ
        ).normalize();
        
        double teleportX = originalX + direction.x * 3.0;
        double teleportY = originalY;
        double teleportZ = originalZ + direction.z * 3.0;
        
        // 创建出现粒子（这里模拟瞬间移动的视觉效果，不实际改变实体位置）
        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    teleportX + offsetX,
                    teleportY + offsetY,
                    teleportZ + offsetZ,
                    1,
                    0,
                    0.2,
                    0,
                    0.1
            );
        }
    }
    
    /**
     * 创建幽灵能量冲击波效果
     */
    private static void createGhostWaveEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target, Vec3 direction) {
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
                    ParticleTypes.CLOUD,
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
     * 创建灵魂收割效果粒子
     */
    private static void createSoulReapingEffect(ServerLevel level, LivingEntity target, EliteMonsterNpc entity) {
        double targetX = target.getX();
        double targetY = target.getY() + 1.0;
        double targetZ = target.getZ();
        
        Vec3 direction = new Vec3(
            entity.getX() - targetX,
            entity.getY() + 1.0 - targetY,
            entity.getZ() - targetZ
        ).normalize();
        
        // 灵魂从目标流向Dark Rider Ghost的效果
        for (int t = 1; t <= 20; t++) {
            double progress = t / 20.0;
            double soulX = targetX + direction.x * 7.0 * progress;
            double soulY = targetY + direction.y * 7.0 * progress;
            double soulZ = targetZ + direction.z * 7.0 * progress;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    soulX,
                    soulY,
                    soulZ,
                    1,
                    direction.x * 0.8,
                    direction.y * 0.8,
                    direction.z * 0.8,
                    0.05
            );
        }
    }
    
    /**
     * 创建幽灵爆炸效果粒子
     */
    private static void createGhostExplosionEffect(ServerLevel level, double x, double y, double z) {
        // 爆炸核心效果
        for (int i = 0; i < 35; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.2;
            double offsetY = (level.random.nextDouble() - 0.5) * 2.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.2;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    offsetX * 0.5,
                    offsetY * 0.5 + 0.2,
                    offsetZ * 0.5,
                    0.15
            );
        }
        
        // 爆炸外围效果
        level.sendParticles(
                ParticleTypes.INSTANT_EFFECT,
                x,
                y + 0.5,
                z,
                15,
                0,
                0,
                0,
                0.3
        );
        
        // 幽灵残像效果
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI * 2 / 10;
            double radius = 2.2;
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    x + Math.cos(angle) * radius,
                    y + 1.0,
                    z + Math.sin(angle) * radius,
                    1,
                    Math.cos(angle) * 0.3,
                    0.15,
                    Math.sin(angle) * 0.3,
                    0.08
            );
        }
    }
}