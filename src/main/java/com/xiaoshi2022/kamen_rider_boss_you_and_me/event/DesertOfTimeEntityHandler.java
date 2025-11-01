package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.anotherRiders.Another_Den_o;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID)
public class DesertOfTimeEntityHandler {
    // 目标维度名称 - 使用和AnotherDenlinerEntity中相同的目标维度
    private static final String TARGET_DIMENSION = "kamen_rider_weapon_craft:the_desertof_time";
    
    // 调试标记
    private static final boolean DEBUG = true;
    
    // 随机数生成器
    private static final Random random = new Random();
    
    /**
     * 当玩家改变维度时触发
     * 监听玩家进入时之沙漠维度的事件，并在玩家进入时生成电王和时劫者实体
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide()) {
            Player player = (Player) event.getEntity();
            ServerLevel level = (ServerLevel) player.level();
            
            // 检查玩家是否进入了时之沙漠维度
            String currentDimension = level.dimension().location().toString();
            if (currentDimension.equals(TARGET_DIMENSION)) {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("Player {} entered Desert of Time dimension at {}", 
                            player.getName().getString(), player.position());
                }
                
                // 在玩家周围生成电王和时劫者实体
                spawnEntitiesForPlayer(player, level);
            }
        }
    }
    
    /**
     * 当玩家第一次加入世界时触发
     * 如果玩家直接加入时之沙漠维度，也需要生成实体
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide()) {
            Player player = (Player) event.getEntity();
            ServerLevel level = (ServerLevel) player.level();
            
            // 检查玩家是否在时之沙漠维度
            String currentDimension = level.dimension().location().toString();
            if (currentDimension.equals(TARGET_DIMENSION)) {
                if (DEBUG) {
                    kamen_rider_boss_you_and_me.LOGGER.info("Player {} logged in directly to Desert of Time dimension at {}", 
                            player.getName().getString(), player.position());
                }
                
                // 在玩家周围生成电王和时劫者实体
                spawnEntitiesForPlayer(player, level);
            }
        }
    }
    
    /**
     * 为进入时之沙漠的玩家生成电王和时劫者实体
     */
    private static void spawnEntitiesForPlayer(Player player, ServerLevel level) {
        // 生成时劫者实体
        TimeJackerEntity timeJacker = ModEntityTypes.TIME_JACKER.get().create(level);
        if (timeJacker != null) {
            // 设置时劫者位置，在玩家周围随机位置
            double x = player.getX() + (random.nextDouble() - 0.5) * 10.0;
            double z = player.getZ() + (random.nextDouble() - 0.5) * 10.0;
            double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)x, (int)z);
            
            timeJacker.setPos(x, y, z);
            level.addFreshEntity(timeJacker);
            
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("Spawned TimeJackerEntity for player {} at position: {}", 
                        player.getName().getString(), timeJacker.position());
            }
        }
        
        // 生成电王实体
        Another_Den_o anotherDenO = ModEntityTypes.ANOTHER_DEN_O.get().create(level);
        if (anotherDenO != null) {
            // 设置电王位置，在玩家周围随机位置，但与时间劫者保持一定距离
            double x = player.getX() + (random.nextDouble() - 0.5) * 10.0;
            double z = player.getZ() + (random.nextDouble() - 0.5) * 10.0;
            double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)x, (int)z);
            
            anotherDenO.setPos(x, y, z);
            level.addFreshEntity(anotherDenO);
            
            if (DEBUG) {
                kamen_rider_boss_you_and_me.LOGGER.info("Spawned Another_Den_o for player {} at position: {}", 
                        player.getName().getString(), anotherDenO.position());
            }
        }
        
        // 向玩家发送提示消息
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.desert_of_time.entities_spawned"));
    }
}