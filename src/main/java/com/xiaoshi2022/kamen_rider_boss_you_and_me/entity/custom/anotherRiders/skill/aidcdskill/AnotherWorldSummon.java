package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.skill.aidcdskill;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Decade;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.misc.DimensionalBarrier;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.EliteMonsterEquipHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class AnotherWorldSummon {
    private static final int COOLDOWN = 60; // 3秒冷却，降低冷却时间使技能更频繁触发
    private static final int SUMMON_DELAY = 40; // 2秒召唤延迟
    private static final int MAX_SUMMONS = 3; // 最大召唤数量
    private static final int MIN_HEALTH_PERCENT = 50; // 降低血量要求，从70%改为50%，更容易触发

    // 配置相关变量 - 可通过数据包和命令修改
    private static final Map<ResourceLocation, Double> SUMMON_ENTITIES = new HashMap<>();
    private static final Map<ResourceLocation, Double> LOW_HEALTH_ENTITIES = new HashMap<>();
    private static final Map<ResourceLocation, Double> ENRAGED_ENTITIES = new HashMap<>();
    private static boolean DEBUG_MODE = false;

    // 初始化默认配置
    static {
        loadDefaultConfig();
        // 暂时开启调试模式，方便测试
        DEBUG_MODE = true;
    }

    public static boolean canUse(Another_Decade user) {
        // 只有在血量充足且敌人较多时才使用召唤技能
        int nearbyEnemies = getNearbyHostileCount(user);
        int currentSummons = getCurrentSummonCount(user);
        boolean healthCheck = user.getHealth() >= user.getMaxHealth() * (MIN_HEALTH_PERCENT / 100.0F);
        boolean cooldownCheck = user.getPersistentData().getInt("AnotherWorldSummonCooldown") <= 0;
        boolean summonLimitCheck = currentSummons < MAX_SUMMONS;
        boolean enemyCheck = nearbyEnemies >= 1;
        
        // 调试信息
        if (DEBUG_MODE) {
            sendDebugMessage(user, "召唤技能检查 - 血量: " + healthCheck + 
                              ", 冷却: " + cooldownCheck + 
                              ", 召唤数: " + currentSummons + "/" + MAX_SUMMONS +
                              ", 敌人: " + nearbyEnemies);
        }

        return healthCheck && enemyCheck && summonLimitCheck && cooldownCheck;
    }

    public static void use(Another_Decade user) {
        if (!canUse(user) || user.level().isClientSide()) {
            return;
        }

        // 播放召唤音效
        user.playSound(SoundEvents.PORTAL_AMBIENT, 1.0F, 0.8F);

        // 设置召唤状态
        user.getPersistentData().putBoolean("IsSummoning", true);
        user.getPersistentData().putInt("SummonTimer", SUMMON_DELAY);

        // 设置冷却
        user.getPersistentData().putInt("AnotherWorldSummonCooldown", COOLDOWN);
    }

    public static void update(Another_Decade user) {
        // 更新冷却
        int cooldown = user.getPersistentData().getInt("AnotherWorldSummonCooldown");
        if (cooldown > 0) {
            user.getPersistentData().putInt("AnotherWorldSummonCooldown", cooldown - 1);
        }

        // 更新召唤进度
        if (user.getPersistentData().getBoolean("IsSummoning")) {
            int summonTimer = user.getPersistentData().getInt("SummonTimer");

            // 生成极光帷幕粒子效果
            spawnAuroraCurtainParticles(user);

            if (summonTimer > 0) {
                user.getPersistentData().putInt("SummonTimer", summonTimer - 1);

                // 召唤即将完成时播放音效
                if (summonTimer == 10) {
                    user.playSound(SoundEvents.PORTAL_TRAVEL, 1.0F, 0.9F);
                }
            } else {
                // 完成召唤
                summonDarkKnight(user);

                // 重置状态
                user.getPersistentData().putBoolean("IsSummoning", false);
            }
        }
    }

    private static void summonDarkKnight(Another_Decade user) {
        if (!(user.level() instanceof ServerLevel serverLevel)) return;

        // 确定召唤位置
        double angle = user.getRandom().nextDouble() * Math.PI * 2;
        double distance = 3.0D + user.getRandom().nextDouble() * 2.0D;
        double x = user.getX() + Math.cos(angle) * distance;
        double y = user.getY() + 1.0D;
        double z = user.getZ() + Math.sin(angle) * distance;

        // 生成次元壁实体
        spawnDimensionalBarrier(user, serverLevel, x, y, z);

        // 生成粒子效果
        spawnSummonParticles(user, x, y, z);
        user.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.7F);

        // 根据条件选择实体池
        Map<ResourceLocation, Double> entityPool = getEntityPoolForConditions(user);

        if (entityPool.isEmpty()) {
            if (DEBUG_MODE) {
                sendDebugMessage(user, "§c没有可召唤的实体，使用备用实体!");
            }
            summonFallbackEntity(user, serverLevel, x, y, z);
            return;
        }

        // 根据权重随机选择实体
        ResourceLocation selectedEntity = selectWeightedRandom(entityPool, user.getRandom());
        summonEntityByResourceLocation(user, serverLevel, selectedEntity, x, y, z);

        // 调试信息
        if (DEBUG_MODE || user.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            sendDebugMessage(user, "召唤了: §e" + selectedEntity +
                    " §7(血量: " + String.format("%.1f", user.getHealth() / user.getMaxHealth() * 100) + "%)");
        }
    }

    private static Map<ResourceLocation, Double> getEntityPoolForConditions(Another_Decade user) {
        Map<ResourceLocation, Double> pool = new HashMap<>();

        // 基础实体池
        pool.putAll(SUMMON_ENTITIES);

        // 根据条件添加特殊实体
        if (user.getHealth() <= user.getMaxHealth() * 0.3) {
            pool.putAll(LOW_HEALTH_ENTITIES);
            if (DEBUG_MODE) {
                sendDebugMessage(user, "§6低血量状态激活，添加低血量实体池");
            }
        }

        if (user.isEnraged()) {
            pool.putAll(ENRAGED_ENTITIES);
            if (DEBUG_MODE) {
                sendDebugMessage(user, "§c愤怒状态激活，添加愤怒实体池");
            }
        }

        return pool;
    }

    private static ResourceLocation selectWeightedRandom(Map<ResourceLocation, Double> weightedMap, net.minecraft.util.RandomSource random) {
        double totalWeight = weightedMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = random.nextDouble() * totalWeight;

        double currentWeight = 0;
        for (Map.Entry<ResourceLocation, Double> entry : weightedMap.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue <= currentWeight) {
                return entry.getKey();
            }
        }

        return weightedMap.keySet().iterator().next();
    }

    private static void summonEntityByResourceLocation(Another_Decade user, ServerLevel level, ResourceLocation entityId, double x, double y, double z) {
        EntityType<?> entityType = user.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENTITY_TYPE).get(entityId);
        if (entityType != null) {
            summonEntity(user, level, entityType, x, y, z);
        } else {
            // 实体类型不存在，使用备用实体（百界众）
            if (DEBUG_MODE) {
                sendDebugMessage(user, "§c实体不存在: §e" + entityId + "§c，使用百界众作为备用实体");
            }
            // 直接使用百界众作为备用实体
            ResourceLocation backupEntity = new ResourceLocation("kamen_rider_boss_you_and_me:elite_monster_npc");
            summonEntityByResourceLocation(user, level, backupEntity, x, y, z);
        }
    }

    private static void summonEntity(Another_Decade user, ServerLevel level, EntityType<?> entityType, double x, double y, double z) {
        Entity entity = entityType.create(level);
        if (entity instanceof Mob mob) {
            setupSummonedEntity(user, mob, x, y, z);
        } else {
            // 如果不是生物，直接生成在位置
            entity.setPos(x, y, z);
            level.addFreshEntity(entity);
        }
    }

    private static void setupSummonedEntity(Another_Decade user, Mob summonedEntity, double x, double y, double z) {
        summonedEntity.setPos(x, y, z);
        summonedEntity.setAggressive(true);

        // 设置生命值加成（基于异类Decade的强度）
        float healthMultiplier = 1.5F;
        if (user.isEnraged()) {
            healthMultiplier = 2.0F;
        }
        summonedEntity.setHealth(summonedEntity.getMaxHealth() * healthMultiplier);

        // 为召唤物设置目标（如果异类Decade有目标）
        if (user.getTarget() != null) {
            summonedEntity.setTarget(user.getTarget());
        }

        // 特殊处理：为百界众（EliteMonsterNpc）装备随机腰带并触发变身
        if (summonedEntity instanceof EliteMonsterNpc eliteMonster) {
            EliteMonsterEquipHandler.equipRandomDriverAndTransform(eliteMonster);
        }

        // 添加到世界
        ((ServerLevel) user.level()).addFreshEntity(summonedEntity);

        // 标记为异类Decade的召唤物
        summonedEntity.getPersistentData().putBoolean("IsAnotherDecadeSummon", true);
        summonedEntity.getPersistentData().putUUID("SummonerUUID", user.getUUID());
    }

    private static void summonFallbackEntity(Another_Decade user, ServerLevel level, double x, double y, double z) {
        // 备用实体列表 - 只保留百界众
        ResourceLocation[] fallbackEntities = {
                new ResourceLocation("kamen_rider_boss_you_and_me:elite_monster_npc")
        };

        ResourceLocation fallbackType = fallbackEntities[user.getRandom().nextInt(fallbackEntities.length)];
        summonEntityByResourceLocation(user, level, fallbackType, x, y, z);

        if (DEBUG_MODE) {
            sendDebugMessage(user, "§6使用备用实体: §e" + fallbackType);
        }
    }

    private static void spawnSummonParticles(Another_Decade user, double x, double y, double z) {
        if (!(user.level() instanceof ServerLevel serverLevel)) return;

        // 生成大量粒子效果表示极光帷幕打开
        for (int i = 0; i < 50; i++) {
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    x + (user.getRandom().nextDouble() - 0.5) * 2.0D,
                    y + user.getRandom().nextDouble() * 3.0D,
                    z + (user.getRandom().nextDouble() - 0.5) * 2.0D,
                    1,
                    (user.getRandom().nextDouble() - 0.5) * 0.2D,
                    user.getRandom().nextDouble() * 0.2D,
                    (user.getRandom().nextDouble() - 0.5) * 0.2D,
                    0.1D
            );

            if (user.getRandom().nextBoolean()) {
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        x + (user.getRandom().nextDouble() - 0.5) * 2.0D,
                        y + user.getRandom().nextDouble() * 3.0D,
                        z + (user.getRandom().nextDouble() - 0.5) * 2.0D,
                        1,
                        (user.getRandom().nextDouble() - 0.5) * 0.2D,
                        user.getRandom().nextDouble() * 0.2D,
                        (user.getRandom().nextDouble() - 0.5) * 0.2D,
                        0.1D
                );
            }
        }
    }

    private static void spawnAuroraCurtainParticles(Another_Decade user) {
        if (!(user.level() instanceof ServerLevel serverLevel)) return;

        // 生成紫色极光帷幕效果
        for (int i = 0; i < 10; i++) {
            double angle = (i * 36) * Math.PI / 180;
            double distance = 2.5D + Math.sin(user.tickCount * 0.05D) * 0.5D;
            double x = user.getX() + Math.cos(angle) * distance;
            double y = user.getY() + 1.0D + Math.sin(user.tickCount * 0.03D + angle) * 0.5D;
            double z = user.getZ() + Math.sin(angle) * distance;

            serverLevel.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    x, y, z,
                    1,
                    0, 0, 0,
                    0.1D
            );

            if (user.getRandom().nextBoolean()) {
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        x, y, z,
                        1,
                        0, 0, 0,
                        0.1D
                );
            }
        }
    }

    private static void spawnDimensionalBarrier(Another_Decade user, ServerLevel serverLevel, double x, double y, double z) {
        // 创建并配置次元壁实体
        DimensionalBarrier barrier = new DimensionalBarrier(serverLevel, user.getUUID());
        barrier.setPos(x, y, z);
        barrier.setYRot(user.getRandom().nextFloat() * 360.0F); // 随机旋转角度
        barrier.setLifetime(60); // 3秒生命周期
        
        // 添加到世界
        serverLevel.addFreshEntity(barrier);
        
        // 播放次元壁出现音效
//        user.playSound(SoundEvents.PORTAL_OPEN, 1.0F, 0.9F);
    }

    private static void sendDebugMessage(Another_Decade user, String message) {
        user.level().players().forEach(player ->
                player.sendSystemMessage(Component.literal("§d[Another Decade] §7" + message))
        );
    }

    private static int getNearbyHostileCount(Another_Decade user) {
        return user.level().getEntitiesOfClass(
                LivingEntity.class,
                user.getBoundingBox().inflate(15.0D),
                entity -> (entity instanceof net.minecraft.world.entity.player.Player || 
                          entity instanceof Monster ||
                          (entity instanceof Mob && ((Mob)entity).getTarget() == user)) &&
                        !entity.isAlliedTo(user)
        ).size();
    }

    private static int getCurrentSummonCount(Another_Decade user) {
        return user.level().getEntitiesOfClass(
                Mob.class,
                user.getBoundingBox().inflate(50.0D),
                entity -> entity.getPersistentData().getBoolean("IsAnotherDecadeSummon") &&
                        entity.getPersistentData().getUUID("SummonerUUID").equals(user.getUUID())
        ).size();
    }

    // 配置管理方法
    private static void loadDefaultConfig() {
        // 清空现有配置
        SUMMON_ENTITIES.clear();
        LOW_HEALTH_ENTITIES.clear();
        ENRAGED_ENTITIES.clear();

        // 默认基础实体 - 只保留百界众
        SUMMON_ENTITIES.put(new ResourceLocation("kamen_rider_boss_you_and_me:elite_monster_npc"), 1.2);

        // 默认低血量实体 - 只保留百界众
        LOW_HEALTH_ENTITIES.put(new ResourceLocation("kamen_rider_boss_you_and_me:elite_monster_npc"), 1.5);

        // 默认愤怒实体 - 只保留百界众
        ENRAGED_ENTITIES.put(new ResourceLocation("kamen_rider_boss_you_and_me:elite_monster_npc"), 2.0);

        DEBUG_MODE = false;
    }

    // 命令和数据包集成接口方法
    public static void addSummonEntity(ResourceLocation entityId, double weight) {
        // 限制权重范围在合理区间
        double clampedWeight = Math.max(0.1, Math.min(5.0, weight));
        SUMMON_ENTITIES.put(entityId, clampedWeight);
        if (DEBUG_MODE) {
            System.out.println("AnotherDecade: 添加实体到普通召唤池: " + entityId + " 权重: " + clampedWeight);
        }
    }

    public static void addLowHealthEntity(ResourceLocation entityId, double weight) {
        // 限制权重范围在合理区间
        double clampedWeight = Math.max(0.1, Math.min(5.0, weight));
        LOW_HEALTH_ENTITIES.put(entityId, clampedWeight);
        if (DEBUG_MODE) {
            System.out.println("AnotherDecade: 添加实体到低血量召唤池: " + entityId + " 权重: " + clampedWeight);
        }
    }

    public static void addEnragedEntity(ResourceLocation entityId, double weight) {
        // 限制权重范围在合理区间
        double clampedWeight = Math.max(0.1, Math.min(5.0, weight));
        ENRAGED_ENTITIES.put(entityId, clampedWeight);
        if (DEBUG_MODE) {
            System.out.println("AnotherDecade: 添加实体到愤怒召唤池: " + entityId + " 权重: " + clampedWeight);
        }
    }

    public static void setDebugMode(boolean debug) {
        DEBUG_MODE = debug;
        System.out.println("AnotherDecade: 调试模式已" + (debug ? "开启" : "关闭"));
    }

    public static void clearAllEntities() {
        SUMMON_ENTITIES.clear();
        LOW_HEALTH_ENTITIES.clear();
        ENRAGED_ENTITIES.clear();
        if (DEBUG_MODE) {
            System.out.println("AnotherDecade: 已清空所有召唤实体列表");
        }
    }

    public static List<String> getSummonableEntities() {
        return SUMMON_ENTITIES.entrySet().stream()
                .map(entry -> entry.getKey() + " (权重: " + entry.getValue() + ")")
                .collect(Collectors.toList());
    }
    
    // 获取低血量可召唤实体列表
    public static List<String> getLowHealthSummonableEntities() {
        return LOW_HEALTH_ENTITIES.entrySet().stream()
                .map(entry -> entry.getKey() + " (权重: " + entry.getValue() + ")")
                .collect(Collectors.toList());
    }
    
    // 获取愤怒状态可召唤实体列表
    public static List<String> getEnragedSummonableEntities() {
        return ENRAGED_ENTITIES.entrySet().stream()
                .map(entry -> entry.getKey() + " (权重: " + entry.getValue() + ")")
                .collect(Collectors.toList());
    }
    
    // 通过命令字符串添加实体 (方便命令系统调用)
    public static boolean addSummonEntityByString(String entityIdStr, double weight) {
        try {
            ResourceLocation entityId = new ResourceLocation(entityIdStr);
            addSummonEntity(entityId, weight);
            return true;
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.out.println("AnotherDecade: 无效的实体ID: " + entityIdStr);
            }
            return false;
        }
    }
    
    // 通过命令字符串添加低血量实体
    public static boolean addLowHealthEntityByString(String entityIdStr, double weight) {
        try {
            ResourceLocation entityId = new ResourceLocation(entityIdStr);
            addLowHealthEntity(entityId, weight);
            return true;
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.out.println("AnotherDecade: 无效的实体ID: " + entityIdStr);
            }
            return false;
        }
    }
    
    // 通过命令字符串添加愤怒状态实体
    public static boolean addEnragedEntityByString(String entityIdStr, double weight) {
        try {
            ResourceLocation entityId = new ResourceLocation(entityIdStr);
            addEnragedEntity(entityId, weight);
            return true;
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.out.println("AnotherDecade: 无效的实体ID: " + entityIdStr);
            }
            return false;
        }
    }
}