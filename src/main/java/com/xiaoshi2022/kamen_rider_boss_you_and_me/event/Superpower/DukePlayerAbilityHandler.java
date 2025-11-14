package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class DukePlayerAbilityHandler {
    // 战斗数据分析持续时间（秒）
    private static final int COMBAT_ANALYSIS_DURATION = 15;
    // 技能冷却时间（秒）
    private static final long COOLDOWN_TIME = 5000; // 5秒
    // 技能消耗的骑士能量
    private static final double ENERGY_COST = 30.0;
    
    // 保存玩家的攻击力修饰符，用于正确移除
    private static final Map<ServerPlayer, AttributeModifier> playerAttackModifiers = new HashMap<>();
    
    // 修饰符UUID，用于一致性
    private static final UUID COMBAT_ANALYSIS_UUID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    
    // 处理玩家tick事件，更新战斗数据分析状态
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        
        // 只在服务器端处理
        if (player.level().isClientSide) {
            return;
        }
        
        ServerPlayer serverPlayer = (ServerPlayer) player;
        
        // 检查是否装备全套Duke盔甲
        if (Duke.isFullArmorEquipped(serverPlayer)) {
            // 获取玩家变量
            KRBVariables.PlayerVariables variables = (KRBVariables.PlayerVariables) 
                    serverPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables());
            
            // 更新战斗数据分析状态
            if (variables.isDukeCombatAnalysisActive) {
                if (System.currentTimeMillis() > variables.dukeCombatAnalysisEndTime) {
                    // 战斗数据分析效果结束
                    deactivateCombatAnalysis(serverPlayer, variables);
                }
            }
            
            // 检查冷却时间
            long currentTime = System.currentTimeMillis();
            if (variables.dukeCombatAnalysisCooldown > 0 && currentTime > variables.dukeCombatAnalysisCooldown) {
                variables.dukeCombatAnalysisCooldown = 0; // 冷却时间结束
                variables.syncPlayerVariables(serverPlayer);
            }
        }
    }
    
    // 激活战斗数据分析技能
    public static boolean activateCombatAnalysis(ServerPlayer player) {
        // 检查是否装备全套Duke盔甲
        if (!Duke.isFullArmorEquipped(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("需要装备全套Duke盔甲"));
            return false;
        }
        
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = (KRBVariables.PlayerVariables) 
                player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());
        
        // 检查冷却时间
        long currentTime = System.currentTimeMillis();
        if (variables.dukeCombatAnalysisCooldown > currentTime) {
            long remaining = (variables.dukeCombatAnalysisCooldown - currentTime) / 1000;
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("战斗数据分析技能冷却中，还需 " + remaining + " 秒"));
            return false;
        }
        
        // 检查是否已经激活
        if (variables.isDukeCombatAnalysisActive) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("战斗数据分析已激活"));
            return false;
        }
        
        // 消耗骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(player, ENERGY_COST)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("骑士能量不足，需要 " + (int)ENERGY_COST + " 点能量"));
            return false;
        }
        
        // 激活战斗数据分析
        variables.isDukeCombatAnalysisActive = true;
        variables.dukeCombatAnalysisEndTime = currentTime + (COMBAT_ANALYSIS_DURATION * 1000);
        variables.dukeCombatAnalysisCooldown = currentTime + COOLDOWN_TIME;
        variables.syncPlayerVariables(player);
        
        // 添加攻击伤害提升效果（+15%攻击力）
        AttributeModifier attackModifier = new AttributeModifier(
                COMBAT_ANALYSIS_UUID, 
                "Duke Combat Analysis", 
                0.15, 
                AttributeModifier.Operation.MULTIPLY_BASE
        );
        
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(attackModifier);
        playerAttackModifiers.put(player, attackModifier);
        
        // 添加伤害减免效果（-20%伤害）：只有当玩家没有伤害减免效果或效果等级低于I时才添加
        if (!player.hasEffect(MobEffects.DAMAGE_RESISTANCE) || player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() < 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 
                    COMBAT_ANALYSIS_DURATION * 20, // 转换为刻
                    0, 
                    false, 
                    true
            ));
        }
        
        // 添加速度提升效果（+10%移动速度）：只有当玩家没有速度效果或效果等级低于I时才添加
        if (!player.hasEffect(MobEffects.MOVEMENT_SPEED) || player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() < 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, 
                    COMBAT_ANALYSIS_DURATION * 20, 
                    0, 
                    false, 
                    true
            ));
        }
        
        // 生成粒子效果
        for (int i = 0; i < 20; i++) {
            double x = player.getX() + (player.getRandom().nextFloat() - 0.5) * player.getBbWidth() * 2;
            double y = player.getY() + player.getRandom().nextFloat() * player.getBbHeight();
            double z = player.getZ() + (player.getRandom().nextFloat() - 0.5) * player.getBbWidth() * 2;
            
            player.level().addParticle(
                    ParticleTypes.ENCHANT, 
                    x, y, z, 
                    0.1 * (player.getRandom().nextFloat() - 0.5), 
                    0.1 * player.getRandom().nextFloat(), 
                    0.1 * (player.getRandom().nextFloat() - 0.5)
            );
        }
        
        // 播放音效
        player.playSound(
                net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE, 
                1.0F, 
                1.5F
        );
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("战斗数据分析已激活！获得攻击力+15%、伤害减免+20%、移动速度+10%，持续15秒"));
        
        return true;
    }
    
    // 停用战斗数据分析技能
    private static void deactivateCombatAnalysis(ServerPlayer player, KRBVariables.PlayerVariables variables) {
        variables.isDukeCombatAnalysisActive = false;
        variables.syncPlayerVariables(player);
        
        // 移除攻击力提升效果
        AttributeModifier modifier = playerAttackModifiers.remove(player);
        if (modifier != null) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(modifier);
        }
        
        // 播放结束音效
        player.playSound(
                net.minecraft.sounds.SoundEvents.ITEM_BREAK, 
                0.5F, 
                2.0F
        );
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("战斗数据分析效果已结束"));
    }
}