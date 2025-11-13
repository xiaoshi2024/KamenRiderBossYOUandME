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

/**
 * 假面骑士Baron柠檬能量形态的技能实现
 * Baron是《假面骑士铠武》中的主要骑士之一，柠檬能量形态是其强化形态
 * 拥有强大的柠檬能量攻击能力和高机动性
 */
public class LemonBaronSkill {
    
    /**
     * 设置Baron柠檬能量形态的盔甲
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟Baron柠檬形态的头饰）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.BARON_LEMON_CHESTPLATE.get()));
        // 设置胸甲（模拟Baron柠檬形态的胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.BARON_LEMON_CHESTPLATE.get()));
        // 设置护腿（模拟Baron柠檬形态的护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.BARON_LEMON_LEGGINGS.get()));

        // 设置主手武器（模拟Baron柠檬形态的武器）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.SONICARROW.get()));
    }
    
    /**
     * 执行Baron柠檬能量形态的技能
     * 使用柠檬能量对敌人发起强力攻击
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
        
        // 生成柠檬能量蓄力效果
        createLemonEnergyChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 计算从当前位置到目标的方向
        double directionX = target.getX() - entity.getX();
        double directionZ = target.getZ() - entity.getZ();
        double distance = Math.sqrt(directionX * directionX + directionZ * directionZ);
        
        if (distance > 0) {
            // 标准化方向向量
            directionX /= distance;
            directionZ /= distance;
            
            // 高机动性冲刺
            entity.push(directionX * 2.0, 0.8, directionZ * 2.0);
            
            // 创建一个任务，在延迟后对目标造成伤害
            double finalDirectionZ = directionZ;
            double finalDirectionX = directionX;
            entity.level().getServer().execute(() -> {
                if (entity.isAlive() && target.isAlive()) {
                    // 计算最终伤害（基于基础攻击力，柠檬能量形态有伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double lemonDamage = baseDamage * 2.1; // 110%伤害加成
                    
                    // 对目标造成伤害
                    boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) lemonDamage);
                    
                    // 强力击退效果
                    if (damaged) {
                        target.knockback(1.8, finalDirectionX, finalDirectionZ);
                    }
                    
                    // 生成柠檬能量爆炸效果
                    createLemonExplosionEffect(serverLevel, target.getX(), target.getY(), target.getZ());
                }
            });
        }
    }
    
    /**
     * 创建柠檬能量蓄力效果粒子
     */
    private static void createLemonEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 35; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * 1.8;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
            
            // 黄色粒子模拟柠檬能量
            level.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.15,
                    0,
                    0.1
            );
            
            // 白色粒子增强效果
            level.sendParticles(
                    ParticleTypes.WHITE_ASH,
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
     * 创建柠檬能量爆炸效果粒子
     */
    private static void createLemonExplosionEffect(ServerLevel level, double x, double y, double z) {
        // 核心爆炸效果
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            
            level.sendParticles(
                    ParticleTypes.FLAME,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    level.random.nextDouble() - 0.5,
                    level.random.nextDouble() * 0.4,
                    level.random.nextDouble() - 0.5,
                    0.2
            );
        }
        
        // 辐射状扩散效果
        for (int i = 0; i < 48; i += 3) {
            double angle = Math.toRadians(i);
            double spreadX = Math.cos(angle) * 1.5;
            double spreadZ = Math.sin(angle) * 1.5;
            
            level.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    x,
                    y + 0.7,
                    z,
                    1,
                    spreadX,
                    0.2,
                    spreadZ,
                    0.15
            );
        }
        
        // 中央强光效果
        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                x,
                y + 0.5,
                z,
                10,
                0,
                0,
                0,
                0.3
        );
    }
}