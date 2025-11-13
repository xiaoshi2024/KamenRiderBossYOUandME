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
 * 假面骑士斩月·真的技能实现
 * 斩月·真是《假面骑士铠武》中的重要骑士，由吴岛贵虎变身而成
 * 使用蜜瓜能量锁种，拥有强大的防御力和锋利的斩击能力
 * 技能特点是力量集中的强力斩击
 */
public class ZangetsuShinSkill {
    
    /**
     * 设置斩月·真的盔甲和对应的武器
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟斩月·真的头饰）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.ZANGETSU_SHIN_HELMET.get()));
        // 设置胸甲（模拟斩月·真的胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.ZANGETSU_SHIN_CHESTPLATE.get()));
        // 设置护腿（模拟斩月·真的护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.ZANGETSU_SHIN_LEGGINGS.get()));

        // 即时替换主手武器为斩月·真的武器：音速弓（根据用户要求保留音速弓）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.SONICARROW.get()));
    }
    
    /**
     * 执行斩月·真的技能
     * 使用蜜瓜能量集中在武器上，对敌人发起强力斩击
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
        
        // 生成蜜瓜能量蓄力效果
        createMelonEnergyChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 武器能量集中效果
        createWeaponChargeEffect(serverLevel, entity.getX(), entity.getY() + 1.0, entity.getZ());
        
        // 计算从当前位置到目标的方向
        double directionX = target.getX() - entity.getX();
        double directionZ = target.getZ() - entity.getZ();
        double distance = Math.sqrt(directionX * directionX + directionZ * directionZ);
        
        if (distance > 0) {
            // 标准化方向向量
            directionX /= distance;
            directionZ /= distance;
            
            // 高速冲刺接近目标
            entity.push(directionX * 1.8, 0.7, directionZ * 1.8);
            
            // 创建一个任务，在延迟后对目标造成伤害
            double finalDirectionX = directionX;
            double finalDirectionZ = directionZ;
            entity.level().getServer().execute(() -> {
                if (entity.isAlive() && target.isAlive()) {
                    // 计算最终伤害（基于基础攻击力，斩月·真技能有高伤害加成）
                    double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    double zangetsuDamage = baseDamage * 2.0; // 100%伤害加成
                    
                    // 对目标造成伤害
                    boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) zangetsuDamage);
                    
                    // 强力击退效果
                    if (damaged) {
                        target.knockback(2.2, finalDirectionX, finalDirectionZ);
                    }
                    
                    // 生成斩击效果
                    createSlashEffect(serverLevel, target.getX(), target.getY(), target.getZ(), finalDirectionX, finalDirectionZ);
                }
            });
        }
    }
    
    /**
     * 创建蜜瓜能量蓄力效果粒子
     */
    private static void createMelonEnergyChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
            
            // 绿色粒子模拟蜜瓜能量
            level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.12,
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
                    0.08,
                    0,
                    0.06
            );
        }
    }
    
    /**
     * 创建武器能量集中效果粒子
     */
    private static void createWeaponChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 25; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = level.random.nextDouble() * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
            
            level.sendParticles(
                    ParticleTypes.CRIT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.05,
                    0,
                    0.15
            );
        }
    }
    
    /**
     * 创建斩击效果粒子
     */
    private static void createSlashEffect(ServerLevel level, double x, double y, double z, double directionX, double directionZ) {
        // 核心斩击效果
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            
            level.sendParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    directionX * 0.5,
                    0.2,
                    directionZ * 0.5,
                    0.2
            );
        }
        
        // 刀光效果（垂直于攻击方向）
        double perpendicularX = -directionZ;
        double perpendicularZ = directionX;
        
        for (int i = -5; i <= 5; i++) {
            double spreadX = perpendicularX * (i * 0.2);
            double spreadZ = perpendicularZ * (i * 0.2);
            
            level.sendParticles(
                    ParticleTypes.CRIT,
                    x + spreadX,
                    y + 0.8,
                    z + spreadZ,
                    1,
                    directionX * 2.0,
                    0,
                    directionZ * 2.0,
                    0.3
            );
        }
        
        // 爆炸效果
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                x,
                y + 0.5,
                z,
                15,
                0,
                0,
                0,
                0.3
        );
    }
}