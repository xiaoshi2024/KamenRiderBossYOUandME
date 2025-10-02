package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class GiifuFeatureHandler {
    // 基础生命值（普通人类）
    private static final double BASE_HEALTH = 20.0D;
    // Giifu额外生命值
    private static final double GIIFU_EXTRA_HEALTH = 50.0D;
    // Giifu目标生命值
    private static final double GIIFU_TARGET_HEALTH = BASE_HEALTH + GIIFU_EXTRA_HEALTH; // 70点

    // Giifu攻击力倍数
    private static final int ATTACK_DAMAGE_MULTIPLIER = 3;
    // 基夫技能冷却时间（tick）
    private static final int GIIFU_SKILL_COOLDOWN = 300;
    private static final int GIIFU_SKILL_COOLDOWN_HUMAN = 400;

    // 存储玩家的技能冷却时间
    private static final Map<UUID, Integer> giifuSkillCooldowns = new HashMap<>();
    // 存储玩家是否已经应用了Giifu生命值加成
    private static final Map<UUID, Boolean> giifuHealthApplied = new HashMap<>();
    // 存储玩家的原始生命值上限
    private static final Map<UUID, Double> originalMaxHealth = new HashMap<>();

    // 每秒检查玩家状态并应用Giifu特性
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level level = player.level();

        // 只在服务器端和玩家活着的时候执行
        if (level.isClientSide() || player.isDeadOrDying()) {
            return;
        }

        // 获取玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());

        // 检查玩家是否为Giifu且未变身
        boolean isGiifu = variables.isGiifu;
        boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);

        // 根据状态调整生命值上限
        if (isGiifu && !isTransformed) {
            // 应用缓慢的生命恢复效果
            if (player.getHealth() < player.getMaxHealth() * 0.5) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, false));
            }

            // 应用Giifu生命值加成
            applyGiifuHealthBonus(player, variables);

            // 更新冷却时间
            updateCooldowns();
        } else {
            // 如果不是Giifu或已变身，恢复默认生命值上限
            removeGiifuHealthBonus(player, variables);
        }
    }

    /**
     * 应用Giifu生命值加成
     */
    private static void applyGiifuHealthBonus(Player player, KRBVariables.PlayerVariables variables) {
        UUID playerUUID = player.getUUID();
        double currentMaxHealth = player.getMaxHealth();

        // 如果还没有应用Giifu生命值加成，或者当前生命值不是目标值
        if (!giifuHealthApplied.getOrDefault(playerUUID, false) || Math.abs(currentMaxHealth - GIIFU_TARGET_HEALTH) > 0.1) {
            // 保存原始生命值（只在第一次应用时保存）
            if (!originalMaxHealth.containsKey(playerUUID)) {
                originalMaxHealth.put(playerUUID, currentMaxHealth);
                System.out.println("Saved original max health: " + currentMaxHealth + " for player " + playerUUID);
            }

            // 设置新的生命值上限
            player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(GIIFU_TARGET_HEALTH);

            // 恢复部分生命值（如果当前生命值较低）
            if (player.getHealth() < GIIFU_TARGET_HEALTH) {
                float healthToAdd = Math.min(10.0F, (float) (GIIFU_TARGET_HEALTH - player.getHealth()));
                player.setHealth(player.getHealth() + healthToAdd);
                System.out.println("Restored health: " + healthToAdd + ", current health: " + player.getHealth());
            }

            // 更新变量
            variables.baseMaxHealth = GIIFU_TARGET_HEALTH;
            variables.syncPlayerVariables(player);
            giifuHealthApplied.put(playerUUID, true);

            System.out.println("Applied Giifu health bonus: " + currentMaxHealth + " -> " + GIIFU_TARGET_HEALTH + " for player " + player.getName().getString());
        }
    }

    /**
     * 移除Giifu生命值加成
     */
    private static void removeGiifuHealthBonus(Player player, KRBVariables.PlayerVariables variables) {
        UUID playerUUID = player.getUUID();

        // 如果已经应用了Giifu生命值加成
        if (giifuHealthApplied.getOrDefault(playerUUID, false)) {
            // 恢复原始生命值或基础生命值
            double originalHealth = originalMaxHealth.getOrDefault(playerUUID, BASE_HEALTH);
            double currentMaxHealth = player.getMaxHealth();

            // 只有在当前生命值上限接近Giifu目标值的情况下才恢复
            if (Math.abs(currentMaxHealth - GIIFU_TARGET_HEALTH) < 1.0) {
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(originalHealth);

                // 确保当前生命值不超过新的上限
                if (player.getHealth() > originalHealth) {
                    player.setHealth((float) originalHealth);
                }

                // 更新变量
                variables.baseMaxHealth = originalHealth;
                variables.syncPlayerVariables(player);

                System.out.println("Removed Giifu health bonus: " + currentMaxHealth + " -> " + originalHealth + " for player " + player.getName().getString());
            }

            // 清理状态
            giifuHealthApplied.remove(playerUUID);
            originalMaxHealth.remove(playerUUID);
        }
    }

    // 更新技能冷却时间
    private static void updateCooldowns() {
        List<UUID> toRemove = new java.util.ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : giifuSkillCooldowns.entrySet()) {
            int remainingCooldown = entry.getValue() - 1;
            if (remainingCooldown <= 0) {
                toRemove.add(entry.getKey());
            } else {
                entry.setValue(remainingCooldown);
            }
        }
        for (UUID uuid : toRemove) {
            giifuSkillCooldowns.remove(uuid);
        }
    }

    // 检查玩家是否可以使用Giifu技能
    private static boolean canUseGiifuSkill(Player player) {
        return !giifuSkillCooldowns.containsKey(player.getUUID());
    }

    // 激活Giifu技能并设置冷却时间
    public static void activateGiifuSkill(Player player) {
        if (!player.level().isClientSide() && canUseGiifuSkill(player)) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean isGiifu = variables.isGiifu;
            boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);

            if (isGiifu) {
                // 根据形态设置不同的冷却时间
                int cooldown = isTransformed ? GIIFU_SKILL_COOLDOWN : GIIFU_SKILL_COOLDOWN_HUMAN;

                // 应用Giifu技能效果
                applyGiifuSkillEffects(player);
                // 设置冷却时间
                giifuSkillCooldowns.put(player.getUUID(), cooldown);
                // 通知玩家技能已激活
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("基夫之力已激活！"), true);
            } else {
                // 通知玩家无法使用此技能
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("只有基夫可以使用此技能！"), true);
            }
        } else if (!canUseGiifuSkill(player)) {
            // 通知玩家技能冷却中
            int cooldownSeconds = giifuSkillCooldowns.get(player.getUUID()) / 20;
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("技能冷却中，剩余 " + cooldownSeconds + " 秒！"), true);
        }
    }

    // 应用Giifu技能效果
    private static void applyGiifuSkillEffects(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // 给玩家施加增益效果
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 2));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0));

        // 对周围的敌人施加虚弱效果
        AABB area = new AABB(playerPos).inflate(6.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            // 不影响玩家自己和其他Giifu种族
            if (entity != player && isHostileEntity(entity, player)) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 1));
            }
        }
    }

    // 增强Giifu的攻击力
    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event) {
        Entity source = event.getSource().getEntity();
        // 检查攻击者是否为玩家且是Giifu且未变身
        if (source instanceof Player player) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean isGiifu = variables.isGiifu;
            boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);

            if (isGiifu && !isTransformed) {
                // 记录原始伤害值
                event.getEntity().getPersistentData().putDouble("OriginalDamage", event.getAmount());
            }
        }
    }

    // 处理伤害计算（增强攻击力和减免伤害）
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();

        // 处理Giifu的攻击增强
        if (source.getEntity() instanceof Player attacker) {
            KRBVariables.PlayerVariables attackerVars = attacker.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean attackerIsGiifu = attackerVars.isGiifu;
            boolean attackerIsTransformed = PlayerDeathHandler.hasTransformationArmor(attacker);

            if (attackerIsGiifu && !attackerIsTransformed) {
                // 应用攻击力倍数
                event.setAmount(event.getAmount() * ATTACK_DAMAGE_MULTIPLIER);
            }
        }

        // 处理Giifu的伤害减免
        if (target instanceof Player player) {
            KRBVariables.PlayerVariables playerVars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean isGiifu = playerVars.isGiifu;
            boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);

            // 即使是Giifu玩家，也应该受到伤害
            if (isGiifu && !isTransformed) {
                // 伤害减免10%
                event.setAmount(event.getAmount() * 0.9F);
            }

            // 基夫玩家被攻击时，附近的基夫门徒会帮助攻击敌方
            if (isGiifu && source.getEntity() instanceof LivingEntity attacker) {
                // 确保攻击者不是基夫同类或基夫人类态
                if (!isGiifuAlly(attacker)) {
                    // 寻找附近的基夫门徒并让他们帮助攻击
                    callGiifuAlliesForHelp(player, attacker);
                }
            }
        }
    }

    /**
     * 设置玩家的基夫状态
     * @param player 要设置状态的玩家
     * @param isGiifu 是否为基夫
     */
    public static void setGiifuState(Player player, boolean isGiifu) {
        if (player != null && !player.level().isClientSide()) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            boolean wasGiifu = variables.isGiifu;
            variables.isGiifu = isGiifu;
            variables.syncPlayerVariables(player);

            // 状态变化时立即处理生命值
            if (isGiifu && !wasGiifu) {
                // 成为Giifu时立即应用生命值加成
                applyGiifuHealthBonus(player, variables);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("你已成为基夫！获得额外生命值。"), true);
            } else if (!isGiifu && wasGiifu) {
                // 不再是Giifu时立即移除生命值加成
                removeGiifuHealthBonus(player, variables);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("基夫之力已消失！"), true);
            }
        }
    }

    /**
     * 获取玩家的基夫状态
     * @param player 要查询的玩家
     * @return 是否为基夫
     */
    public static boolean isPlayerGiifu(Player player) {
        if (player != null) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            return variables.isGiifu;
        }
        return false;
    }

    // 基夫玩家被攻击时调用附近的基夫门徒帮助攻击
    private static void callGiifuAlliesForHelp(Player player, LivingEntity attacker) {
        Level level = player.level();
        if (level.isClientSide()) {
            return; // 只在服务器端执行
        }

        // 检查攻击者是否是没有基夫标签的玩家
        boolean shouldAttack = true;
        if (attacker instanceof Player) {
            KRBVariables.PlayerVariables attackerVars = ((Player)attacker).getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            shouldAttack = !attackerVars.isGiifu; // 只有当攻击者没有基夫标签时才攻击
        }

        if (!shouldAttack) {
            return; // 如果攻击者有基夫标签，则不调用基夫门徒帮助
        }

        // 搜索范围：玩家周围10格内的实体
        AABB searchArea = new AABB(player.blockPosition()).inflate(10.0D);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchArea);

        for (LivingEntity entity : nearbyEntities) {
            // 检查是否是基夫门徒（通过类名识别）
            if (isGiifuDisciple(entity) && !entity.isDeadOrDying()) {
                // 设置基夫门徒攻击目标为攻击者
                if (entity instanceof net.minecraft.world.entity.monster.Monster monster) {
                    if (monster.getTarget() == null || monster.getTarget() != attacker) {
                        monster.setTarget(attacker);
                        // 可以给基夫门徒一个短暂的速度提升，使他们更快地接近敌人
                        monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, true, false));
                    }
                }
            }
        }
    }

    // 检查实体是否是基夫门徒
    private static boolean isGiifuDisciple(LivingEntity entity) {
        // 通过类名识别基夫门徒
        try {
            String className = entity.getClass().getName().toLowerCase();
            // 确保不是玩家实体，且是基夫相关生物
            return !(entity instanceof Player) &&
                    (className.contains("giifu_human") || className.contains("gifftarian"));
        } catch (Exception e) {
            return false;
        }
    }

    // 检查实体是否是基夫同类或基夫人类态
    private static boolean isGiifuAlly(LivingEntity entity) {
        // 如果是玩家，检查是否是基夫
        if (entity instanceof Player) {
            KRBVariables.PlayerVariables playerVars = ((Player)entity).getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            return playerVars.isGiifu;
        }

        // 检查是否是基夫相关生物
        try {
            String className = entity.getClass().getName().toLowerCase();
            return className.contains("giifu_human") || className.contains("gifftarian") || className.contains("storious");
        } catch (Exception e) {
            return false;
        }
    }

    // 检查实体是否是敌对实体（排除玩家自己和Giifu生物）
    private static boolean isHostileEntity(LivingEntity entity, Player player) {
        if (entity == player) {
            return false;
        }

        // 对玩家特殊处理：只将敌对玩家视为敌对目标
        if (entity instanceof Player) {
            Player targetPlayer = (Player) entity;
            KRBVariables.PlayerVariables targetVars = targetPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            // 如果目标玩家是Giifu，不视为敌对目标
            if (targetVars.isGiifu) {
                return false;
            }
            return !player.getTeam().equals(targetPlayer.getTeam()) && player.canAttack(targetPlayer);
        }

        // 检查是否为敌对生物，但保持基夫门徒不会一开始就设定为敌对玩家
        try {
            String className = entity.getClass().getName().toLowerCase();
            if (className.contains("giifu_human") || className.contains("gifftarian") || className.contains("storious")) {
                return false; // 基夫相关生物不视为敌对
            }
        } catch (Exception e) {
            // 如果出现异常，继续执行
        }

        return !entity.getType().getCategory().isFriendly() && entity.canChangeDimensions() && player.canAttack(entity);
    }

    /**
     * 清理玩家数据（当玩家退出游戏时调用）
     * @param player 退出的玩家
     */
    public static void cleanupPlayerData(Player player) {
        if (player != null) {
            UUID playerUUID = player.getUUID();
            giifuSkillCooldowns.remove(playerUUID);
            giifuHealthApplied.remove(playerUUID);
            originalMaxHealth.remove(playerUUID);
        }
    }

    /**
     * 获取玩家的技能冷却时间（秒）
     * @param player 要查询的玩家
     * @return 剩余冷却时间（秒），0表示没有冷却
     */
    public static int getSkillCooldownSeconds(Player player) {
        if (player != null && giifuSkillCooldowns.containsKey(player.getUUID())) {
            return giifuSkillCooldowns.get(player.getUUID()) / 20;
        }
        return 0;
    }

    /**
     * 调试方法：获取玩家生命值状态
     */
    public static String getHealthStatus(Player player) {
        if (player == null) return "Player is null";

        UUID playerUUID = player.getUUID();
        boolean applied = giifuHealthApplied.getOrDefault(playerUUID, false);
        double original = originalMaxHealth.getOrDefault(playerUUID, BASE_HEALTH);
        double current = player.getMaxHealth();
        boolean isGiifu = isPlayerGiifu(player);
        boolean isTransformed = PlayerDeathHandler.hasTransformationArmor(player);

        return String.format("Giifu: %s, Transformed: %s, HealthApplied: %s, Original: %.1f, Current: %.1f, Target: %.1f",
                isGiifu, isTransformed, applied, original, current, GIIFU_TARGET_HEALTH);
    }
}