package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class RoidmudeHandler {
    // 定义UUID用于属性修改器（确保每次都是同一个修改器）
    private static final UUID RoidMUDE_HEALTH_BOOST_MODIFIER_ID = UUID.fromString("c9d8e7f6-5a4b-3c2d-1e0f-9a8b7c6d5e4f");
    private static final String RoidMUDE_HEALTH_BOOST_MODIFIER_NAME = "Roidmude Health Boost";

    // Roidmude的额外生命值加成
    private static final double RoidMUDE_HEALTH_BOOST = 50.0; // 基础额外生命值
    private static final double RoidMUDE_EVOLVED_HEALTH_BOOST = 100.0; // 进化后额外生命值

    // Roidmude的核心形态冷却时间（毫秒）
    private static final long CORE_FORM_COOLDOWN = 60000; // 1分钟

    // 记录当前处于核心形态的玩家
    private static final Set<UUID> CORE_FORM_PLAYERS = new HashSet<>();
    // 记录玩家进入核心形态的时间
    private static final Set<UUID> DEAD_PLAYERS = new HashSet<>();

    /* ------ 死亡时处理核心形态 ------ */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            DEAD_PLAYERS.add(player.getUUID());
            
            // 检查玩家是否是Roidmude
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                if (variables.isRoidmude) {
                    // 进入核心形态
                    CORE_FORM_PLAYERS.add(player.getUUID());
                    System.out.println("RoidmudeHandler: Player " + player.getGameProfile().getName() + " entered core form");
                }
            });
        }
    }

    /* ------ 重生后处理核心形态 ------ */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        DEAD_PLAYERS.remove(player.getUUID());
        
        // 检查玩家是否是Roidmude核心形态
        if (CORE_FORM_PLAYERS.contains(player.getUUID())) {
            // 退出核心形态
            CORE_FORM_PLAYERS.remove(player.getUUID());
            System.out.println("RoidmudeHandler: Player " + player.getGameProfile().getName() + " exited core form");
        }
    }

    /* ------ 主 Tick ------ */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide()) return;
        
        // 死亡期间直接 return
        if (DEAD_PLAYERS.contains(player.getUUID())) return;

        // 获取或创建玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());

        // 处理Roidmude的特殊能力
        if (variables.isRoidmude) {
            handleRoidmudeAbilities(player, variables);
        }

        // 同步变量
        variables.syncPlayerVariables(player);
    }

    /**
     * 处理Roidmude的特殊能力
     */
    private static void handleRoidmudeAbilities(Player player, KRBVariables.PlayerVariables variables) {
        Level level = player.level();
        
        // 处理生命值加成
        handleHealthBoost(player, variables);
        
        // 处理进化机制
        handleEvolution(player, variables);
        
        // 处理核心形态
        handleCoreForm(player, variables);
    }

    /**
     * 处理Roidmude的生命值加成
     */
    private static void handleHealthBoost(Player player, KRBVariables.PlayerVariables variables) {
        // 获取玩家的最大生命值属性
        AttributeInstance maxHealthAttribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealthAttribute == null) return;

        // 移除旧的修改器
        maxHealthAttribute.removeModifier(RoidMUDE_HEALTH_BOOST_MODIFIER_ID);

        // 根据Roidmude状态添加生命值加成
        double healthBoost = variables.isRoidmudeEvolved ? RoidMUDE_EVOLVED_HEALTH_BOOST : RoidMUDE_HEALTH_BOOST;

        // 添加修改器来增加最大生命值
        maxHealthAttribute.addTransientModifier(
                new AttributeModifier(
                        RoidMUDE_HEALTH_BOOST_MODIFIER_ID,
                        RoidMUDE_HEALTH_BOOST_MODIFIER_NAME,
                        healthBoost,
                        AttributeModifier.Operation.ADDITION
                )
        );

        // 确保当前生命值也相应调整
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /**
     * 处理Roidmude的进化机制
     */
    private static void handleEvolution(Player player, KRBVariables.PlayerVariables variables) {
        // 这里可以实现Roidmude的进化机制，例如通过学习人类情绪来进化
        // 目前简单实现为通过命令手动进化
    }

    /**
     * 处理Roidmude的核心形态
     */
    private static void handleCoreForm(Player player, KRBVariables.PlayerVariables variables) {
        // 检查玩家是否处于核心形态
        if (CORE_FORM_PLAYERS.contains(player.getUUID())) {
            // 核心形态的特殊效果
            // 例如：无敌、缓慢恢复生命值等
            
            // 每10tick恢复1点生命值
            if (player.tickCount % 10 == 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0F);
            }
            
            // 核心形态的粒子效果
            if (player.tickCount % 5 == 0) {
                spawnCoreFormParticles(player);
            }
        }
    }

    /**
     * 处理Roidmude的重加速能力
     */
    public static void handleHeavyAcceleration(Player player, KRBVariables.PlayerVariables variables) {
        // 检查玩家是否有重加速能力
        if (variables.isRoidmude) {
            // 检查骑士能量是否足够
            double requiredEnergy = 20.0; // 重加速消耗的能量
            if (variables.riderEnergy >= requiredEnergy) {
                // 消耗骑士能量
                variables.riderEnergy -= requiredEnergy;
                variables.syncPlayerVariables(player);
                
                // 触发重加速效果
                triggerHeavyAcceleration(player);
            } else {
                // 能量不足提示
                player.displayClientMessage(Component.literal("骑士能量不足，无法激活重加速！"), true);
            }
        }
    }

    /**
     * 触发重加速效果
     */
    private static void triggerHeavyAcceleration(Player player) {
        Level level = player.level();
        Vec3 center = player.position();
        
        // 重加速影响范围
        double range = 10.0;
        AABB area = new AABB(
                center.x - range, center.y - range, center.z - range,
                center.x + range, center.y + range, center.z + range
        );
        
        // 对范围内的生物应用重加速效果
        for (Entity entity : level.getEntitiesOfClass(Entity.class, area)) {
            if (entity != player && entity instanceof LivingEntity livingEntity) {
                // 检查目标是否为机械变异体
                boolean isTargetRoidmude = false;
                
                // 检查玩家是否为机械变异体
                if (livingEntity instanceof Player targetPlayer) {
                    isTargetRoidmude = isRoidmude(targetPlayer);
                }
                // 检查实体是否为机械变异体（如Brain Roidmude）
                else if (entity instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Roidmude.BrainRoidmudeEntity) {
                    isTargetRoidmude = true;
                }
                
                // 只对非机械变异体应用重加速效果
                if (!isTargetRoidmude) {
                    // 应用重加速效果：减慢生物的移动速度
                    // 1. 添加缓慢效果
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3, true, false, true));
                    // 2. 添加挖掘缓慢效果
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 3, true, false, true));
                    // 3. 添加攻击缓慢效果
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, true, false, true));
                    
                    // 显示重加速粒子效果
                    if (level.random.nextDouble() < 0.6) {
                        level.addParticle(
                                ParticleTypes.CLOUD,
                                entity.getX() + (level.random.nextDouble() - 0.5) * 2,
                                entity.getY() + level.random.nextDouble() * 2,
                                entity.getZ() + (level.random.nextDouble() - 0.5) * 2,
                                0.0D, 0.0D, 0.0D
                        );
                    }
                    
                    // 对玩家显示重加速提示
                    if (livingEntity instanceof Player targetPlayer) {
                        targetPlayer.displayClientMessage(Component.literal("你受到了重加速的影响！"), true);
                    }
                }
            }
        }
        
        // 对Roidmude玩家显示重加速激活提示
        player.displayClientMessage(Component.literal("重加速已激活！周围的非机械变异体行动变得缓慢。"), true);
        
        // 播放重加速音效（如果有）
        // 这里可以添加音效播放代码
    }

    /**
     * 生成核心形态的粒子效果
     */
    private static void spawnCoreFormParticles(Player player) {
        Level level = player.level();
        Vec3 center = player.position().add(0, 1, 0);
        
        // 生成数字状能量体的核心形态粒子
        for (int i = 0; i < 5; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;
            
            level.addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    center.x + offsetX,
                    center.y + offsetY,
                    center.z + offsetZ,
                    0.0D, 0.0D, 0.0D
            );
        }
    }

    /**
     * 检查玩家是否是Roidmude
     */
    public static boolean isRoidmude(Player player) {
        return player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .map(variables -> variables.isRoidmude)
                .orElse(false);
    }

    /**
     * 检查玩家是否是进化后的Roidmude
     */
    public static boolean isEvolvedRoidmude(Player player) {
        return player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .map(variables -> variables.isRoidmude && variables.isRoidmudeEvolved)
                .orElse(false);
    }

    /**
     * 检查玩家是否处于核心形态
     */
    public static boolean isInCoreForm(Player player) {
        return CORE_FORM_PLAYERS.contains(player.getUUID());
    }
}
