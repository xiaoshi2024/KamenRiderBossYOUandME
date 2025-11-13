// package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

// import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
// import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
// import net.minecraft.server.level.ServerLevel;
// import net.minecraft.util.RandomSource;
// import net.minecraft.world.entity.player.Player;
// import net.minecraft.world.level.LevelAccessor;
// import net.minecraftforge.event.TickEvent;
// import net.minecraftforge.event.entity.player.PlayerEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.common.Mod;

// import java.util.List;

// import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

// @Mod.EventBusSubscriber(modid = MODID)
// public class EliteMonsterSpawnManager {

//     // 生成概率配置
//     private static final double TRANSFORMED_SPAWN_PROBABILITY = 0.3; // 30%概率生成已变身的百界众
//     private static final int MAX_ELITE_MONSTERS_PER_CHUNK = 1; // 每个区块最大百界众数量
//     private static final int MAX_ELITE_MONSTERS_PER_DIMENSION = 5; // 每个维度最大百界众数量
//     private static final int SPAWN_RADIUS_AROUND_PLAYER = 32; // 玩家周围生成范围
    
//     // 清理配置
//     private static final int CLEANUP_INTERVAL = 600; // 清理间隔（tick），从12000减少到600（约30秒）
//     private static final int MAX_DISTANCE_FROM_PLAYER = 128; // 距离玩家的最大距离
//     private static int tickCounter = 0;

//     // 移除CheckSpawn事件处理，因为LivingSpawnEvent在当前Forge版本中不存在
//     // 我们使用biome_modifier来配置生成条件

//     /**
//      * 当玩家进入新区块时，尝试生成百界众
//      */
//     @SubscribeEvent
//     public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
//         if (event.getEntity().level() instanceof ServerLevel serverLevel) {
//             spawnEliteMonstersAroundPlayer(serverLevel, event.getEntity());
//         }
//     }

//     /**
//      * 当玩家登录时，尝试生成百界众
//      */
//     @SubscribeEvent
//     public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
//         if (event.getEntity().level() instanceof ServerLevel serverLevel) {
//             spawnEliteMonstersAroundPlayer(serverLevel, event.getEntity());
//         }
//     }

//     /**
//      * 在玩家周围生成百界众实体
//      */
//     private static void spawnEliteMonstersAroundPlayer(ServerLevel level, Player player) {
//         RandomSource random = level.random;

//         // 减少生成概率，从20%降低到10%
//         if (random.nextDouble() < 0.1) {
//             // 检查维度内百界众总数是否超过限制
//             if (countEliteMonstersInDimension(level) >= MAX_ELITE_MONSTERS_PER_DIMENSION) {
//                 return; // 维度内百界众数量已达上限，不生成新的
//             }

//             int count = 1;

//             for (int i = 0; i < count; i++) {
//                 // 在玩家周围随机位置生成
//                 double x = player.getX() + (random.nextDouble() - 0.5) * SPAWN_RADIUS_AROUND_PLAYER * 2;
//                 double z = player.getZ() + (random.nextDouble() - 0.5) * SPAWN_RADIUS_AROUND_PLAYER * 2;
//                 int y = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
//                         new net.minecraft.core.BlockPos((int)x, 0, (int)z)).getY();

//                 // 检查生成位置是否有效，并且区块内百界众数量未超过限制
//                 if (y > 0 && y < level.getMaxBuildHeight()) {
//                     // 计算目标位置区块内的百界众数量
//                     int nearbyEliteCount = countNearbyEliteMonsters(level, x, z);
                    
//                     // 只有当区块内百界众数量少于最大限制时才生成
//                     if (nearbyEliteCount < MAX_ELITE_MONSTERS_PER_CHUNK) {
//                         // 额外检查玩家周围是否已有足够多的百界众
//                         int nearbyPlayerCount = countEliteMonstersNearPlayer(level, player);
//                         if (nearbyPlayerCount < 2) { // 玩家周围最多2个百界众
//                             EliteMonsterNpc eliteMonster = ModEntityTypes.ELITE_MONSTER_NPC.get().create(level);
//                             if (eliteMonster != null) {
//                                 eliteMonster.setPos(x, y, z);

//                                 // 随机决定是否生成已变身的百界众
//                                 if (random.nextDouble() < TRANSFORMED_SPAWN_PROBABILITY) {
//                                     setTransformedState(eliteMonster);
//                                 }

//                                 level.addFreshEntity(eliteMonster);
//                             }
//                         }
//                     }
//                 }
//             }
//         }
//     }

//     /**
//      * 设置百界众为已变身状态
//      */
//     private static void setTransformedState(EliteMonsterNpc eliteMonster) {
//         // 标记为已变身
//         eliteMonster.setTransformed(true);

//         // 设置血量
//         eliteMonster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
//                 .setBaseValue(80.0D);
//         eliteMonster.setHealth(80.0F);

//         // 强制刷新实体尺寸和状态
//         eliteMonster.refreshDimensions();

//         // 触发变身，装备随机腰带
//         EliteMonsterEquipHandler.equipRandomDriverAndTransform(eliteMonster);
//     }

