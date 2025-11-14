package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.skill;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 假面骑士Duke的技能实现
 * Duke是《假面骑士铠武》中的反派骑士，变身者是战极凌马
 * 拥有Lemon Energy Arms形态和Dragon Energy Arms强化形态，使用音速箭矢作为武器
 * 融合了柠檬能量和龙之力量的强大攻击能力
 */
public class DukeRiderSkill {
    
    // 静态初始化块，注册事件监听器
    static {
        MinecraftForge.EVENT_BUS.register(DukeRiderSkill.class);
    }
    
    /**
     * 内部类，用于存储残影的信息
     */
    private static class AfterimageInfo {
        private final double x;         // 残影位置X坐标
        private final double y;         // 残影位置Y坐标
        private final double z;         // 残影位置Z坐标
        private final UUID ownerUUID;   // 拥有者UUID
        private final long creationTime; // 创建时间（游戏tick）
        private final UUID id;          // 唯一标识符
        private final ServerLevel level; // 所属世界
        
        public AfterimageInfo(double x, double y, double z, EliteMonsterNpc owner, long creationTime, ServerLevel level) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.ownerUUID = owner.getUUID();
            this.creationTime = creationTime;
            this.id = UUID.randomUUID();
            this.level = level;
        }
        
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public UUID getOwnerUUID() { return ownerUUID; }
        public long getCreationTime() { return creationTime; }
        public UUID getId() { return id; }
        public ServerLevel getLevel() { return level; }
        
