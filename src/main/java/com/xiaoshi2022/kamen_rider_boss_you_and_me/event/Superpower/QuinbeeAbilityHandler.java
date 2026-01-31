package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.quinbee.QuinbeeItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 假面骑士Queen Bee能力处理器
 * 实现飞行能力和针刺苦无攻击
 */
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class QuinbeeAbilityHandler {

    /* ====== 数值常量 ====== */
    private static final int FLIGHT_COOLDOWN = 300; // 飞行技能冷却（15秒）
    private static final int NEEDLE_KUNAI_COOLDOWN = 20; // 针刺苦无冷却（1秒）
    private static final double ENERGY_COST_FLIGHT = 30.0; // 飞行技能消耗的骑士能量
    private static final double ENERGY_COST_NEEDLE = 5.0; // 针刺苦无消耗的骑士能量
    private static final int FLIGHT_DURATION = 600; // 飞行持续时间（30秒）

    /* ====== 主 Tick：处理持续效果和冷却 ====== */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        
        // 检查是否穿着Queen Bee盔甲
            boolean isQuinbee = QuinbeeItem.isFullArmorEquipped(sp);
        
        // 处理冷却时间
        handleCooldowns(variables, sp.level().getGameTime());
        
        // 更新套装基础效果
        updateBaseEffects(sp, variables, isQuinbee);
    }

    /* ====== 能力触发方法（可被网络包调用） ====== */

    /**
     * 飞行能力：V键触发，使玩家获得飞行能力
     */
    public static void tryFlight(ServerPlayer sp) {
        if (!QuinbeeItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerControlled(sp)) return;
        
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();
        
        // 检查冷却时间
        if (variables.quinbee_flight_cooldown > currentTime) {
            long remaining = (variables.quinbee_flight_cooldown - currentTime) / 20;
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("技能冷却中，还需 " + remaining + " 秒"));
            return;
        }
        
        // 检查骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, ENERGY_COST_FLIGHT)) {
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("骑士能量不足，需要 " + (int)ENERGY_COST_FLIGHT + " 点能量"));
            return;
        }
        
        // 设置冷却时间
        variables.quinbee_flight_cooldown = currentTime + FLIGHT_COOLDOWN;
        variables.quinbee_flight_end_time = currentTime + FLIGHT_DURATION;
        variables.syncPlayerVariables(sp);
        
        // 赋予飞行能力
        sp.getAbilities().flying = true;
        sp.getAbilities().mayfly = true;
        sp.onUpdateAbilities();
        
        // 播放技能音效
        playSound(sp, SoundEvents.ELYTRA_FLYING, 1.0f, 1.2f);
    }

    /**
     * 针刺苦无：鼠标右键触发，发射针刺苦无攻击敌人
     */
    public static void tryNeedleKunai(ServerPlayer sp) {
        if (!QuinbeeItem.isFullArmorEquipped(sp) || !sp.isAlive() || isPlayerControlled(sp)) return;
        
        KRBVariables.PlayerVariables variables = sp.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        long currentTime = sp.level().getGameTime();
        
        // 检查冷却时间
        if (variables.quinbee_needle_kunai_cooldown > currentTime) return;
        
        // 检查骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(sp, ENERGY_COST_NEEDLE)) return;
        
        // 设置冷却时间
        variables.quinbee_needle_kunai_cooldown = currentTime + NEEDLE_KUNAI_COOLDOWN;
        variables.syncPlayerVariables(sp);
        
        // 发射针刺苦无
        spawnNeedleKunai(sp);
        
        // 播放技能音效
        playSound(sp, SoundEvents.ARROW_SHOOT, 1.0f, 1.2f);
    }

    /* ====== 辅助方法 ====== */

    /**
      * 检查玩家是否被控（有控制类负面效果）
      */
     private static boolean isPlayerControlled(ServerPlayer player) {
         // 检查玩家是否有以下控制类负面效果
         return player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || // 缓慢
                player.hasEffect(MobEffects.BLINDNESS) || // 失明
                player.hasEffect(MobEffects.CONFUSION) || // 反胃
                player.hasEffect(MobEffects.POISON) || // 中毒
                player.hasEffect(MobEffects.WITHER) || // 凋零
                player.hasEffect(MobEffects.WEAKNESS); // 虚弱
     }

    // 处理冷却时间
    private static void handleCooldowns(KRBVariables.PlayerVariables variables, long currentTime) {
        // 不需要特别处理，冷却时间在触发技能时设置
    }

    // 更新基础效果
    private static void updateBaseEffects(ServerPlayer sp, KRBVariables.PlayerVariables variables, boolean isQuinbee) {
        if (isQuinbee) {
            // 检查飞行持续时间
            if (variables.quinbee_flight_end_time > 0 && sp.level().getGameTime() > variables.quinbee_flight_end_time) {
                // 飞行时间结束，禁用飞行
                sp.getAbilities().flying = false;
                sp.getAbilities().mayfly = false;
                sp.onUpdateAbilities();
                variables.quinbee_flight_end_time = 0;
                variables.syncPlayerVariables(sp);
            }
            
            // 添加Aguilera骑士的初始buff效果
            applyAguileraBuffs(sp);
        } else {
            // 未穿着Queen Bee盔甲，重置飞行状态
            if (variables.quinbee_flight_end_time > 0) {
                sp.getAbilities().flying = false;
                sp.getAbilities().mayfly = false;
                sp.onUpdateAbilities();
                variables.quinbee_flight_end_time = 0;
                variables.syncPlayerVariables(sp);
            }
        }
    }
    
    // 应用Aguilera骑士的初始buff效果
    private static void applyAguileraBuffs(ServerPlayer player) {
        if (!player.level().isClientSide()) {
            // 添加跳跃提升效果（与飞行能力相关）
            MobEffectInstance jumpEffect = player.getEffect(MobEffects.JUMP);
            if (jumpEffect == null || jumpEffect.getAmplifier() < 1) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.JUMP,
                        400,
                        1,
                        false,
                        false
                ));
            }
            
            // 添加攻击力提升效果
            MobEffectInstance strengthEffect = player.getEffect(MobEffects.DAMAGE_BOOST);
            if (strengthEffect == null || strengthEffect.getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        400,
                        0,
                        false,
                        false
                ));
            }
            
            // 添加生命恢复效果（体现赎罪意志）
            MobEffectInstance regenerationEffect = player.getEffect(MobEffects.REGENERATION);
            if (regenerationEffect == null || regenerationEffect.getAmplifier() < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,
                        400,
                        0,
                        false,
                        false
                ));
            }
        }
    }

    // 生成针刺苦无（使用雪球作为替代）
    private static void spawnNeedleKunai(ServerPlayer sp) {
        if (!sp.level().isClientSide()) {
            // 创建雪球实体作为针刺苦无
            Snowball snowball = new Snowball(sp.level(), sp);
            
            // 设置发射方向
            Vec3 lookVec = sp.getViewVector(1.0F);
            double velocity = 3.0D;
            snowball.setDeltaMovement(lookVec.x * velocity, lookVec.y * velocity, lookVec.z * velocity);
            
            // 添加实体到世界
            sp.level().addFreshEntity(snowball);
        }
    }

    // 播放音效的辅助方法
    private static void playSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, 
                net.minecraft.sounds.SoundSource.PLAYERS, volume, pitch);
    }
}