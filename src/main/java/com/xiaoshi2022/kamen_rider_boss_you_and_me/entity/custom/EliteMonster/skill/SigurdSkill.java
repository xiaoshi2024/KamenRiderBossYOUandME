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
 * 假面骑士Sigurd的技能实现
 * Sigurd是《假面骑士铠武》中的骑士，由Sid变身而成
 * 使用樱桃能量锁种，擅长远距离攻击和幻术
 * 技能特点是释放樱桃能量箭和创建幻象
 */
public class SigurdSkill {
    
    /**
     * 设置Sigurd的盔甲和对应的武器
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟Sigurd的头饰）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.SIGURD_HELMET.get()));
        // 设置胸甲（模拟Sigurd的胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.SIGURD_CHESTPLATE.get()));
        // 设置护腿（模拟Sigurd的护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.SIGURD_LEGGINGS.get()));

        // 即时替换主手武器为Sigurd的正确武器：樱桃能量箭矢枪
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.SONICARROW.get()));
    }
    
    /**
     * 执行Sigurd的技能
     * 释放樱桃能量箭，同时创建幻象干扰敌人
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
        
        // 生成樱桃能量蓄力效果
        createCherryEnergyChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 计算从当前位置到目标的方向
        Vec3 direction = new Vec3(
            target.getX() - entity.getX(),
            target.getY() - entity.getY(),
            target.getZ() - entity.getZ()
        ).normalize();
        
        // 发射樱桃能量箭视觉效果
        createCherryEnergyArrowEffect(serverLevel, entity, target, direction);
        
        // 创建幻象干扰敌人
        createIllusionEffect(serverLevel, entity, target);
        
        // 创建一个任务，在延迟后对目标造成伤害
        entity.level().getServer().execute(() -> {
            if (entity.isAlive() && target.isAlive()) {
                // 计算最终伤害（基于基础攻击力，Sigurd技能有穿透伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double sigurdDamage = baseDamage * 1.6; // 60%伤害加成
                
                // 对目标造成伤害
                boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) sigurdDamage);
                
                // 中等击退效果
                if (damaged) {
                    target.knockback(1.5, direction.x, direction.z);
                }
                
                // 生成命中爆炸效果
                createImpactExplosionEffect(serverLevel, target.getX(), target.getY(), target.getZ());
            }
        });
    }
    
    /**
     * 创建樱桃能量蓄力效果粒子
     */
    private static void createCherryEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 35; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetY = level.random.nextDouble() * 1.8;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.2;
            
            // 红色粒子模拟樱桃能量
            level.sendParticles(
                    ParticleTypes.FALLING_HONEY,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.1,
                    0,
                    0.08
            );
            
            // 白色粒子增强效果
            level.sendParticles(
                    ParticleTypes.WHITE_ASH,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.05,
                    0,
                    0.06
            );
        }
    }
    
    /**
     * 创建樱桃能量箭飞行效果粒子
     */
    private static void createCherryEnergyArrowEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target, Vec3 direction) {
        double startX = entity.getX();
        double startY = entity.getY() + 1.0;
        double startZ = entity.getZ();
        
        // 模拟能量箭飞行轨迹
        for (int t = 1; t <= 20; t++) {
            double progress = t / 20.0;
            double arrowX = startX + direction.x * 4.0 * progress;
            double arrowY = startY + direction.y * 4.0 * progress + (progress * (1 - progress)) * -0.5; // 轻微抛物线路径
            double arrowZ = startZ + direction.z * 4.0 * progress;
            
            // 在轨迹上生成红色粒子
            level.sendParticles(
                    ParticleTypes.FLAME,
                    arrowX,
                    arrowY,
                    arrowZ,
                    1,
                    direction.x * 0.5,
                    direction.y * 0.5,
                    direction.z * 0.5,
                    0.03
            );
            
            // 轨迹上的光效
            level.sendParticles(
                    ParticleTypes.GLOW,
                    arrowX,
                    arrowY,
                    arrowZ,
                    1,
                    direction.x * 0.3,
                    direction.y * 0.3,
                    direction.z * 0.3,
                    0.02
            );
        }
    }
    
    /**
     * 创建幻象干扰效果
     */
    private static void createIllusionEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target) {
        // 创建多个半透明的幻象
        for (int i = 0; i < 3; i++) {
            // 计算幻象位置（在目标周围随机）
            double illusionX = target.getX() + (level.random.nextDouble() - 0.5) * 6.0;
            double illusionY = target.getY();
            double illusionZ = target.getZ() + (level.random.nextDouble() - 0.5) * 6.0;
            
            // 幻象粒子效果
            for (int j = 0; j < 15; j++) {
                level.sendParticles(
                        ParticleTypes.CLOUD,
                        illusionX + (level.random.nextDouble() - 0.5) * 1.5,
                        illusionY + level.random.nextDouble() * 2.0,
                        illusionZ + (level.random.nextDouble() - 0.5) * 1.5,
                        1,
                        0,
                        0.05,
                        0,
                        0.08
                );
                
                level.sendParticles(
                        ParticleTypes.FIREWORK,
                        illusionX + (level.random.nextDouble() - 0.5) * 1.5,
                        illusionY + level.random.nextDouble() * 2.0,
                        illusionZ + (level.random.nextDouble() - 0.5) * 1.5,
                        1,
                        0,
                        0.05,
                        0,
                        0.04
                );
            }
        }
    }
    
    /**
     * 创建命中爆炸效果粒子
     */
    private static void createImpactExplosionEffect(ServerLevel level, double x, double y, double z) {
        // 爆炸核心效果
        for (int i = 0; i < 25; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
            
            level.sendParticles(
                    ParticleTypes.FISHING,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    offsetX * 0.5,
                    offsetY * 0.5,
                    offsetZ * 0.5,
                    0.1
            );
        }
        
        // 爆炸外围效果
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                x,
                y + 0.5,
                z,
                10,
                0,
                0,
                0,
                0.2
        );
        
        // 烟雾效果
        level.sendParticles(
                ParticleTypes.CLOUD,
                x,
                y + 0.5,
                z,
                8,
                0,
                0.1,
                0,
                0.15
        );
    }
}