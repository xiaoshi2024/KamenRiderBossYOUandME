package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.marika.Marika;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class MarikaAbilityHandler {
    private static final Random random = new Random();
    private static final int HEAL_INTERVAL = 100; // 5秒 = 100tick
    private static final int MIN_HEALTH_FOR_HEAL = 1; // 最低生命值阈值
    private static final double SENSORY_ENHANCEMENT_COST = 50.0; // 感官加强技能消耗的能量
    private static final int SENSORY_ENHANCEMENT_DURATION = 600; // 感官加强持续时间（tick），30秒
    private static final String SENSORY_ENHANCEMENT_KEY = "MarikaSensoryEnhancement";
    private static final String SENSORY_ENHANCEMENT_EXPIRY_KEY = "MarikaSensoryEnhancementExpiry";

    /**
     * 检查玩家是否装备了全套Marika装甲
     */
    private static boolean isFullMarikaArmorEquipped(ServerPlayer player) {
        return Marika.isArmorEquipped(player, ModItems.MARIKA_HELMET.get()) &&
               Marika.isArmorEquipped(player, ModItems.MARIKA_CHESTPLATE.get()) &&
               Marika.isArmorEquipped(player, ModItems.MARIKA_LEGGINGS.get());
    }
    
    // 给玩家周围6格内的实体添加高亮标记
    private static void highlightNearbyEntities(ServerPlayer player) {
        // 获取玩家位置
        Vec3 playerPos = player.position();
        double radius = 6.0;
        
        // 查找附近的实体（包括玩家）
        List<Entity> nearbyEntities = player.level().getEntities(player, 
                new AABB(
                        playerPos.x - radius, playerPos.y - radius, playerPos.z - radius,
                        playerPos.x + radius, playerPos.y + radius, playerPos.z + radius
                ),
                entity -> true // 高亮所有实体，包括玩家
        );
        
        // 给每个实体添加发光效果作为高亮标记
        for (Entity entity : nearbyEntities) {
            // 直接给实体添加发光效果
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING, 
                        SENSORY_ENHANCEMENT_DURATION, 
                        0, 
                        false, 
                        false
                ));
            }
        }
    }

    /**
     * 每tick检查玩家状态，触发桃子治愈能力
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player && event.phase == TickEvent.Phase.END) {
            // 检查是否装备全套Marika装甲且玩家存活
            if (isFullMarikaArmorEquipped(player) && player.isAlive()) {
                // 每HEAL_INTERVAL tick触发一次治愈
                if (player.tickCount % HEAL_INTERVAL == 0) {
                    // 只在生命值未满时触发
                    if (player.getHealth() < player.getMaxHealth()) {
                        player.heal(1.0F); // 恢复1点生命值
                    }
                }
            }
        }
    }

    /**
     * 当玩家受到伤害时，有几率触发额外治愈效果
     */
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 检查是否装备全套Marika装甲且玩家存活
            if (isFullMarikaArmorEquipped(player) && player.isAlive()) {
                // 20%几率触发
                if (random.nextDouble() < 0.2) {
                    // 恢复3点生命值
                    player.heal(3.0F);
                    // 给予3秒的生命恢复I效果，但只有当玩家没有生命恢复效果或效果等级低于I时才添加
                    if (!player.hasEffect(MobEffects.REGENERATION) || player.getEffect(MobEffects.REGENERATION).getAmplifier() < 0) {
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false));
                    }
                }
            }
        }
    }

    /**
     * 激活感官加强技能
     * @param player 使用技能的玩家
     */
    public static void activateSensoryEnhancement(ServerPlayer player) {
        // 检查是否装备全套Marika装甲
        if (!isFullMarikaArmorEquipped(player)) {
            player.sendSystemMessage(Component.literal("请装备全套玛丽卡装甲！").withStyle(ChatFormatting.RED));
            return;
        }
        
        // 检查技能是否在冷却中
        if (isSensoryEnhancementActive(player)) {
            long expiryTime = player.getPersistentData().getLong(SENSORY_ENHANCEMENT_EXPIRY_KEY);
            long remainingTime = expiryTime - player.level().getGameTime();
            int remainingSeconds = (int) Math.ceil(remainingTime / 20.0);
            player.sendSystemMessage(Component.literal("感官加强技能正在冷却中，还剩 " + remainingSeconds + " 秒！").withStyle(ChatFormatting.RED));
            return;
        }
        
        // 消耗骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(player, SENSORY_ENHANCEMENT_COST)) {
            player.sendSystemMessage(Component.literal("骑士能量不足，需要 " + (int)SENSORY_ENHANCEMENT_COST + " 点能量！").withStyle(ChatFormatting.RED));
            return;
        }
        
        // 设置感官加强状态和过期时间
        player.getPersistentData().putBoolean(SENSORY_ENHANCEMENT_KEY, true);
        player.getPersistentData().putLong(SENSORY_ENHANCEMENT_EXPIRY_KEY, player.level().getGameTime() + SENSORY_ENHANCEMENT_DURATION);
        
        // 应用效果
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, SENSORY_ENHANCEMENT_DURATION, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SENSORY_ENHANCEMENT_DURATION, 1, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, SENSORY_ENHANCEMENT_DURATION, 1, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, SENSORY_ENHANCEMENT_DURATION, 0, false, false));
        
        // 给周围6格内的实体添加高亮标记
        highlightNearbyEntities(player);
        
        // 播放技能激活音效
        player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 1.0f, 1.2f);
        
        // 发送技能激活消息
        player.sendSystemMessage(Component.literal("感官加强已激活！获得夜视、速度提升、跳跃提升和防御提升效果，持续30秒！").withStyle(ChatFormatting.GREEN));
    }
    
    /**
     * 检查感官加强技能是否处于激活状态
     * @param player 玩家
     * @return 是否处于激活状态
     */
    private static boolean isSensoryEnhancementActive(ServerPlayer player) {
        if (!player.getPersistentData().contains(SENSORY_ENHANCEMENT_KEY)) {
            return false;
        }
        
        if (!player.getPersistentData().getBoolean(SENSORY_ENHANCEMENT_KEY)) {
            return false;
        }
        
        // 检查是否过期
        if (player.getPersistentData().contains(SENSORY_ENHANCEMENT_EXPIRY_KEY)) {
            long expiryTime = player.getPersistentData().getLong(SENSORY_ENHANCEMENT_EXPIRY_KEY);
            if (expiryTime <= player.level().getGameTime()) {
                // 技能已过期，清除状态
                player.getPersistentData().putBoolean(SENSORY_ENHANCEMENT_KEY, false);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 监听玩家tick事件，检查感官加强效果是否过期
     */
    @SubscribeEvent
    public static void onPlayerTickEffectCheck(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player && event.phase == TickEvent.Phase.END) {
            // 检查是否装备全套Marika装甲
            if (isFullMarikaArmorEquipped(player)) {
                // 检查感官加强是否过期
                if (player.getPersistentData().contains(SENSORY_ENHANCEMENT_KEY) && 
                    player.getPersistentData().contains(SENSORY_ENHANCEMENT_EXPIRY_KEY)) {
                    
                    boolean isActive = player.getPersistentData().getBoolean(SENSORY_ENHANCEMENT_KEY);
                    long expiryTime = player.getPersistentData().getLong(SENSORY_ENHANCEMENT_EXPIRY_KEY);
                    
                    // 如果技能已激活但已过期，清除状态
                    if (isActive && expiryTime <= player.level().getGameTime()) {
                        player.getPersistentData().putBoolean(SENSORY_ENHANCEMENT_KEY, false);
                        
                        // 发送技能结束消息
                        player.sendSystemMessage(Component.literal("感官加强效果已结束！").withStyle(ChatFormatting.YELLOW));
                    }
                }
            }
        }
    }
}