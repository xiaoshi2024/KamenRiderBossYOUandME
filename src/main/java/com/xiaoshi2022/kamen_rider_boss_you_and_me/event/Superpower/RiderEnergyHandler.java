package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RiderEnergyHandler {
    // 自然恢复的间隔（tick）
    private static final int REGEN_INTERVAL = 20; // 每秒一次
    // 能量充满提示的冷却时间（毫秒）
    private static final long FULL_ENERGY_NOTIFICATION_COOLDOWN = 5000;

    /**
     * 监听实体受到伤害的事件，当玩家造成伤害时增加骑士能量
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 确保事件在服务端触发
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 获取伤害来源
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        // 检查攻击者是否为玩家
        if (attacker instanceof ServerPlayer player) {
            // 获取玩家变量
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
            
            // 检查玩家是否佩戴了骑士腰带
            if (!KamenBossArmorUtil.isKamenBossArmorEquipped(player)) {
                return; // 玩家未佩戴腰带，不增加能量
            }

            // 计算要增加的能量值
            float damageDealt = event.getAmount();
            double energyToAdd = damageDealt * variables.riderEnergyGainPerDamage;

            // 增加能量值，但不超过最大值
            variables.riderEnergy = Math.min(variables.riderEnergy + energyToAdd, variables.maxRiderEnergy);

            // 检查能量是否已满
            boolean wasFull = variables.isRiderEnergyFull;
            variables.isRiderEnergyFull = variables.riderEnergy >= variables.maxRiderEnergy;
            
            // 如果能量刚充满且距离上次提示已超过冷却时间，则发送提示
            if (variables.isRiderEnergyFull && !wasFull) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - variables.lastEnergyFullTime > FULL_ENERGY_NOTIFICATION_COOLDOWN) {
                    player.sendSystemMessage(Component.literal("骑士能量已充满！").withStyle(ChatFormatting.BLUE));
                    variables.lastEnergyFullTime = currentTime;
                }
            }

            // 同步变量到客户端
            variables.syncPlayerVariables(player);
        }
    }

    /**
     * 为玩家添加骑士能量
     * @param player 玩家
     * @param amount 要添加的能量数量
     */
    public static void addRiderEnergy(Player player, double amount) {
        if (player.level().isClientSide()) {
            return; // 在客户端不执行实际添加
        }

        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());

        // 记录添加前的状态
        boolean wasFull = variables.isRiderEnergyFull;

        // 添加能量值，但不超过最大值
        variables.riderEnergy = Math.min(variables.riderEnergy + amount, variables.maxRiderEnergy);

        // 更新能量满状态
        variables.isRiderEnergyFull = variables.riderEnergy >= variables.maxRiderEnergy;

        // 如果能量刚充满且距离上次提示已超过冷却时间，则发送提示
        if (variables.isRiderEnergyFull && !wasFull) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - variables.lastEnergyFullTime > FULL_ENERGY_NOTIFICATION_COOLDOWN) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(Component.literal("骑士能量已充满！").withStyle(ChatFormatting.BLUE));
                }
                variables.lastEnergyFullTime = currentTime;
            }
        }

        // 同步变量到客户端
        variables.syncPlayerVariables(player);
    }

    /**
     * 检查玩家是否可以获取/使用骑士能量
     * @param player 玩家
     * @return 是否可以获取/使用能量
     */
    public static boolean canUseRiderEnergy(Player player) {
        return KamenBossArmorUtil.isKamenBossArmorEquipped(player);
    }

    /**
     * 服务器端tick事件，用于自然恢复骑士能量
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // 只在结束阶段执行，并且每20tick（1秒）执行一次
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % REGEN_INTERVAL != 0) {
            return;
        }

        // 遍历所有玩家
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            // 获取玩家变量
                KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());

                // 检查玩家是否佩戴了骑士腰带
                if (!KamenBossArmorUtil.isKamenBossArmorEquipped(player)) {
                    continue; // 玩家未佩戴腰带，跳过自然恢复
                }
            
            // 如果能量未满，进行自然恢复
            if (variables.riderEnergy < variables.maxRiderEnergy) {
                // 记录恢复前的状态
                boolean wasFull = variables.isRiderEnergyFull;
                
                // 增加能量值
                variables.riderEnergy = Math.min(variables.riderEnergy + variables.riderEnergyNaturalRegen, variables.maxRiderEnergy);

                // 更新能量满状态
                variables.isRiderEnergyFull = variables.riderEnergy >= variables.maxRiderEnergy;
                
                // 如果能量刚充满且距离上次提示已超过冷却时间，则发送提示
                if (variables.isRiderEnergyFull && !wasFull) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - variables.lastEnergyFullTime > FULL_ENERGY_NOTIFICATION_COOLDOWN) {
                        player.sendSystemMessage(Component.literal("骑士能量已充满！").withStyle(ChatFormatting.BLUE));
                        variables.lastEnergyFullTime = currentTime;
                    }
                }

                // 同步变量到客户端
                variables.syncPlayerVariables(player);
            }
        }
    }

    /**
     * 尝试消耗指定数量的骑士能量
     * @param player 玩家
     * @param amount 要消耗的能量数量
     * @return 是否成功消耗
     */
    public static boolean consumeRiderEnergy(Player player, double amount) {
        if (player.level().isClientSide()) {
            return false; // 在客户端不执行实际消耗
        }

        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());

        // 检查能量是否足够
        if (variables.riderEnergy >= amount) {
            // 消耗能量
            variables.riderEnergy -= amount;
            variables.isRiderEnergyFull = false; // 消耗后不再是满能量状态
            
            // 同步变量到客户端
            variables.syncPlayerVariables(player);
            return true;
        }

        // 能量不足时发送提示
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("骑士能量不足！").withStyle(ChatFormatting.RED));
        }
        return false;
    }

    /**
     * 获取玩家当前的骑士能量
     * @param player 玩家
     * @return 当前能量值
     */
    public static double getCurrentRiderEnergy(Player player) {
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());
        return variables.riderEnergy;
    }

    /**
     * 获取玩家的最大骑士能量
     * @param player 玩家
     * @return 最大能量值
     */
    public static double getMaxRiderEnergy(Player player) {
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());
        return variables.maxRiderEnergy;
    }

    /**
     * 直接设置玩家的骑士能量
     * @param player 玩家
     * @param amount 要设置的能量值
     */
    public static void setRiderEnergy(Player player, double amount) {
        if (player.level().isClientSide()) {
            return; // 在客户端不执行实际设置
        }

        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());

        // 设置能量值，但不超过最大值
        variables.riderEnergy = Math.max(0, Math.min(amount, variables.maxRiderEnergy));
        variables.isRiderEnergyFull = variables.riderEnergy >= variables.maxRiderEnergy;
        
        // 同步变量到客户端
        variables.syncPlayerVariables(player);
    }
}