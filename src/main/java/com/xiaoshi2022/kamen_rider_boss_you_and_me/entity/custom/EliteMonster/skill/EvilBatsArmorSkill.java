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
 * 邪恶蝙蝠装甲技能的实现
 * 结合了《假面骑士Evil》的元素和蝙蝠特性
 * 技能特点是召唤蝙蝠群、黑暗能量和吸血能力
 */
public class EvilBatsArmorSkill {
    
    /**
     * 设置邪恶蝙蝠装甲
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟蝙蝠头盔）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.EVIL_BATS_HELMET.get()));
        // 设置胸甲（模拟蝙蝠胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.EVIL_BATS_CHESTPLATE.get()));
        // 设置护腿（模拟蝙蝠护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.EVIL_BATS_LEGGINGS.get()));

        // 设置主手武器（模拟蝙蝠利爪）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.TWO_WEAPON_SWORD.get()));
    }
    
    /**
     * 执行邪恶蝙蝠装甲的技能
     * 召唤蝙蝠群，释放黑暗能量并对敌人造成伤害
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
        
        // 召唤蝙蝠群
        summonBatSwarmEffect(serverLevel, entity, target);
        
        // 计算从当前位置到目标的方向
        Vec3 direction = new Vec3(
            target.getX() - entity.getX(),
            target.getY() - entity.getY(),
            target.getZ() - entity.getZ()
        ).normalize();
        
        // 释放黑暗能量光束
        createDarkEnergyBeamEffect(serverLevel, entity, target, direction);
        
        // 创建一个任务，在延迟后对目标造成伤害并吸取生命值
        entity.level().getServer().execute(() -> {
            if (entity.isAlive() && target.isAlive()) {
                // 计算最终伤害（基于基础攻击力，邪恶蝙蝠技能有高伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double evilDamage = baseDamage * 2.2; // 120%伤害加成
                
                // 对目标造成伤害
                boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) evilDamage);
                
                // 强力击退效果
                if (damaged) {
                    target.knockback(1.9, direction.x, direction.z);
                    
                    // 吸血效果 - 吸取造成伤害的30%作为生命值恢复
                    double healAmount = evilDamage * 0.3;
                    entity.heal((float) healAmount);
                    
                    // 显示吸血效果
                    createVampiricEffect(serverLevel, entity, target);
                }
                
                // 生成黑暗爆炸效果
                createDarkExplosionEffect(serverLevel, target.getX(), target.getY(), target.getZ());
            }
        });
    }
    
    /**
     * 创建黑暗能量蓄力效果粒子
     */
    private static void createDarkEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 45; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.8;
            double offsetY = level.random.nextDouble() * 2.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.8;
            
            // 黑色粒子模拟黑暗能量
            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.1,
                    0,
                    0.08
            );
            
            // 紫色粒子增强效果
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.06,
                    0,
                    0.05
            );
        }
    }
    
    /**
     * 召唤蝙蝠群效果
     */
    private static void summonBatSwarmEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target) {
        // 蝙蝠从骑士身边飞出
        for (int i = 0; i < 15; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double radius = 2.0;
            double batX = entity.getX() + Math.cos(angle) * radius;
            double batY = entity.getY() + 0.5 + level.random.nextDouble() * 1.5;
            double batZ = entity.getZ() + Math.sin(angle) * radius;
            
            // 蝙蝠飞往目标
            Vec3 direction = new Vec3(
                target.getX() - batX,
                target.getY() - batY,
                target.getZ() - batZ
            ).normalize();
            
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    batX,
                    batY,
                    batZ,
                    1,
                    direction.x * 0.8,
                    direction.y * 0.8,
                    direction.z * 0.8,
                    0.05
            );
            
            // 蝙蝠轨迹上的黑暗能量
            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    batX,
                    batY,
                    batZ,
                    1,
                    direction.x * 0.5,
                    direction.y * 0.5,
                    direction.z * 0.5,
                    0.03
            );
        }
    }
    
    /**
     * 创建黑暗能量光束效果
     */
    private static void createDarkEnergyBeamEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target, Vec3 direction) {
        double startX = entity.getX();
        double startY = entity.getY() + 1.0;
        double startZ = entity.getZ();
        
        // 光束飞行轨迹
        for (int t = 1; t <= 20; t++) {
            double progress = t / 20.0;
            double beamX = startX + direction.x * 5.0 * progress;
            double beamY = startY + direction.y * 5.0 * progress;
            double beamZ = startZ + direction.z * 5.0 * progress;
            
            // 光束核心
            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    beamX,
                    beamY,
                    beamZ,
                    1,
                    direction.x * 0.4,
                    direction.y * 0.4,
                    direction.z * 0.4,
                    0.04
            );
            
            // 光束外围
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    beamX,
                    beamY,
                    beamZ,
                    1,
                    direction.x * 0.3 + (level.random.nextDouble() - 0.5) * 0.15,
                    direction.y * 0.3 + (level.random.nextDouble() - 0.5) * 0.15,
                    direction.z * 0.3 + (level.random.nextDouble() - 0.5) * 0.15,
                    0.03
            );
        }
    }
    
    /**
     * 创建吸血效果粒子
     */
    private static void createVampiricEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target) {
        // 生命能量从目标流向骑士
        for (int i = 0; i < 15; i++) {
            double progress = i / 15.0;
            double x = target.getX() + (entity.getX() - target.getX()) * progress + (level.random.nextDouble() - 0.5) * 0.5;
            double y = target.getY() + 0.5 + (entity.getY() + 0.5 - (target.getY() + 0.5)) * progress + (level.random.nextDouble() - 0.5) * 0.5;
            double z = target.getZ() + (entity.getZ() - target.getZ()) * progress + (level.random.nextDouble() - 0.5) * 0.5;
            
            level.sendParticles(
                    ParticleTypes.RAIN,
                    x,
                    y,
                    z,
                    1,
                    (entity.getX() - target.getX()) * 0.3,
                    (entity.getY() + 0.5 - (target.getY() + 0.5)) * 0.3,
                    (entity.getZ() - target.getZ()) * 0.3,
                    0.05
            );
        }
        
        // 骑士吸收生命能量的效果
        for (int i = 0; i < 8; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.2;
            
            level.sendParticles(
                    ParticleTypes.HEART,
                    entity.getX() + offsetX,
                    entity.getY() + offsetY,
                    entity.getZ() + offsetZ,
                    1,
                    -offsetX * 0.3,
                    -offsetY * 0.3 + 0.1,
                    -offsetZ * 0.3,
                    0.08
            );
        }
    }
    
    /**
     * 创建黑暗爆炸效果粒子
     */
    private static void createDarkExplosionEffect(ServerLevel level, double x, double y, double z) {
        // 爆炸核心效果
        for (int i = 0; i < 35; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            
            level.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    offsetX * 0.6,
                    offsetY * 0.6 + 0.15,
                    offsetZ * 0.6,
                    0.12
            );
        }
        
        // 爆炸外围效果
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                x,
                y + 0.5,
                z,
                12,
                0,
                0,
                0,
                0.25
        );
        
        // 黑暗光环效果
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI * 2 / 10;
            double radius = 2.0;
            
            level.sendParticles(
                    ParticleTypes.INSTANT_EFFECT,
                    x + Math.cos(angle) * radius,
                    y + 1.0,
                    z + Math.sin(angle) * radius,
                    1,
                    Math.cos(angle) * 0.3,
                    0.2,
                    Math.sin(angle) * 0.3,
                    0.06
            );
        }
    }
}