        /**
         * 检查残影是否过期（超过5秒）
         */
        public boolean isExpired(long currentTime) {
            return currentTime - creationTime > 100; // 20tick/秒 * 5秒 = 100tick
        }
    }
    
    // 存储所有活跃的残影
    private static final List<AfterimageInfo> activeAfterimages = new ArrayList<>();
    
    /**
     * 服务器tick事件处理，定期检查并清除过期的残影
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 每5个tick执行一次检查，减少性能消耗
            if (event.getServer().getWorldData().overworldData().getGameTime() % 5 == 0) {
                cleanupExpiredAfterimages();
            }
        }
    }
    
    /**
     * 设置Duke的盔甲
     */
    public static void setArmor(EliteMonsterNpc entity) {
        // 设置头盔（模拟Duke的头饰）
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.DUKE_HELMET.get()));
        // 设置胸甲（模拟Duke的胸甲）
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.DUKE_CHESTPLATE.get()));
        // 设置护腿（模拟Duke的护腿）
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.DUKE_LEGGINGS.get()));

        // 设置主手武器（模拟Duke的音速箭矢）
        entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.SONICARROW.get()));
    }
    
    /**
     * 执行Duke的技能
     * 结合柠檬能量和龙之力量，使用音速箭矢对敌人发起强力攻击
     * 同时生成存在5秒的残影
     */
    public static void perform(EliteMonsterNpc entity) {
        // 只在服务器端处理
        if (entity.level().isClientSide) {
            return;
        }
        
        // 生成残影
        createAfterimages(entity);
        
        LivingEntity target = entity.getTarget();
        if (target == null) {
            return;
        }
        
        ServerLevel serverLevel = (ServerLevel) entity.level();
        
        // 生成能量箭矢蓄力效果
        createArrowChargeEffect(serverLevel, entity.getX(), entity.getY(), entity.getZ());
        
        // 计算从当前位置到目标的方向
        double directionX = target.getX() - entity.getX();
        double directionZ = target.getZ() - entity.getZ();
        double distance = Math.sqrt(directionX * directionX + directionZ * directionZ);
        
        if (distance > 0) {
            // 标准化方向向量
            directionX /= distance;
            directionZ /= distance;
            
            // 发射能量箭矢的视觉效果
            createEnergyArrowVisual(serverLevel, entity.getX(), entity.getY() + 1.0, entity.getZ(), target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
            
            // 创建一个任务，在延迟后对目标造成伤害
            double finalDirectionZ = directionZ;
            double finalDirectionX = directionX;
            entity.level().getServer().execute(() -> {
                if (entity.isAlive() && target.isAlive()) {
                    // 计算最终伤害（基于基础攻击力，Duke技能有高伤害加成）
                double baseDamage = entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
                double dukeDamage = baseDamage * 2.5; // 150%伤害加成
                    
                    // 对目标造成伤害
                    boolean damaged = target.hurt(entity.damageSources().mobAttack(entity), (float) dukeDamage);
                    
                    // 强力击退效果
                    if (damaged) {
                        target.knockback(2.0, finalDirectionX, finalDirectionZ);
                    }
                    
                    // 生成箭矢命中爆炸效果
                    createArrowImpactEffect(serverLevel, target.getX(), target.getY(), target.getZ());
                }
            });
        }
    }
    
    /**
     * 创建箭矢蓄力效果粒子
     */
    private static void createArrowChargeEffect(ServerLevel level, double x, double y, double z) {
        for (int i = 0; i < 40; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.0;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.0;
            
            // 紫色粒子模拟龙能量
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.1,
                    0,
                    0.15
            );
            
            // 黄色粒子模拟柠檬能量
            level.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.08,
                    0,
                    0.1
            );
        }
    }
    
    /**
     * 创建能量箭矢飞行视觉效果
     */
    private static void createEnergyArrowVisual(ServerLevel level, double startX, double startY, double startZ, double endX, double endY, double endZ) {
        // 计算飞行路径上的多个点
        int steps = 20;
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            double x = startX + (endX - startX) * progress;
            double y = startY + (endY - startY) * progress;
            double z = startZ + (endZ - startZ) * progress;
            
            // 在路径上生成粒子
            level.sendParticles(
                    ParticleTypes.CRIT,
                    x,
                    y,
                    z,
                    1,
                    0,
                    0,
                    0,
                    0.1
            );
            
            level.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    x,
                    y,
                    z,
                    1,
                    0,
                    0,
                    0,
                    0.05
            );
        }
    }
    
    /**
     * 创建箭矢命中爆炸效果粒子
     */
    private static void createArrowImpactEffect(ServerLevel level, double x, double y, double z) {
        // 核心爆炸效果
        for (int i = 0; i < 35; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.5;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.5;
            
            level.sendParticles(
                    ParticleTypes.EXPLOSION,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    level.random.nextDouble() - 0.5,
                    level.random.nextDouble() * 0.5,
                    level.random.nextDouble() - 0.5,
                    0.2
            );
        }
        
        // 龙形扩散效果
        for (int i = 0; i < 60; i += 4) {
            double angle = Math.toRadians(i);
            double spreadX = Math.cos(angle) * 2.0;
            double spreadZ = Math.sin(angle) * 2.0;
            
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x,
                    y + 1.0,
                    z,
                    1,
                    spreadX * 0.8,
                    0.3,
                    spreadZ * 0.8,
                    0.2
            );
        }
        
        // 中央强光效果
        level.sendParticles(
                ParticleTypes.CRIT,
                x,
                y + 0.7,
                z,
                15,
                0,
                0,
                0,
                0.4
        );
    }
    
    /**
     * 创建Duke的残影
     * 生成存在5秒的幻影实体，模拟分身效果
     */
    private static void createAfterimages(EliteMonsterNpc entity) {
        ServerLevel serverLevel = (ServerLevel) entity.level();
        
        // 生成3个残影，分布在不同位置
        for (int i = 0; i < 3; i++) {
            // 计算随机偏移位置，让残影分布在主体周围
            double angle = (Math.PI * 2 * i) / 3.0; // 三个等距的角度
            double radius = 2.0 + entity.level().random.nextDouble() * 1.0; // 距离主体2-3格
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            
            double x = entity.getX() + offsetX;
            double y = entity.getY();
            double z = entity.getZ() + offsetZ;
            
            // 创建残影的视觉效果
            spawnAfterimageVisual(serverLevel, x, y, z, entity);
            
            // 创建残影信息并存储
            long currentTime = serverLevel.getGameTime();
            AfterimageInfo afterimage = new AfterimageInfo(x, y, z, entity, currentTime, serverLevel);
            
            // 将残影添加到活跃列表
            activeAfterimages.add(afterimage);
            
            // 不需要手动启动清理任务，事件处理器会自动处理
            
            // 保存要清除的残影位置到实体的NBT数据中，供延迟命令使用
            // 注意：在实际实现中，你可能需要一个更完善的方式来存储这些位置信息
        }
    }
    
    /**
     * 生成残影的视觉效果
     */
    private static void spawnAfterimageVisual(ServerLevel level, double x, double y, double z, EliteMonsterNpc originalEntity) {
        // 创建半透明的实体轮廓效果
        for (int i = 0; i < 60; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.0;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.0;
            
            // 紫色粒子模拟龙能量残影
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.05,
                    0,
                    0.1
            );
            
            // 黄色粒子模拟柠檬能量残影
            level.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.03,
                    0,
                    0.08
            );
            
            // 半透明效果的粒子
            level.sendParticles(
                    ParticleTypes.CRIT,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    1,
                    0,
                    0.02,
                    0,
                    0.05
            );
        }
        
        // 残影位置的持续粒子效果，使其看起来更像实体
        createContinuousAfterimageEffect(level, x, y, z);
    }
    
    /**
     * 创建持续的残影粒子效果
     */
    private static void createContinuousAfterimageEffect(ServerLevel level, double x, double y, double z) {
        // 这里我们通过在服务器上定期生成粒子来模拟持续的残影效果
        // 由于Minecraft的粒子系统限制，我们使用多个短时粒子效果组合来模拟长期存在的残影
        for (int tickDelay = 0; tickDelay < 100; tickDelay += 5) { // 每5tick生成一次，共20次
            final double finalX = x;
            final double finalY = y;
            final double finalZ = z;
            final int finalTickDelay = tickDelay;
            
            // 使用更简单直接的方式来处理持续粒子效果，避免复杂的服务器调度
            // 直接生成一次性的粒子云，但使用更多的粒子数量和持续时间参数
            for (int i = 0; i < 30; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
                double offsetY = level.random.nextDouble() * 1.8;
                double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
                
                // 使用较大的持续时间参数，让粒子在空中停留更长时间
                level.sendParticles(
                        ParticleTypes.CRIT,
                        finalX + offsetX,
                        finalY + offsetY,
                        finalZ + offsetZ,
                        1,
                        0,
                        0.01,
                        0,
                        0.5 // 增加持续时间
                );
            }
        }
    }
    
    /**
     * 清理过期的残影
     */
    private static void cleanupExpiredAfterimages() {
        // 获取第一个残影所在的世界来获取游戏时间
        long currentTime = 0;
        if (!activeAfterimages.isEmpty()) {
            ServerLevel level = activeAfterimages.get(0).getLevel();
            if (level != null) {
                currentTime = level.getGameTime();
            }
        }
        
        List<AfterimageInfo> toRemove = new ArrayList<>();
        
        // 找出所有过期的残影
        for (AfterimageInfo afterimage : activeAfterimages) {
            if (afterimage.isExpired(currentTime)) {
                toRemove.add(afterimage);
                // 为每个过期的残影创建清除视觉效果
                clearAfterimageVisual(afterimage.getLevel(), afterimage.getX(), afterimage.getY(), afterimage.getZ());
            }
        }
        
        // 从活跃列表中移除过期的残影
        activeAfterimages.removeAll(toRemove);
    }
    
    /**
     * 清除残影的视觉效果
     */
    private static void clearAfterimageVisual(ServerLevel level, double x, double y, double z) {
        // 创建一个更华丽的残影消失效果，结合多种粒子
        
        // 1. 创建POOF粒子作为主要消失效果
        for (int i = 0; i < 40; i++) {
            // 随机偏移量，使粒子向各个方向扩散
            double offsetX = (level.random.nextDouble() - 0.5) * 3.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 3.0 + 0.2; // 稍微向上
            double offsetZ = (level.random.nextDouble() - 0.5) * 3.0;
            
            level.sendParticles(
                    ParticleTypes.POOF,
                    x + offsetX * 0.5,
                    y,
                    z + offsetZ * 0.5,
                    1,
                    offsetX * 0.1,
                    offsetY * 0.1,
                    offsetZ * 0.1,
                    0.2
            );
        }
        
        // 2. 添加白色雾气粒子，增强消散感
        for (int i = 0; i < 25; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 4.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 4.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 4.0;
            
            level.sendParticles(
                    ParticleTypes.WHITE_ASH,
                    x + offsetX * 0.3,
                    y,
                    z + offsetZ * 0.3,
                    1,
                    offsetX * 0.05,
                    offsetY * 0.05,
                    offsetZ * 0.05,
                    0.15
            );
        }
        
        // 3. 添加紫色魔法粒子，呼应Duke骑士的主题色
        for (int i = 0; i < 20; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * 0.6;
            double offsetX = Math.cos(angle) * distance;
            double offsetY = level.random.nextDouble() * 0.3 + 0.1;
            double offsetZ = Math.sin(angle) * distance;
            
            // 创建螺旋上升的紫色粒子
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    x + offsetX * 0.5,
                    y,
                    z + offsetZ * 0.5,
                    1,
                    offsetX * 0.8,
                    offsetY * 2,
                    offsetZ * 0.8,
                    0.3
            );
        }
        
        // 4. 添加小型爆炸效果，模拟能量爆发
        for (int i = 0; i < 15; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = level.random.nextDouble() * 0.5;
            double offsetX = Math.cos(angle) * distance;
            double offsetY = level.random.nextDouble() * 0.4 + 0.2;
            double offsetZ = Math.sin(angle) * distance;
            
            // 使用火花粒子创建爆炸效果
            level.sendParticles(
                    ParticleTypes.CRIT,
                    x,
                    y,
                    z,
                    1,
                    offsetX,
                    offsetY,
                    offsetZ,
                    0.2
            );
        }
    }
}