//     /**
//      * 计算指定坐标周围的百界众数量
//      */
//     private static int countNearbyEliteMonsters(LevelAccessor level, double x, double z) {
//         if (!(level instanceof ServerLevel serverLevel)) {
//             return 0;
//         }

//         // 计算区块坐标
//         int chunkX = (int)x >> 4;
//         int chunkZ = (int)z >> 4;

//         // 搜索周围的区块
//         int count = 0;
//         for (int cx = -1; cx <= 1; cx++) {
//             for (int cz = -1; cz <= 1; cz++) {
//                 List<EliteMonsterNpc> monsters = serverLevel.getEntitiesOfClass(
//                         EliteMonsterNpc.class,
//                         new net.minecraft.world.phys.AABB(
//                                 (chunkX + cx) << 4,
//                                 0,
//                                 (chunkZ + cz) << 4,
//                                 ((chunkX + cx) << 4) + 16,
//                                 serverLevel.getMaxBuildHeight(),
//                                 ((chunkZ + cz) << 4) + 16
//                         )
//                 );
//                 count += monsters.size();
//             }
//         }

//         return count;
//     }
    
//     /**
//      * 计算玩家周围的百界众数量
//      */
//     private static int countEliteMonstersNearPlayer(ServerLevel level, Player player) {
//         double radius = SPAWN_RADIUS_AROUND_PLAYER * 2;
//         List<EliteMonsterNpc> monsters = level.getEntitiesOfClass(
//                 EliteMonsterNpc.class,
//                 new net.minecraft.world.phys.AABB(
//                         player.getX() - radius,
//                         player.getY() - radius,
//                         player.getZ() - radius,
//                         player.getX() + radius,
//                         player.getY() + radius,
//                         player.getZ() + radius
//                 )
//         );
//         return monsters.size();
//     }
    
//     /**
//      * 计算整个维度内的百界众数量
//      */
//     private static int countEliteMonstersInDimension(ServerLevel level) {
//         List<EliteMonsterNpc> monsters = level.getEntitiesOfClass(
//                 EliteMonsterNpc.class,
//                 new net.minecraft.world.phys.AABB(-30000000, -30000000, -30000000, 30000000, 30000000, 30000000)
//         );
//         return monsters.size();
//     }

//     // 我们在EliteMonsterNpc的finalizeSpawn方法中已经实现了变身概率逻辑
//     // 注意：通过修改biome_modifier中的maxCount=1，我们已经限制了自然生成的最大数量
//     // 同时在spawnEliteMonstersAroundPlayer方法中也检查了区块内的数量限制
    
//     /**
//      * 服务器tick事件监听器，用于定期清理远离玩家的百界众实体
//      */
//     @SubscribeEvent
//     public static void onServerTick(TickEvent.ServerTickEvent event) {
//         if (event.phase != TickEvent.Phase.END) {
//             return;
//         }
        
//         // 增加tick计数器
//         tickCounter++;
        
//         // 达到清理间隔时执行清理
//         if (tickCounter >= CLEANUP_INTERVAL) {
//             tickCounter = 0;
//             cleanupIdleEliteMonsters(event.getServer());
//         }
//     }
    
//     /**
//      * 清理远离玩家的百界众实体
//      */
//     private static void cleanupIdleEliteMonsters(net.minecraft.server.MinecraftServer server) {
//         // 遍历所有服务器维度
//         for (ServerLevel level : server.getAllLevels()) {
//             // 获取维度中的所有百界众实体
//             List<EliteMonsterNpc> eliteMonsters = level.getEntitiesOfClass(EliteMonsterNpc.class, 
//                     new net.minecraft.world.phys.AABB(-30000000, -30000000, -30000000, 30000000, 30000000, 30000000));
            
//             // 获取维度中的所有玩家
//             List<net.minecraft.server.level.ServerPlayer> players = level.players();
            
//             // 如果维度内百界众数量超过最大限制，优先清理未变身的百界众
//             if (eliteMonsters.size() > MAX_ELITE_MONSTERS_PER_DIMENSION) {
//                 // 先清理未变身的百界众
//                 for (EliteMonsterNpc monster : new java.util.ArrayList<>(eliteMonsters)) {
//                     if (!monster.isTransformed()) {
//                         monster.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
//                         eliteMonsters.remove(monster);
//                         // 如果数量已达标，停止清理
//                         if (eliteMonsters.size() <= MAX_ELITE_MONSTERS_PER_DIMENSION) {
//                             break;
//                         }
//                     }
//                 }
//             }
            
//             // 清理远离所有玩家的百界众实体
//             for (EliteMonsterNpc monster : eliteMonsters) {
//                 // 检查是否有玩家在范围内
//                 boolean hasNearbyPlayer = false;
//                 for (Player player : players) {
//                     if (monster.distanceToSqr(player) < MAX_DISTANCE_FROM_PLAYER * MAX_DISTANCE_FROM_PLAYER) {
//                         hasNearbyPlayer = true;
//                         break;
//                     }
//                 }
                
//                 // 如果没有玩家在范围内，移除该百界众实体
//                 if (!hasNearbyPlayer) {
//                     monster.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
//                 }
//             }
//         }
//     }
// }