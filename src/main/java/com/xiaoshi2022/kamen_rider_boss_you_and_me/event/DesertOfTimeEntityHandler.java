package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import net.minecraft.world.phys.AABB;

@Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID)
public class DesertOfTimeEntityHandler {
    // 目标维度名称 - 使用和AnotherDenlinerEntity中相同的目标维度
    private static final String TARGET_DIMENSION = "kamen_rider_weapon_craft:the_desertof_time";
    // 主世界维度名称
    private static final String OVERWORLD_DIMENSION = "minecraft:overworld";
    // 时间领域维度名称
    private static final String TIME_REALM_DIMENSION = "kamen_rider_boss_you_and_me:time_realm";
    
    // NBT标签键名
    private static final String HAS_ATTACKED_ANOTHER_DEN_O = "HasAttackedAnotherDenO";
    private static final String HAS_ATTACKED_TIME_JACKER = "HasAttackedTimeJacker";
    private static final String HAS_DEFEATED_ANOTHER_DEN_O = "HasDefeatedAnotherDenO";
    
    // 调试标记
    private static final boolean DEBUG = true;
    
    // 随机数生成器
    private static final Random random = new Random();
    
    /**
     * 当玩家改变维度时触发
     * 监听玩家进入时之沙漠维度或时间领域维度的事件，并在玩家进入时生成电王和时劫者实体
     * 只在玩家在主世界攻击过这些实体后才生成
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide()) {
            Player player = (Player) event.getEntity();
            ServerLevel level = (ServerLevel) player.level();
            
            // 检查玩家是否进入了时之沙漠维度或时间领域维度
            String currentDimension = level.dimension().location().toString();
            if (currentDimension.equals(TARGET_DIMENSION) || currentDimension.equals(TIME_REALM_DIMENSION)) {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("Player {} entered {} dimension at {}", 
                            player.getName().getString(), 
                            currentDimension.equals(TARGET_DIMENSION) ? "Desert of Time" : "Time Realm", 
                            player.position());
                }
                
                // 只有在玩家在主世界攻击过这些实体后才生成
                spawnEntitiesForPlayer(player, level);
            }
        }
    }
    
    /**
     * 监听生物死亡事件，记录玩家在时之沙漠击败异类电王的行为
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 只在服务器端处理
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        // 检查死亡来源是否是玩家
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof Player)) {
            return;
        }
        
        Player player = (Player) sourceEntity;
        LivingEntity target = event.getEntity();
        
        // 检查是否在时之沙漠维度或时间领域维度
        String currentDimension = player.level().dimension().location().toString();
        if (currentDimension.equals(TARGET_DIMENSION) || currentDimension.equals(TIME_REALM_DIMENSION)) {
            // 检查被击败的是否是异类电王
            if (target instanceof Another_Den_o) {
                // 标记玩家在时之沙漠击败过异类电王
                player.getPersistentData().putBoolean(HAS_DEFEATED_ANOTHER_DEN_O, true);
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("Player {} defeated Another_Den_o in {}", 
                            player.getName().getString(),
                            currentDimension.equals(TARGET_DIMENSION) ? "Desert of Time" : "Time Realm");
                }
            }
        }
    }
    
    /**
     * 当玩家第一次加入世界时触发
     * 如果玩家直接加入时之沙漠维度或时间领域维度，也需要生成实体
     * 只在玩家在主世界攻击过这些实体后才生成
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide()) {
            Player player = (Player) event.getEntity();
            ServerLevel level = (ServerLevel) player.level();
            
            // 检查玩家是否在时之沙漠维度或时间领域维度
            String currentDimension = level.dimension().location().toString();
            if (currentDimension.equals(TARGET_DIMENSION) || currentDimension.equals(TIME_REALM_DIMENSION)) {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("Player {} logged in directly to {} dimension at {}", 
                            player.getName().getString(),
                            currentDimension.equals(TARGET_DIMENSION) ? "Desert of Time" : "Time Realm",
                            player.position());
                }
                
                // 只有在玩家在主世界攻击过这些实体后才生成
                spawnEntitiesForPlayer(player, level);
            }
        }
    }
    
    /**
     * 监听生物受到伤害的事件，记录玩家在主世界攻击异类电王和时劫者的行为
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // 只在服务器端处理
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        // 检查伤害来源是否是玩家
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof Player)) {
            return;
        }
        
        Player player = (Player) sourceEntity;
        LivingEntity target = event.getEntity();
        
        // 检查是否在主世界
        String currentDimension = player.level().dimension().location().toString();
        if (!currentDimension.equals(OVERWORLD_DIMENSION)) {
            return;
        }
        
        // 检查被攻击的是否是异类电王
        if (target instanceof Another_Den_o) {
            // 标记玩家攻击过异类电王
            player.getPersistentData().putBoolean(HAS_ATTACKED_ANOTHER_DEN_O, true);
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("Player {} attacked Another_Den_o in overworld", 
                        player.getName().getString());
            }
        }
        // 检查被攻击的是否是时劫者
        else if (target instanceof TimeJackerEntity) {
            // 标记玩家攻击过时劫者
            player.getPersistentData().putBoolean(HAS_ATTACKED_TIME_JACKER, true);
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("Player {} attacked TimeJackerEntity in overworld", 
                        player.getName().getString());
            }
        }
    }
    
    /**
     * 为进入时之沙漠或时间领域维度的玩家生成电王和时劫者实体
     * 只在玩家在主世界攻击过这些实体后才生成，且在时之沙漠中未击败过异类电王
     */
    private static void spawnEntitiesForPlayer(Player player, ServerLevel level) {
        // 获取玩家的攻击记录
        boolean hasAttackedAnotherDenO = player.getPersistentData().getBoolean(HAS_ATTACKED_ANOTHER_DEN_O);
        boolean hasAttackedTimeJacker = player.getPersistentData().getBoolean(HAS_ATTACKED_TIME_JACKER);
        
        // 检查是否满足生成条件：玩家在主世界攻击过相应实体，且附近没有足够的实体
        AABB searchArea = new AABB(player.blockPosition()).inflate(50);
        int existingTimeJackerCount = level.getEntitiesOfClass(TimeJackerEntity.class, searchArea).size();
        int existingAnotherDenOCount = level.getEntitiesOfClass(Another_Den_o.class, searchArea).size();
        
        boolean spawnedEntities = false;
        
        // 只有在玩家攻击过时劫者且附近时劫者数量不足时才生成
        if (hasAttackedTimeJacker && existingTimeJackerCount < 2) {
            // 生成时劫者实体
            TimeJackerEntity timeJacker = ModEntityTypes.TIME_JACKER.get().create(level);
            if (timeJacker != null) {
                // 设置时劫者位置，在玩家周围随机位置
                double x = player.getX() + (random.nextDouble() - 0.5) * 10.0;
                double z = player.getZ() + (random.nextDouble() - 0.5) * 10.0;
                double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)x, (int)z);
                
                timeJacker.setPos(x, y, z);
                level.addFreshEntity(timeJacker);
                
                spawnedEntities = true;
                
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("Spawned TimeJackerEntity for player {} at position: {}", 
                            player.getName().getString(), timeJacker.position());
                }
            }
        }
        
        // 只有在玩家攻击过异类电王、附近异类电王数量不足且未击败过时之沙漠中的异类电王时才生成
        boolean hasDefeatedAnotherDenO = player.getPersistentData().getBoolean(HAS_DEFEATED_ANOTHER_DEN_O);
        if (hasAttackedAnotherDenO && existingAnotherDenOCount < 1 && !hasDefeatedAnotherDenO) {
            // 生成电王实体
            Another_Den_o anotherDenO = ModEntityTypes.ANOTHER_DEN_O.get().create(level);
            if (anotherDenO != null) {
                // 设置电王位置，在玩家周围随机位置，但与时间劫者保持一定距离
                double x = player.getX() + (random.nextDouble() - 0.5) * 10.0;
                double z = player.getZ() + (random.nextDouble() - 0.5) * 10.0;
                double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)x, (int)z);
                
                anotherDenO.setPos(x, y, z);
                level.addFreshEntity(anotherDenO);
                
                spawnedEntities = true;
                
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("Spawned Another_Den_o for player {} at position: {}", 
                            player.getName().getString(), anotherDenO.position());
                }
            }
        }
        
        // 只在确实生成了实体时才发送提示消息
        if (spawnedEntities) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.desert_of_time.entities_spawned"));
        }
    }
}