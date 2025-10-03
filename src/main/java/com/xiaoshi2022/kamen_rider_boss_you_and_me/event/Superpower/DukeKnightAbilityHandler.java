package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.duke.Duke;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.DukeKnightEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class DukeKnightAbilityHandler {

    private static final Random RANDOM = new Random();
    private static final long COOLDOWN_TIME = 5000; // 5秒冷却时间（毫秒）
    // 技能消耗的骑士能量
    private static final double ENERGY_COST = 25.0;

    // 处理玩家tick事件，更新Duke骑士技能状态
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
            KRBVariables.PlayerVariables variables = (KRBVariables.PlayerVariables) serverPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
            
            // 检查冷却时间
            long currentTime = System.currentTimeMillis();
            if (variables.dukeKnightCooldown > 0 && currentTime > variables.dukeKnightCooldown) {
                variables.dukeKnightCooldown = 0; // 冷却时间结束
                // 可以添加冷却结束的提示
                variables.syncPlayerVariables(serverPlayer);
            }
        }
    }

    // 召唤Duke骑士的方法
    public static boolean summonDukeKnight(Player player) {
        // 只在服务器端处理
        if (player.level().isClientSide) {
            return false;
        }
        
        ServerPlayer serverPlayer = (ServerPlayer) player;
        
        // 检查是否装备全套Duke盔甲
        if (!Duke.isFullArmorEquipped(serverPlayer)) {
            return false;
        }
        
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = (KRBVariables.PlayerVariables) serverPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        
        // 检查冷却时间
        long currentTime = System.currentTimeMillis();
        if (variables.dukeKnightCooldown > currentTime) {
            long remaining = (variables.dukeKnightCooldown - currentTime) / 1000;
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("技能冷却中，还需 " + remaining + " 秒"));
            return false;
        }
        
        // 消耗骑士能量
        if (!RiderEnergyHandler.consumeRiderEnergy(player, ENERGY_COST)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("骑士能量不足，需要 " + (int)ENERGY_COST + " 点能量"));
            return false;
        }
        
        // 设置冷却时间
        variables.dukeKnightCooldown = currentTime + COOLDOWN_TIME;
        variables.syncPlayerVariables(serverPlayer);

        // 保存玩家主手武器
        ItemStack mainHandItem = player.getMainHandItem();
        
        // 保存玩家攻击力（包括武器伤害）
        // 在Minecraft中，Attributes.ATTACK_DAMAGE已经包含了基础攻击力和武器提供的攻击力
        double playerAttackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        
        boolean summoned = false;
        
        // 召唤3个Duke骑士实体
        for (int i = 1; i <= 3; i++) {
            DukeKnightEntity dukeKnight = ModEntityTypes.DUKE_KNIGHT.get().create(player.level());
            if (dukeKnight != null) {
                dukeKnight.setOwner(player);
                
                // 设置攻击顺序
                dukeKnight.setAttackOrder(i);
                
                // 设置是否可以攻击：第一个可以立即攻击，其他两个初始不可攻击
                dukeKnight.setCanAttack(i == 1);
                
                // 装备玩家手中的武器
                if (!mainHandItem.isEmpty()) {
                    dukeKnight.setWeapon(mainHandItem);
                }
                
                // 设置攻击力与玩家一致
                dukeKnight.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(playerAttackDamage);
                
                // 设置不同的位置，避免实体重叠
                double angle = Math.toRadians(player.getYRot() + (i - 2) * 20); // 三个实体分别位于玩家前方，左右各偏20度
                double offsetX = Math.sin(angle) * 1.5;
                double offsetZ = Math.cos(angle) * 1.5;
                dukeKnight.setPos(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
                
                // 添加实体到世界
                player.level().addFreshEntity(dukeKnight);
                
                // 生成召唤粒子效果
                for (int j = 0; j < 10; j++) {
                    double x = dukeKnight.getX() + RANDOM.nextFloat() * dukeKnight.getBbWidth() * 2.0F - dukeKnight.getBbWidth();
                    double y = dukeKnight.getY() + RANDOM.nextFloat() * dukeKnight.getBbHeight();
                    double z = dukeKnight.getZ() + RANDOM.nextFloat() * dukeKnight.getBbWidth() * 2.0F - dukeKnight.getBbWidth();
                    
                    // 使用正确的粒子效果生成方法
                    serverPlayer.level().addParticle(
                        ParticleTypes.ENCHANTED_HIT,
                        x, y, z,
                        0, 0, 0
                    );
                }
                
                summoned = true;
            }
        }
        
        // 设置召唤状态
        if (summoned) {
            variables.dukeKnightSummoned = true;
            variables.syncPlayerVariables(serverPlayer);
        }
        
        return summoned;
    }
}