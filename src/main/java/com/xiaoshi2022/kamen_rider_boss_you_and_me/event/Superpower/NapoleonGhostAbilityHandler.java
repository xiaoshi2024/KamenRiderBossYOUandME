package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.NapoleonGhost.NapoleonGhostItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class NapoleonGhostAbilityHandler {
    
    // 战术指挥技能冷却计时器
    private static final Map<UUID, Integer> tacticsCooldown = new HashMap<>();
    // 战场激励技能冷却计时器
    private static final Map<UUID, Integer> inspirationCooldown = new HashMap<>();
    
    /* ---------------- 数值常量 ---------------- */
    private static final int STRENGTH_DURATION = 80; // 力量提升持续时间(tick)
    private static final int SPEED_DURATION = 80; // 速度提升持续时间(tick)
    private static final int RESISTANCE_DURATION = 80; // 抗性提升持续时间(tick)
    private static final int TACTICS_COOLDOWN = 200; // 战术指挥冷却时间(tick)
    private static final int INSPIRATION_COOLDOWN = 300; // 战场激励冷却时间(tick)
    private static final int COMMAND_RADIUS = 10; // 指挥半径(方块)
    private static final UUID HEALTH_BOOST_UUID = UUID.fromString("a7b2c9d1-e3f4-5a6b-7c8d-9e0f1a2b3c4d");
    private static final String HEALTH_BOOST_NAME = "NapoleonGhostHealthBoost";
    
    /* ---------------- 常驻被动 ---------------- */

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;

        if (isWearingNapoleonGhostArmor(player) && isNapoleonGhostActive(player)) {
            // 1. 领导力光环 - 提升周围友方生物的属性
            applyLeadershipAura(player);
            
            // 2. 战术指挥 - 周期性提升自己的战斗能力
            applyTacticalCommand(player);
            
            // 3. 战场激励 - 周期性恢复周围友方的生命值
            applyBattlefieldInspiration(player);
            
            // 4. 军事敏锐 - 提升视野和感知能力
            applyMilitaryAcumen(player);
            
            // 5. 生命值提升 - 作为指挥官的耐久力
            applyHealthBoost(player);
            
        } else {
            // 当玩家不再穿着盔甲或变身状态结束时，移除生命值提升
            removeHealthBoost(player);
        }
    }

    /* ---------------- 核心技能 ---------------- */
    
    /**
     * 领导力光环 - 提升周围友方生物的属性
     */
    private static void applyLeadershipAura(Player player) {
        // 为玩家自身提供基本的力量和速度提升
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, STRENGTH_DURATION, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPEED_DURATION, 0, false, false));
        
        // 为周围的友方生物提供增益效果
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.level().getEntitiesOfClass(Player.class, 
                player.getBoundingBox().inflate(COMMAND_RADIUS), 
                p -> p != player && !p.isSpectator() && p.isAlliedTo(player))
                .forEach(ally -> {
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, STRENGTH_DURATION / 2, 0, false, false));
                    ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPEED_DURATION / 2, 0, false, false));
                });
        }
    }
    
    /**
     * 战术指挥 - 周期性提升自己的战斗能力
     */
    private static void applyTacticalCommand(Player player) {
        UUID playerId = player.getUUID();
        int cooldown = tacticsCooldown.getOrDefault(playerId, 0);
        
        if (cooldown <= 0) {
            // 战术指挥激活：同时获得力量和抗性提升
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false)); // 更强的力量
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, false, false)); // 抗性提升
            
            // 播放战术指挥音效
            player.playNotifySound(SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6F, 1.2F);
            
            // 设置冷却时间
            tacticsCooldown.put(playerId, TACTICS_COOLDOWN);
        } else {
            // 减少冷却时间
            tacticsCooldown.put(playerId, cooldown - 1);
        }
    }
    
    /**
     * 战场激励 - 周期性恢复周围友方的生命值
     */
    private static void applyBattlefieldInspiration(Player player) {
        UUID playerId = player.getUUID();
        int cooldown = inspirationCooldown.getOrDefault(playerId, 0);
        
        if (cooldown <= 0) {
            // 恢复自己的生命值
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(4.0F); // 恢复2颗心
            }
            
            // 恢复周围友方的生命值
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.level().getEntitiesOfClass(Player.class, 
                    player.getBoundingBox().inflate(COMMAND_RADIUS), 
                    p -> p != player && !p.isSpectator() && p.isAlliedTo(player))
                    .forEach(ally -> {
                        if (ally.getHealth() < ally.getMaxHealth()) {
                            ally.heal(2.0F); // 恢复1颗心
                        }
                    });
            }
            
            // 播放激励音效
            player.playNotifySound(SoundEvents.ARMOR_EQUIP_DIAMOND, SoundSource.PLAYERS, 0.8F, 1.0F);
            
            // 设置冷却时间
            inspirationCooldown.put(playerId, INSPIRATION_COOLDOWN);
        } else {
            // 减少冷却时间
            inspirationCooldown.put(playerId, cooldown - 1);
        }
    }
    
    /**
     * 军事敏锐 - 提升视野和感知能力
     */
    private static void applyMilitaryAcumen(Player player) {
        // 夜视能力 - 提升在黑暗环境中的战斗能力
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, false, false));
        
        // 急迫效果 - 提升攻击速度
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, 0, false, false));
    }
    
    /* ---------------- 工具方法 ---------------- */
    
    /**
     * 检查玩家是否穿着拿破仑幽灵盔甲
     */
    public static boolean isWearingNapoleonGhostArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof NapoleonGhostItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof NapoleonGhostItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof NapoleonGhostItem;
    }
    
    /**
     * 检查玩家是否处于拿破仑幽灵变身状态
     */
    public static boolean isNapoleonGhostActive(Player player) {
        return player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .map(variables -> variables.isNapoleonGhostTransformed)
                .orElse(false);
    }
    
    /**
     * 应用拿破仑幽灵生命值提升
     */
    private static void applyHealthBoost(Player player) {
        // 保存当前生命值百分比
        float healthPercentage = player.getHealth() / player.getMaxHealth();
        
        AttributeInstance maxHealth = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            // 计算额外生命值提升 - 指挥官需要更强的生存能力
            double healthBoost = 37.0D; // 额外37点生命值
            
            // 移除旧的修改器（如果存在）
            maxHealth.removeModifier(HEALTH_BOOST_UUID);
            
            // 添加新的生命值提升修改器
            maxHealth.addTransientModifier(new AttributeModifier(
                    HEALTH_BOOST_UUID,
                    HEALTH_BOOST_NAME,
                    healthBoost,
                    AttributeModifier.Operation.ADDITION
            ));
            
            // 确保当前生命值按照百分比保持
            float newHealth = (float) (player.getMaxHealth() * healthPercentage);
            player.setHealth(newHealth);
        }
    }
    
    /**
     * 移除拿破仑幽灵生命值提升
     */
    private static void removeHealthBoost(Player player) {
        AttributeInstance maxHealth = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            // 保存当前生命值百分比
            float healthPercentage = player.getHealth() / player.getMaxHealth();
            
            // 移除生命值提升修改器
            maxHealth.removeModifier(HEALTH_BOOST_UUID);
            
            // 调整当前生命值以保持百分比
            float newHealth = (float) (player.getMaxHealth() * healthPercentage);
            player.setHealth(newHealth);
        }
    }
    
    // 存储玩家是否处于远程伤害减免状态
    private static final Map<UUID, Integer> rangedDamageReductionCooldown = new HashMap<>();
    private static final int RANGED_REDUCTION_DURATION = 60; // 效果持续60个tick
    private static final int RANGED_REDUCTION_COOLDOWN = 200; // 冷却时间200个tick

    // 激活远程伤害减免功能
    public static void activateRangedDamageReduction(ServerPlayer player) {
        UUID playerId = player.getUUID();
        
        // 检查玩家是否处于冷却中
        if (rangedDamageReductionCooldown.containsKey(playerId)) {
            int cooldown = rangedDamageReductionCooldown.get(playerId);
            if (cooldown > 0) {
                player.displayClientMessage(Component.literal("技能冷却中：" + (cooldown / 20) + "秒"), true);
                return;
            }
        }
        
        // 检查玩家是否穿着拿破仑Ghost盔甲
        if (!isWearingNapoleonGhostArmor(player) || !isNapoleonGhostActive(player)) {
            return;
        }
        
        // 激活远程伤害减免效果
        rangedDamageReductionCooldown.put(playerId, RANGED_REDUCTION_DURATION);
        player.displayClientMessage(Component.literal("远程伤害减免已激活！"), true);
        
        // 设置冷却时间
        rangedDamageReductionCooldown.put(playerId, RANGED_REDUCTION_COOLDOWN);
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 处理远程伤害减免冷却时间减少
            Iterator<Map.Entry<UUID, Integer>> iterator = rangedDamageReductionCooldown.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                int remaining = entry.getValue() - 1;
                if (remaining <= 0) {
                    iterator.remove();
                } else {
                    entry.setValue(remaining);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID playerId = player.getUUID();
            
            // 检查是否有远程伤害减免效果
            if (rangedDamageReductionCooldown.containsKey(playerId)) {
                int remaining = rangedDamageReductionCooldown.get(playerId);
                
                // 检查伤害来源是否是远程伤害
                DamageSource source = event.getSource();
                boolean isRangedDamage = source.getMsgId().equals("arrow") ||
                        source.getMsgId().equals("trident") || source.getMsgId().equals("snowball") ||
                        source.getMsgId().equals("egg") || source.getMsgId().equals("firework") ||
                        source.getMsgId().equals("fireball") || source.getMsgId().equals("fourze_rocket");
                
                if (isRangedDamage) {
                    // 减免伤害，最多减免40点
                    float damage = event.getAmount();
                    float reducedDamage = Math.max(0, damage - 40);
                    event.setAmount(reducedDamage);
                    
                    player.displayClientMessage(Component.literal("远程伤害减免已触发！"), true);
                }
            }
        }
    }
}