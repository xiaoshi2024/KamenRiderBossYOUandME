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
 * 黑暗橙子是假面骑士铠武被金光暂时夺舍时的系统
 * 典型的力量是邪气外漏
 * 发射致盲黑暗气场给予伤害
 */
public class DarkOrangeAngelsSkill {
    
    /**
     * 设置黑暗橙子的盔甲
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟黑暗橙子头盔）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.DARK_ORANGELS_HELMET.get()));
        // 设置胸甲（模拟黑暗橙子胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.DARK_ORANGELS_CHESTPLATE.get()));
        // 设置护腿（模拟黑暗橙子护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.DARK_ORANGELS_LEGGINGS.get()));

        // 设置主手武器（模拟黑暗橙子武器）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.SONICARROW.get()));
    }
    
    /**
     * 执行黑暗橙子的技能
     * 发射致盲黑暗气场给予伤害
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
        
        // 生成邪气外漏效果
        createCorruptedEnergyLeakEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 计算从当前位置到目标的方向
        Vec3 direction = new Vec3(
            target.getX() - entity.getX(),
            target.getY() - entity.getY(),
            target.getZ() - entity.getZ()
        ).normalize();
        
        // 释放致盲黑暗气场
        createBlindingDarkAuraEffect(serverLevel, entity, target, direction);
        
        // 创建一个任务，在延迟后对目标造成伤害
        entity.level().getServer().execute(() -> {
            if (entity.isAlive() && target.isAlive()) {
                // 计算最终伤害（基于基础攻击力，黑暗橙子技能有高伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double darkOrangeDamage = baseDamage * 1.8; // 80%伤害加成
                
                // 对目标造成伤害
                boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) darkOrangeDamage);
                
                // 强力击退效果
                if (damaged) {
                    target.knockback(2.5, direction.x, direction.z);
                    
                    // 致盲效果（这里简化处理，实际应该使用Minecraft的效果系统）
                    blindTarget(target);
                }
                
                // 生成黑暗爆炸效果
                createDarkExplosionEffect(serverLevel, target.getX(), target.getY(), target.getZ());
            }
        });
    }
    
    /**
     * 创建邪气外漏效果粒子
     */
    private static void createCorruptedEnergyLeakEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 2.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            
            // 暗紫色粒子模拟邪气
            level.sendParticles(
                    ParticleTypes.CRIT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.15,
                    0,
                    0.12
            );
            
            // 橙色粒子代表橙子元素
            level.sendParticles(
                    ParticleTypes.FLAME,
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
     * 创建致盲黑暗气场效果
     */
    private static void createBlindingDarkAuraEffect(ServerLevel level, EliteMonsterNpc entity, LivingEntity target, Vec3 direction) {
        double startX = entity.getX();
        double startY = entity.getY() + 1.0;
        double startZ = entity.getZ();
        
        // 黑暗气场传播效果
        for (int t = 1; t <= 25; t++) {
            double progress = t / 25.0;
            double auraX = startX + direction.x * 6.0 * progress;
            double auraY = startY + level.random.nextDouble() * 0.5;
            double auraZ = startZ + direction.z * 6.0 * progress;
            
            // 黑暗气场核心
            level.sendParticles(
                    ParticleTypes.SQUID_INK,
                    auraX,
                    auraY,
                    auraZ,
                    1,
                    direction.x * 0.4,
                    0,
                    direction.z * 0.4,
                    0.05
            );
            
            // 黑暗气场周围效果
            for (int j = -3; j <= 3; j++) {
                double perpendicularX = -direction.z * j * 0.3;
                double perpendicularZ = direction.x * j * 0.3;
                
                level.sendParticles(
                        ParticleTypes.FLAME,
                        auraX + perpendicularX,
                        auraY,
                        auraZ + perpendicularZ,
                        1,
                        direction.x * 0.2,
                        0,
                        direction.z * 0.2,
                        0.03
                );
            }
        }
    }
    
    /**
     * 使目标致盲
     */
    private static void blindTarget(LivingEntity target) {
        // 这里应该应用致盲效果，使用Minecraft的效果系统
        // 由于我们没有直接访问效果系统，这里仅作为示例
    }
    
    /**
     * 创建黑暗爆炸效果粒子
     */
    private static void createDarkExplosionEffect(ServerLevel level, double x, double y, double z) {
        // 爆炸核心效果
        for (int i = 0; i < 50; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 3.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 3.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 3.0;
            
            level.sendParticles(
                    ParticleTypes.SQUID_INK,
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
        
        // 橙色火焰效果
        for (int i = 0; i < 35; i++) {
            level.sendParticles(
                    ParticleTypes.FLAME,
                    x + (level.random.nextDouble() - 0.5) * 3.5,
                    y + 0.5 + level.random.nextDouble() * 2.5,
                    z + (level.random.nextDouble() - 0.5) * 3.5,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    level.random.nextDouble() * 0.5 + 0.2,
                    (level.random.nextDouble() - 0.5) * 0.3,
                    0.12
            );
        }
        
        // 致盲光效
        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                    ParticleTypes.WHITE_ASH,
                    x + (level.random.nextDouble() - 0.5) * 4.0,
                    y + 1.0 + level.random.nextDouble() * 1.5,
                    z + (level.random.nextDouble() - 0.5) * 4.0,
                    1,
                    (level.random.nextDouble() - 0.5) * 0.2,
                    level.random.nextDouble() * 0.4 + 0.1,
                    (level.random.nextDouble() - 0.5) * 0.2,
                    0.1
            );
        }
    }
}