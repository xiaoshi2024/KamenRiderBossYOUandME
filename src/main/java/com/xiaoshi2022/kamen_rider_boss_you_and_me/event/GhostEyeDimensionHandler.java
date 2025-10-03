package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.GhostEyeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Set;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class GhostEyeDimensionHandler {
    // 存储已经转换为眼魔种族的玩家
    public static final Set<String> ghostEyePlayers = new HashSet<>();
    
    // 存储需要在重生时传送到眼魔维度的玩家
    private static final Set<String> playersToTeleportOnRespawn = new HashSet<>();
    
    // 存储已经获得过宝箱的玩家
    private static final Set<String> playersReceivedLoot = new HashSet<>();
    
    // 眼魔维度的键
    private static final ResourceKey<Level> GHOST_EYE_DIM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "ghost_eye")
    );
    
    // 当玩家死亡时触发
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 检查玩家是否持有Ridernecrom手环或相关装备
            if (hasRidernecromBracelet(player)) {
                // 记录玩家需要在重生时传送
                playersToTeleportOnRespawn.add(player.getUUID().toString());
                
                // 设置眼魔世界的重生点，让玩家直接在那里重生
                if (player.level().dimension() != GHOST_EYE_DIM) {
                    Level ghostEyeLevel = player.server.getLevel(GHOST_EYE_DIM);
                    if (ghostEyeLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        // 寻找或创建一个安全的重生位置
                        BlockPos teleportPos = new BlockPos(0, 64, 0);
                        BlockPos safeTeleportPos = findOrCreateSafePosition(serverLevel, teleportPos);
                        
                        // 设置眼魔世界的重生点
                        player.setRespawnPosition(GHOST_EYE_DIM, safeTeleportPos, 0.0F, true, false);
                    }
                }
            }
        }
    }
    
    // 将玩家传送回主世界
    private static void transferToOverworld(ServerPlayer player) {
        // 保存玩家的生命和经验
        float health = player.getHealth();
        int experienceLevel = player.experienceLevel;
        float experienceProgress = player.experienceProgress;
        
        // 创建主世界的服务器级别
        Level overworldLevel = player.server.getLevel(Level.OVERWORLD);
        
        if (overworldLevel instanceof ServerLevel serverLevel) {
            // 获取玩家在主世界的出生点或上次记录的位置
            BlockPos spawnPos = player.getRespawnPosition();
            if (spawnPos == null) {
                // 如果没有记录的出生点，使用世界出生点
                spawnPos = serverLevel.getSharedSpawnPos();
            }
            
            // 寻找或创建一个安全的传送位置
            BlockPos safeTeleportPos = findOrCreateSafePosition(serverLevel, spawnPos);
            
            // 创建传送目标
            net.minecraft.world.level.portal.PortalInfo portalInfo = new net.minecraft.world.level.portal.PortalInfo(
                new net.minecraft.world.phys.Vec3(safeTeleportPos.getX() + 0.5, safeTeleportPos.getY(), safeTeleportPos.getZ() + 0.5),
                net.minecraft.world.phys.Vec3.ZERO,
                player.getYRot(),
                player.getXRot()
            );
            
            // 执行传送
            player.teleportTo(serverLevel, portalInfo.pos.x, portalInfo.pos.y, portalInfo.pos.z, portalInfo.yRot, portalInfo.xRot);
            
            // 恢复玩家的生命和经验
            player.setHealth(health);
            player.experienceLevel = experienceLevel;
            player.experienceProgress = experienceProgress;
            
            // 给玩家消息提示
            player.displayClientMessage(Component.literal("You have been teleported back to the Overworld..."), true);
            
            // 恢复玩家原来的种族
            restoreOriginalRace(player);
        }
    }
    
    // 当玩家重生时触发
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String playerId = player.getUUID().toString();
            
            // 检查玩家是否需要在重生时传送到眼魔维度
            if (playersToTeleportOnRespawn.contains(playerId)) {
                // 从列表中移除玩家
                playersToTeleportOnRespawn.remove(playerId);
                
                // 如果玩家已经在眼魔维度，将其传送回主世界
                if (player.level().dimension() == GHOST_EYE_DIM) {
                    transferToOverworld(player);
                } else {
                    // 如果玩家不在眼魔维度，将其传送过去
                    teleportToGhostEyeDimension(player);
                }
            }
        }
    }
    
    // 当玩家改变维度时触发
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 如果玩家进入眼魔维度，将其转变为眼魔种族
            if (event.getTo() == GHOST_EYE_DIM) {
                // 恢复玩家生命值，防止进入眼魔世界时死亡
                player.setHealth(player.getMaxHealth());
                transformToGhostEye(player);
            }
            // 如果玩家离开眼魔维度，恢复其原来的种族
            else if (event.getFrom() == GHOST_EYE_DIM) {
                restoreOriginalRace(player);
            }
        }
    }
    
    // 检查玩家是否持有Ridernecrom手环或相关装备
    private static boolean hasRidernecromBracelet(Player player) {
        // 初始化AtomicBoolean变量用于Curios槽位检查
        java.util.concurrent.atomic.AtomicBoolean hasCuriosBracelet = new java.util.concurrent.atomic.AtomicBoolean(false);
        
        // 1. 检查玩家手中是否有Mega_uiorder手环驱动器
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        
        if (mainHand.getItem() == ModItems.MEGA_UIORDER_ITEM.get() || 
            offHand.getItem() == ModItems.MEGA_UIORDER_ITEM.get()) {
            return true;
        }
        
        // 2. 检查玩家是否装备了Ridernecrom盔甲
        if (player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() == ModItems.RIDERNECROM_HELMET.get() &&
            player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() == ModItems.RIDERNECROM_CHESTPLATE.get() &&
            player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() == ModItems.RIDERNECROM_LEGGINGS.get()) {
            return true;
        }
        
        // 3. 检查玩家物品栏中是否有Necrom Eye
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ModItems.NECROM_EYE.get()) {
                return true;
            }
        }
        
        // 4. 检查玩家物品栏中是否有Mega_uiorder手环（即使不在手上）
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ModItems.MEGA_UIORDER_ITEM.get()) {
                return true;
            }
        }
        
        // 5. 检查玩家是否在Curios槽位中装备了Mega_uiorder手环
        // 注意：这可能会与PlayerDeathHandler中的逻辑冲突，因为Curios槽位可能已经被清空
        try {
            boolean hasCurioBracelet = CuriosApi.getCuriosInventory(player)
                    .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof Mega_uiorder)
                            .isPresent())
                    .orElse(false);
            if (hasCurioBracelet) {
                return true;
            }
        } catch (Exception e) {
            // 安全检查，避免因Curios API调用失败导致整个检测逻辑失效
        }
        
        // 6. 检查玩家是否处于眼魔或幽冥状态（根据变量）
        try {
            // 使用AtomicBoolean来避免静态变量的线程安全问题
            java.util.concurrent.atomic.AtomicBoolean isNecrom = new java.util.concurrent.atomic.AtomicBoolean(false);
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(vars -> {
                if (vars.isMegaUiorderTransformed || vars.isNecromStandby) {
                    isNecrom.set(true);
                }
            });
            if (isNecrom.get()) {
                return true;
            }
        } catch (Exception e) {
            // 安全检查
        }
        
        return false;
    }
    
    // 将玩家传送到眼魔维度
    private static void teleportToGhostEyeDimension(ServerPlayer player) {
        // 保存玩家的生命和经验
        float health = player.getHealth();
        int experienceLevel = player.experienceLevel;
        float experienceProgress = player.experienceProgress;
        
        // 创建眼魔维度的服务器级别
        Level ghostEyeLevel = player.server.getLevel(GHOST_EYE_DIM);
        
        if (ghostEyeLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // 寻找或创建一个安全的传送位置
            BlockPos teleportPos = new BlockPos(0, 64, 0); // 使用固定坐标但会检查安全性
            BlockPos safeTeleportPos = findOrCreateSafePosition(serverLevel, teleportPos);
            
            // 创建传送目标
            net.minecraft.world.level.portal.PortalInfo portalInfo = new net.minecraft.world.level.portal.PortalInfo(
                new net.minecraft.world.phys.Vec3(safeTeleportPos.getX() + 0.5, safeTeleportPos.getY(), safeTeleportPos.getZ() + 0.5),
                net.minecraft.world.phys.Vec3.ZERO,
                player.getYRot(),
                player.getXRot()
            );
            
            // 执行传送
            player.teleportTo(serverLevel, portalInfo.pos.x, portalInfo.pos.y, portalInfo.pos.z, portalInfo.yRot, portalInfo.xRot);
            
            // 恢复玩家的生命和经验
            player.setHealth(health);
            player.experienceLevel = experienceLevel;
            player.experienceProgress = experienceProgress;
            
            // 给玩家消息提示
            player.displayClientMessage(Component.literal("你被传送到了眼魔世界..."), true);
            
            // 将玩家转变为眼魔种族
            transformToGhostEye(player);
            
            // 检查玩家是否已经获得过宝箱
            if (!playersReceivedLoot.contains(player.getUUID().toString())) {
                // 生成宝箱（一个存档一个玩家只能拿一次）
                spawnLootChest(serverLevel, player.blockPosition());
                // 标记玩家已经获得过宝箱
                playersReceivedLoot.add(player.getUUID().toString());
            }
        }
    }
    
    // 将玩家转变为眼魔种族
    public static void transformToGhostEye(ServerPlayer player) {
        String playerId = player.getUUID().toString();
        
        // 检查玩家是否是第一次转变为眼魔
        boolean isFirstTime = !ghostEyePlayers.contains(playerId);
        
        // 使用GhostEyeManager来设置眼魔状态并添加效果
        GhostEyeManager.setGhostEyeState(player, isFirstTime);
    }
    
    // 恢复玩家原来的种族
    public static void restoreOriginalRace(ServerPlayer player) {
        // 使用GhostEyeManager来移除眼魔状态和效果
        GhostEyeManager.removeGhostEyeState(player);
    }
    
    // 检查玩家是否是眼魔种族
    // 同时检查运行时集合和持久化变量，确保状态一致性
    public static boolean isGhostEyePlayer(Player player) {
        // 检查运行时集合
        boolean inRuntimeCollection = ghostEyePlayers.contains(player.getUUID().toString());
        
        // 检查持久化变量
        boolean hasPersistentFlag = false;
        try {
            hasPersistentFlag = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .map(variables -> variables.isGhostEye)
                    .orElse(false);
        } catch (Exception e) {
            // 安全检查，避免因能力系统调用失败导致整个方法失效
        }
        
        // 如果两个状态不一致，优先使用持久化变量的值进行同步
        if (inRuntimeCollection != hasPersistentFlag) {
            if (hasPersistentFlag) {
                // 如果持久化变量为true，但运行时集合中没有，添加到集合中
                ghostEyePlayers.add(player.getUUID().toString());
            } else {
                // 如果持久化变量为false，但运行时集合中有，从集合中移除
                ghostEyePlayers.remove(player.getUUID().toString());
            }
        }
        
        return hasPersistentFlag;
    }
    
    // 寻找或创建一个安全的位置
    private static BlockPos findOrCreateSafePosition(ServerLevel level, BlockPos startPos) {
        // 首先检查起始位置是否安全
        if (isPositionSafe(level, startPos)) {
            return startPos;
        }
        
        // 在周围寻找安全位置
        int searchRadius = 10;
        for (int y = -5; y <= 10; y++) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos pos = startPos.offset(x, y, z);
                    if (isPositionSafe(level, pos)) {
                        return pos;
                    }
                }
            }
        }
        
        // 如果没有找到安全位置，创建一个临时平台
        BlockPos platformPos = new BlockPos(startPos.getX(), 64, startPos.getZ());
        createTemporaryPlatform(level, platformPos);
        return platformPos.above();
    }
    
    // 检查指定位置是否安全
    private static boolean isPositionSafe(ServerLevel level, BlockPos pos) {
        // 检查Y坐标是否在有效范围内
        if (pos.getY() < 1 || pos.getY() > level.getHeight()) {
            return false;
        }
        
        // 检查脚下是否有方块
        BlockPos below = pos.below();
        if (level.isEmptyBlock(below)) {
            return false;
        }
        
        // 检查当前位置和上方是否有足够的空间
        return !level.getBlockState(pos).blocksMotion() && 
               !level.getBlockState(pos.above()).blocksMotion();
    }
    
    // 创建一个临时平台
    private static void createTemporaryPlatform(ServerLevel level, BlockPos center) {
        // 使用石头创建一个3x3的小平台
        net.minecraft.world.level.block.Block platformBlock = net.minecraft.world.level.block.Blocks.STONE;
        
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = center.offset(x, 0, z);
                level.setBlock(pos, platformBlock.defaultBlockState(), 3);
            }
        }
    }
    
    // 生成宝箱
    private static void spawnLootChest(ServerLevel level, BlockPos pos) {
        // 在玩家周围随机位置生成一个宝箱
        int offsetX = level.random.nextInt(10) - 5;
        int offsetZ = level.random.nextInt(10) - 5;
        
        BlockPos chestPos = findOrCreateSafePosition(level, pos.offset(offsetX, 0, offsetZ));
        chestPos = chestPos.below(); // 宝箱应该放在地面上
        
        // 设置宝箱方块
        level.setBlock(chestPos, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState(), 3);
        
        // 获取宝箱实体
        if (level.getBlockEntity(chestPos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chestEntity) {
            // 创建一个简单的战利品表
            net.minecraft.world.level.storage.loot.LootParams.Builder lootParams = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, 
                            new net.minecraft.world.phys.Vec3(chestPos.getX(), chestPos.getY(), chestPos.getZ()))
                    .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, null);
            
            // 生成一些基本物品
            net.minecraft.world.item.ItemStack eyeItem = new ItemStack(ModItems.NECROM_EYE.get());

            // 将物品放入宝箱
            chestEntity.setItem(0, eyeItem);
            chestEntity.setItem(2, new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT, 5 + level.random.nextInt(10)));
            chestEntity.setItem(3, new ItemStack(net.minecraft.world.item.Items.EMERALD, 2 + level.random.nextInt(5)));
            
            // 标记为已修改
            chestEntity.setChanged();
        }
    }
}