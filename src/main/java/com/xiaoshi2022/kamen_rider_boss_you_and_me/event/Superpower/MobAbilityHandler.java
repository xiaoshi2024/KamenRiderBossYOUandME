package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.MobRiderVariables;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class MobAbilityHandler {
    private static final Random RANDOM = new Random();
    private static final int ABILITY_CHECK_INTERVAL = 20; // 每20tick检查一次（1秒）

    /**
     * 服务器tick事件，检查并触发非玩家生物的技能
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // 只在结束阶段执行，并且每20tick（1秒）执行一次
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % ABILITY_CHECK_INTERVAL != 0) {
            return;
        }

        // 遍历所有世界中的非玩家生物
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // 检查并移除过期的柠檬分身
            checkAndRemoveExpiredClones(level);

            // 修复：使用正确的getEntitiesOfClass方法，添加边界参数
            AABB worldBounds = getWorldBounds(level);
            for (Mob mob : level.getEntitiesOfClass(Mob.class, worldBounds, entity -> true)) {
                // 检查生物是否装备了骑士装备
                if (RiderEnergyHandler.hasRiderEquipment(mob)) {
                    // 处理僵尸和村民的血量提升
                    handleZombieVillagerHealthBoost(mob);
                    
                    // 处理技能触发
                    processMobAbilities(mob);
                }
            }
        }
    }

    /**
     * 处理僵尸和村民装备骑士装备后的血量提升和音速弓装备
     */
    private static void handleZombieVillagerHealthBoost(Mob mob) {
        // 检查是否是僵尸或村民
        boolean isZombie = mob.getType() == net.minecraft.world.entity.EntityType.ZOMBIE || 
                          mob.getType() == net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER;
        boolean isVillager = mob.getType() == net.minecraft.world.entity.EntityType.VILLAGER ||
                           mob.getType() == net.minecraft.world.entity.EntityType.WANDERING_TRADER;
        
        // 如果是僵尸或村民，并且血量没有被提升过（通过持久化数据标记）
        if ((isZombie || isVillager) && !mob.getPersistentData().getBoolean("RiderHealthBoostApplied")) {
            // 设置最大生命值为40
            mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(40.0);
            // 同时恢复到满生命值
            mob.setHealth(40.0F);
            // 标记已经应用过血量提升
            mob.getPersistentData().putBoolean("RiderHealthBoostApplied", true);
        }
    }
    
    /**
     * 获取世界的边界范围
     */
    private static AABB getWorldBounds(ServerLevel level) {
        return new AABB(
                Double.NEGATIVE_INFINITY, level.getMinBuildHeight(), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, level.getMaxBuildHeight(), Double.POSITIVE_INFINITY
        );
    }

    /**
     * 处理非玩家生物的技能
     */
    private static void processMobAbilities(Mob mob) {
        // 获取生物的骑士数据
        MobRiderVariables.MobRiderData data = mob.getCapability(MobRiderVariables.MOB_RIDER_DATA_CAPABILITY).orElse(new MobRiderVariables.MobRiderData());

        // 检查是否有目标
        Entity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            // 如果没有目标，尝试寻找新目标
            findTarget(mob);
            return;
        }

        // 根据装备的不同类型触发相应的技能
        if (hasBaronEquipment(mob)) {
            // 处理巴隆相关技能
            handleBaronAbilities(mob, target, data);
        } else if (hasGenesisDriver(mob)) {
            // 处理Genesis驱动器相关技能
            handleGenesisDriverAbilities(mob, target, data);
        } else if (hasMegaUiorder(mob)) {
            // 处理Mega Uiorder相关技能
            handleMegaUiorderAbilities(mob, target, data);
        }

        // 通用技能处理（适用于所有装备）
        handleGenericAbilities(mob, target, data);

        // 检查是否可以触发樱桃能量箭矢技能
        CherrySonicArrowAbilityHandler.checkAndTriggerForMob(mob);
    }

    /**
     * 尝试为生物寻找目标
     */
    private static void findTarget(Mob mob) {
        // 寻找15格范围内的玩家作为目标
        List<Player> nearbyPlayers = mob.level().getEntitiesOfClass(Player.class,
                new AABB(mob.getX() - 15, mob.getY() - 15, mob.getZ() - 15,
                        mob.getX() + 15, mob.getY() + 15, mob.getZ() + 15));

        if (!nearbyPlayers.isEmpty()) {
            // 选择最近的玩家作为目标
            Player nearestPlayer = null;
            double nearestDistance = Double.MAX_VALUE;

            for (Player player : nearbyPlayers) {
                double distance = mob.distanceToSqr(player);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPlayer = player;
                }
            }

            if (nearestPlayer != null) {
                mob.setTarget(nearestPlayer);
            }
        }
    }

    /**
     * 检查生物是否装备了巴隆相关装备
     */
    private static boolean hasBaronEquipment(Mob mob) {
        ItemStack mainHand = mob.getMainHandItem();
        ItemStack offHand = mob.getOffhandItem();

        return mainHand.getItem() instanceof rider_baronsItem ||
                offHand.getItem() instanceof rider_baronsItem ||
                mainHand.getItem() instanceof baron_lemonItem ||
                offHand.getItem() instanceof baron_lemonItem;
    }

    /**
     * 检查生物是否装备了Genesis驱动器
     */
    private static boolean hasGenesisDriver(Mob mob) {
        ItemStack mainHand = mob.getMainHandItem();
        ItemStack offHand = mob.getOffhandItem();

        return mainHand.getItem() instanceof Genesis_driver ||
                offHand.getItem() instanceof Genesis_driver;
    }

    /**
     * 检查生物是否装备了Mega Uiorder
     */
    private static boolean hasMegaUiorder(Mob mob) {
        ItemStack mainHand = mob.getMainHandItem();
        ItemStack offHand = mob.getOffhandItem();

        return mainHand.getItem() instanceof Mega_uiorder ||
                offHand.getItem() instanceof Mega_uiorder;
    }

    // 临时占位方法 - 如果你没有DarkKiva装备，可以删除相关代码
    private static boolean hasDarkKivaEquipment(Mob mob) {
        // 实现你的DarkKiva装备检查逻辑
        return false;
    }

    // 临时占位方法 - 如果你没有DarkKiva技能，可以删除相关代码
    private static void handleDarkKivaAbilities(Mob mob, Entity target, MobRiderVariables.MobRiderData data) {
        // 实现你的DarkKiva技能逻辑
    }

    /**
     * 处理巴隆相关技能
     */
    private static void handleBaronAbilities(Mob mob, Entity target, MobRiderVariables.MobRiderData data) {
        ItemStack mainHand = mob.getMainHandItem();
        ItemStack offHand = mob.getOffhandItem();
        
        // 检查是否装备了柠檬相关装备
        boolean hasLemonEquipment = mainHand.getItem() instanceof baron_lemonItem || 
                                    offHand.getItem() instanceof baron_lemonItem;
        
        // 柠檬分身技能
        if (hasLemonEquipment && RANDOM.nextFloat() < 0.1f && data.lemon_clone_cooldown < mob.level().getGameTime()) {
            // 检查能量是否足够（消耗30点能量）
            if (RiderEnergyHandler.consumeRiderEnergy(mob, 30.0)) {
                // 生成柠檬分身
                spawnLemonClone(mob);
                
                // 设置冷却时间（15秒）
                data.lemon_clone_cooldown = mob.level().getGameTime() + 300;
            }
        } else {
            // 香蕉能量技能（非柠檬装备时触发）
            if (RANDOM.nextFloat() < 0.2f && data.baron_banana_energy_cooldown < mob.level().getGameTime()) {
                // 检查能量是否足够（消耗20点能量）
                if (RiderEnergyHandler.consumeRiderEnergy(mob, 20.0)) {
                    // 生成香蕉能量实体（简化版实现）
                    spawnBananaEnergyEntity(mob);

                    // 设置冷却时间（10秒）
                    data.baron_banana_energy_cooldown = mob.level().getGameTime() + 200;
                }
            }
        }
    }
    
    /**
     * 生成柠檬分身
     */
    /**
     * 生成柠檬分身（简化版）
     */
    private static void spawnLemonClone(Mob originalMob) {
        try {
            // 创建原始生物的副本
            Mob clone = (Mob) originalMob.getType().create(originalMob.level());
            if (clone != null) {
                // 设置分身位置
                double offsetX = (RANDOM.nextDouble() - 0.5) * 2.0;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 2.0;
                clone.moveTo(
                        originalMob.getX() + offsetX,
                        originalMob.getY(),
                        originalMob.getZ() + offsetZ,
                        originalMob.getYRot(),
                        originalMob.getXRot()
                );

                // 复制装备和属性
                clone.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, originalMob.getMainHandItem().copy());
                clone.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, originalMob.getOffhandItem().copy());
                clone.setHealth(originalMob.getHealth() * 0.7f);

                if (originalMob.getTarget() != null) {
                    clone.setTarget(originalMob.getTarget());
                }

                // 添加消失计时器效果
                clone.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WEAKNESS,
                        1200, // 60秒
                        0,
                        false,
                        false
                ));

                // 播放音效并添加到世界
                originalMob.level().playSound(null, originalMob.getX(), originalMob.getY(), originalMob.getZ(),
                        net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                        net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 0.8f);

                originalMob.level().addFreshEntity(clone);

                // 使用数据标记来跟踪分身
                clone.getPersistentData().putBoolean("IsLemonClone", true);
                clone.getPersistentData().putLong("RemoveAt", originalMob.level().getGameTime() + 1200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理Genesis驱动器相关技能
     */
    private static void handleGenesisDriverAbilities(Mob mob, Entity target, MobRiderVariables.MobRiderData data) {
        // 15%概率触发能量增强
        if (RANDOM.nextFloat() < 0.15f) {
            // 检查能量是否足够（消耗25点能量）
            if (RiderEnergyHandler.consumeRiderEnergy(mob, 25.0)) {
                // 增加攻击力（持续15秒）
                mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                        .addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                "GenesisDriverAttackBoost",
                                0.5,
                                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
                        ));

                // 300tick后自动移除buff（这里简化处理，实际应该用定时器）
            }
        }
    }

    /**
     * 处理Mega Uiorder相关技能
     */
    private static void handleMegaUiorderAbilities(Mob mob, Entity target, MobRiderVariables.MobRiderData data) {
        // 10%概率触发隐身技能
        if (RANDOM.nextFloat() < 0.1f) {
            // 检查能量是否足够（消耗40点能量）
            if (RiderEnergyHandler.consumeRiderEnergy(mob, 40.0)) {
                // 给予隐身效果
                mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.INVISIBILITY,
                        600, // 30秒
                        0,
                        false,
                        false
                ));
            }
        }
    }

    /**
     * 处理通用技能（适用于所有装备）
     */
    private static void handleGenericAbilities(Mob mob, Entity target, MobRiderVariables.MobRiderData data) {
        // 当生命值较低时（低于30%），有概率使用防御技能
        if (mob.getHealth() / mob.getMaxHealth() < 0.3 && RANDOM.nextFloat() < 0.25f) {
            // 检查能量是否足够（消耗15点能量）
            if (RiderEnergyHandler.consumeRiderEnergy(mob, 15.0)) {
                // 给予伤害抗性
                mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                        400, // 20秒
                        1,
                        false,
                        false
                ));
            }
        }

        // 当目标距离较远时，有概率使用冲刺技能
        if (mob.distanceTo(target) > 5 && RANDOM.nextFloat() < 0.15f) {
            // 检查能量是否足够（消耗10点能量）
            if (RiderEnergyHandler.consumeRiderEnergy(mob, 10.0)) {
                // 向目标冲刺
                Vec3 direction = target.position().subtract(mob.position()).normalize();
                mob.push(direction.x * 1.5, 0.3, direction.z * 1.5);
            }
        }
    }

    /**
     * 在tick事件中检查并移除过期的分身
     */
    private static void checkAndRemoveExpiredClones(ServerLevel level) {
        long currentTime = level.getGameTime();

        for (Mob mob : level.getEntitiesOfClass(Mob.class, getWorldBounds(level), entity -> true)) {
            if (mob.getPersistentData().getBoolean("IsLemonClone")) {
                long removeAt = mob.getPersistentData().getLong("RemoveAt");
                if (currentTime >= removeAt && mob.isAlive()) {
                    // 播放消失效果
                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                            net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.2f);

                    // 移除分身
                    mob.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    /**
     * 生成香蕉能量实体
     */
    private static void spawnBananaEnergyEntity(Mob mob) {
        // 简化版实现，实际应该生成具体的能量实体
        // 这里只是播放音效和粒子效果
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.2f);

        // 创建减速效果区域
        AABB area = new AABB(
                mob.getX() - 3, mob.getY() - 3, mob.getZ() - 3,
                mob.getX() + 3, mob.getY() + 3, mob.getZ() + 3
        );

        // 修复：使用明确的getEntities方法重载
        List<Entity> entitiesInArea = mob.level().getEntities(mob, area,
                entity -> entity.isAlive() && !(entity instanceof Mob && RiderEnergyHandler.hasRiderEquipment((Mob) entity)));

        for (Entity entity : entitiesInArea) {
            // 给予缓慢效果
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                        400, // 20秒
                        2,
                        false,
                        false
                ));
            }
        }
    }
}