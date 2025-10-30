package com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.MobRiderVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KamenBossArmorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
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
     * 监听实体受到伤害的事件，当攻击者造成伤害时增加骑士能量
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
            handlePlayerDamage(player, event.getAmount());
        }
        // 检查攻击者是否为非玩家生物且装备了骑士装备
        else if (attacker instanceof Mob mob && hasRiderEquipment(mob)) {
            handleMobDamage(mob, event.getAmount());
        }
    }
    
    /**
     * 处理玩家造成伤害时的能量增加
     */
    private static void handlePlayerDamage(ServerPlayer player, float damageDealt) {
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());
        
        // 检查玩家是否佩戴了骑士腰带
        if (!KamenBossArmorUtil.isKamenBossArmorEquipped(player)) {
            return; // 玩家未佩戴腰带，不增加能量
        }

        // 计算要增加的能量值
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
    
    /**
     * 处理非玩家生物造成伤害时的能量增加
     */
    private static void handleMobDamage(Mob mob, float damageDealt) {
        // 获取生物的骑士数据
        MobRiderVariables.MobRiderData data = mob.getCapability(MobRiderVariables.MOB_RIDER_DATA_CAPABILITY).orElse(new MobRiderVariables.MobRiderData());
        
        // 计算要增加的能量值
        double energyToAdd = damageDealt * data.riderEnergyGainPerDamage;
        
        // 增加能量值，但不超过最大值
        data.riderEnergy = Math.min(data.riderEnergy + energyToAdd, data.maxRiderEnergy);
        data.isRiderEnergyFull = data.riderEnergy >= data.maxRiderEnergy;
    }
    
    /**
     * 检查非玩家生物是否装备了骑士相关装备
     */
    public static boolean hasRiderEquipment(Mob mob) {
        // 检查生物是否装备了任何骑士腰带或相关装备
        // 这里可以根据实际需求扩展检查逻辑
        return mob.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.AbstractRiderBelt ||
               mob.getOffhandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.AbstractRiderBelt ||
               // 检查盔甲装备
               (mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD) != null && 
                mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.AbstractRiderBelt);
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
     * 为非玩家生物添加骑士能量
     * @param mob 非玩家生物
     * @param amount 要添加的能量数量
     */
    public static void addRiderEnergy(Mob mob, double amount) {
        if (mob.level().isClientSide()) {
            return; // 在客户端不执行实际添加
        }
        
        MobRiderVariables.MobRiderData data = mob.getCapability(MobRiderVariables.MOB_RIDER_DATA_CAPABILITY).orElse(new MobRiderVariables.MobRiderData());
        
        // 添加能量值，但不超过最大值
        data.riderEnergy = Math.min(data.riderEnergy + amount, data.maxRiderEnergy);
        data.isRiderEnergyFull = data.riderEnergy >= data.maxRiderEnergy;
    }
    
    /**
     * 通用方法：为实体添加骑士能量
     * @param entity 实体（玩家或非玩家生物）
     * @param amount 要添加的能量数量
     */
    public static void addRiderEnergy(Entity entity, double amount) {
        if (entity instanceof Player player) {
            addRiderEnergy(player, amount);
        } else if (entity instanceof Mob mob) {
            addRiderEnergy(mob, amount);
        }
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
     * 检查非玩家生物是否可以获取/使用骑士能量
     * @param mob 非玩家生物
     * @return 是否可以获取/使用能量
     */
    public static boolean canUseRiderEnergy(Mob mob) {
        return hasRiderEquipment(mob);
    }
    
    /**
     * 通用方法：检查实体是否可以获取/使用骑士能量
     * @param entity 实体
     * @return 是否可以获取/使用能量
     */
    public static boolean canUseRiderEnergy(Entity entity) {
        if (entity instanceof Player player) {
            return canUseRiderEnergy(player);
        } else if (entity instanceof Mob mob) {
            return canUseRiderEnergy(mob);
        }
        return false;
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
            // 恢复玩家的骑士能量
            regeneratePlayerRiderEnergy(player);
        }

        // 遍历所有世界中的非玩家生物，为装备了骑士装备的生物恢复能量
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // 使用 getWorldBounds() 方法获取世界边界
            AABB worldBounds = getWorldBounds(level);

            // 使用 getEntitiesOfClass 方法，并通过 EntityType 筛选 Mob 类实体
            for (Mob mob : level.getEntitiesOfClass(Mob.class, worldBounds, entity -> true)) {
                if (hasRiderEquipment(mob)) {
                    regenerateMobRiderEnergy(mob);
                }
            }
        }
    }

    /**
     * 获取世界的边界范围
     */
    private static AABB getWorldBounds(ServerLevel level) {
        WorldBorder worldBorder = level.getWorldBorder();
        double centerX = worldBorder.getCenterX();
        double centerZ = worldBorder.getCenterZ();
        double size = worldBorder.getSize() / 2.0;

        return new AABB(
                centerX - size, level.getMinBuildHeight(), centerZ - size,
                centerX + size, level.getMaxBuildHeight(), centerZ + size
        );
    }
    
    /**
     * 恢复玩家的骑士能量
     */
    private static void regeneratePlayerRiderEnergy(ServerPlayer player) {
        // 获取玩家变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY).orElse(new KRBVariables.PlayerVariables());

        // 检查玩家是否佩戴了骑士腰带
        if (!KamenBossArmorUtil.isKamenBossArmorEquipped(player)) {
            return; // 玩家未佩戴腰带，跳过自然恢复
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
    
    /**
     * 恢复非玩家生物的骑士能量
     */
    private static void regenerateMobRiderEnergy(Mob mob) {
        MobRiderVariables.MobRiderData data = mob.getCapability(MobRiderVariables.MOB_RIDER_DATA_CAPABILITY).orElse(new MobRiderVariables.MobRiderData());
        
        // 如果能量未满，进行自然恢复
        if (data.riderEnergy < data.maxRiderEnergy) {
            // 增加能量值
            data.riderEnergy = Math.min(data.riderEnergy + data.riderEnergyNaturalRegen, data.maxRiderEnergy);
            data.isRiderEnergyFull = data.riderEnergy >= data.maxRiderEnergy;
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
     * 尝试消耗非玩家生物的骑士能量
     * @param mob 非玩家生物
     * @param amount 要消耗的能量数量
     * @return 是否成功消耗
     */
    public static boolean consumeRiderEnergy(Mob mob, double amount) {
        if (mob.level().isClientSide()) {
            return false; // 在客户端不执行实际消耗
        }
        
        MobRiderVariables.MobRiderData data = mob.getCapability(MobRiderVariables.MOB_RIDER_DATA_CAPABILITY).orElse(new MobRiderVariables.MobRiderData());
        
        // 检查能量是否足够
        if (data.riderEnergy >= amount) {
            // 消耗能量
            data.riderEnergy -= amount;
            data.isRiderEnergyFull = false; // 消耗后不再是满能量状态
            return true;
        }
        
        return false;
    }
    
    /**
     * 通用方法：尝试消耗实体的骑士能量
     * @param entity 实体
     * @param amount 要消耗的能量数量
     * @return 是否成功消耗
     */
    public static boolean consumeRiderEnergy(Entity entity, double amount) {
        if (entity instanceof Player player) {
            return consumeRiderEnergy(player, amount);
        } else if (entity instanceof Mob mob) {
            return consumeRiderEnergy(mob, amount);
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
     * 获取非玩家生物当前的骑士能量
     * @param mob 非玩家生物
     * @return 当前能量值
     */
    public static double getCurrentRiderEnergy(Mob mob) {
        MobRiderVariables.MobRiderData data = mob.getCapability(MobRiderVariables.MOB_RIDER_DATA_CAPABILITY).orElse(new MobRiderVariables.MobRiderData());
        return data.riderEnergy;
    }
    
    /**
     * 通用方法：获取实体当前的骑士能量
     * @param entity 实体
     * @return 当前能量值
     */
    public static double getCurrentRiderEnergy(Entity entity) {
        if (entity instanceof Player player) {
            return getCurrentRiderEnergy(player);
        } else if (entity instanceof Mob mob) {
            return getCurrentRiderEnergy(mob);
        }
        return 0.0D;
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
     * 获取非玩家生物的最大骑士能量
     * @param mob 非玩家生物
     * @return 最大能量值
     */
    public static double getMaxRiderEnergy(Mob mob) {
        MobRiderVariables.MobRiderData data = mob.getCapability(MobRiderVariables.MOB_RIDER_DATA_CAPABILITY).orElse(new MobRiderVariables.MobRiderData());
        return data.maxRiderEnergy;
    }
    
    /**
     * 通用方法：获取实体的最大骑士能量
     * @param entity 实体
     * @return 最大能量值
     */
    public static double getMaxRiderEnergy(Entity entity) {
        if (entity instanceof Player player) {
            return getMaxRiderEnergy(player);
        } else if (entity instanceof Mob mob) {
            return getMaxRiderEnergy(mob);
        }
        return 100.0D;
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
    
    /**
     * 直接设置非玩家生物的骑士能量
     * @param mob 非玩家生物
     * @param amount 要设置的能量值
     */
    public static void setRiderEnergy(Mob mob, double amount) {
        if (mob.level().isClientSide()) {
            return; // 在客户端不执行实际设置
        }
        
        MobRiderVariables.MobRiderData data = mob.getCapability(MobRiderVariables.MOB_RIDER_DATA_CAPABILITY).orElse(new MobRiderVariables.MobRiderData());
        
        // 设置能量值，但不超过最大值
        data.riderEnergy = Math.max(0, Math.min(amount, data.maxRiderEnergy));
        data.isRiderEnergyFull = data.riderEnergy >= data.maxRiderEnergy;
    }
    
    /**
     * 通用方法：直接设置实体的骑士能量
     * @param entity 实体
     * @param amount 要设置的能量值
     */
    public static void setRiderEnergy(Entity entity, double amount) {
        if (entity instanceof Player player) {
            setRiderEnergy(player, amount);
        } else if (entity instanceof Mob mob) {
            setRiderEnergy(mob, amount);
        }
    }
}