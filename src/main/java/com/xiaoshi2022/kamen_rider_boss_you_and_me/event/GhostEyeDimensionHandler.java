package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.GhostEyeManager;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.SafeTeleportUtil;
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
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.SafeTeleportUtil.findOrCreateSafePosition;

@Mod.EventBusSubscriber(modid = MODID)
public class GhostEyeDimensionHandler {
    // 存储已经转换为眼魔种族的玩家
    public static final Set<String> ghostEyePlayers = new HashSet<>();
    
    // 存储需要在重生时传送到眼魔维度的玩家
    private static final Set<String> playersToTeleportOnRespawn = new HashSet<>();
    
    // 不再需要静态集合来存储玩家是否获得过宝箱，现在使用PlayerVariables中的hasReceivedGhostEyeLootChest字段
    
    // 眼魔维度的键
    private static final ResourceKey<Level> GHOST_EYE_DIM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "ghost_eye")
    );
    
    // 当玩家死亡时触发
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 检查玩家是否持有Ridernecrom手环或相关装备
            boolean hasBracelet = hasRidernecromBracelet(player);

            // 检查玩家是否已经是眼魔状态
            boolean isGhostEye = isGhostEyePlayer(player);

            // 只有当玩家实际持有装备且不是眼魔状态时才记录传送
            if (hasBracelet && !isGhostEye) {
                // 记录玩家需要在重生时传送
                playersToTeleportOnRespawn.add(player.getUUID().toString());

                // 设置眼魔世界的重生点
                if (player.level().dimension() != GHOST_EYE_DIM) {
                    Level ghostEyeLevel = player.server.getLevel(GHOST_EYE_DIM);
                    if (ghostEyeLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        BlockPos teleportPos = new BlockPos(0, 64, 0);
                        BlockPos safeTeleportPos = findOrCreateSafePosition(serverLevel, teleportPos);
                        player.setRespawnPosition(GHOST_EYE_DIM, safeTeleportPos, 0.0F, true, false);
                    }
                }
            }
        }
    }


    // 将玩家传送回主世界
    private static void transferToOverworld(ServerPlayer player) {
        // 创建主世界的服务器级别
        Level overworldLevel = player.server.getLevel(Level.OVERWORLD);
        
        if (overworldLevel instanceof ServerLevel serverLevel) {
            // 获取玩家在主世界的出生点或上次记录的位置
            BlockPos spawnPos = player.getRespawnPosition();
            if (spawnPos == null) {
                // 如果没有记录的出生点，使用世界出生点
                spawnPos = serverLevel.getSharedSpawnPos();
            }
            
            // 使用安全传送工具执行传送
            SafeTeleportUtil.safelyTeleport(player, serverLevel, spawnPos);
            
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

                // 重新检查玩家是否仍然持有装备且不是眼魔状态
                boolean hasBracelet = hasRidernecromBracelet(player);
                boolean isGhostEye = isGhostEyePlayer(player);

                // 只有当玩家仍然持有装备且不是眼魔状态时才传送
                if (player.level().dimension() != GHOST_EYE_DIM && hasBracelet && !isGhostEye) {
                    Level ghostEyeLevel = player.server.getLevel(GHOST_EYE_DIM);
                    if (ghostEyeLevel instanceof ServerLevel serverLevel) {
                        BlockPos teleportPos = new BlockPos(0, 64, 0);
                        SafeTeleportUtil.safelyTeleportAfterDeath(player, serverLevel, teleportPos);

                        transformToGhostEye(player);

                        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                            if (!variables.hasReceivedGhostEyeLootChest) {
                                spawnLootChest(serverLevel, player.blockPosition());
                                variables.hasReceivedGhostEyeLootChest = true;
                                variables.syncPlayerVariables(player);
                            }
                        });
                    }
                } else {
                    // 如果玩家已经在眼魔维度或者不再持有装备，清除重生点
                    if (player.getRespawnPosition() != null &&
                            player.getRespawnDimension() == GHOST_EYE_DIM) {
                        player.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
                    }
                }
            }
        }
    }


    // 当玩家改变维度时触发
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 如果玩家进入眼魔维度，检查是否应该转变为眼魔种族
            if (event.getTo() == GHOST_EYE_DIM) {
                boolean hasBracelet = hasRidernecromBracelet(player);
                boolean isGhostEye = isGhostEyePlayer(player);

                // 只有持有装备且不是眼魔状态时才转变
                if (hasBracelet && !isGhostEye) {
                    player.setHealth(player.getMaxHealth());
                    transformToGhostEye(player);
                }
            }
            // 如果玩家离开眼魔维度，恢复其原来的种族
            else if (event.getFrom() == GHOST_EYE_DIM) {
                restoreOriginalRace(player);
            }
        }
    }

    
    // 检查玩家是否持有Ridernecrom手环或相关装备
    // 根据用户要求，只检查玩家是否实际持有物品，不根据状态变量
    private static boolean hasRidernecromBracelet(Player player) {
        // 检查手中物品
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean inHands = mainHand.getItem() == ModItems.MEGA_UIORDER_ITEM.get() ||
                offHand.getItem() == ModItems.MEGA_UIORDER_ITEM.get() ||
                mainHand.getItem() == ModItems.NECROM_EYE.get() ||
                offHand.getItem() == ModItems.NECROM_EYE.get();

        if (inHands) return true;

        // 检查Curios装备槽
        return CuriosApi.getCuriosInventory(player).map(inv -> {
            return inv.findFirstCurio(stack ->
                    stack.getItem() == ModItems.MEGA_UIORDER_ITEM.get() ||
                            stack.getItem() == ModItems.NECROM_EYE.get()
            ).isPresent();
        }).orElse(false);
    }

    
    // 将玩家传送到眼魔维度
    private static void teleportToGhostEyeDimension(ServerPlayer player) {
        // 创建眼魔维度的服务器级别
        Level ghostEyeLevel = player.server.getLevel(GHOST_EYE_DIM);
        
        if (ghostEyeLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // 使用固定坐标但会检查安全性
            BlockPos teleportPos = new BlockPos(0, 64, 0);
            
            // 使用安全传送工具执行传送
            SafeTeleportUtil.safelyTeleport(player, serverLevel, teleportPos);
            
            // 给玩家消息提示
            player.displayClientMessage(Component.literal("你被传送到了眼魔世界..."), true);
            
            // 将玩家转变为眼魔种族
            transformToGhostEye(player);
            
            // 检查玩家是否已经获得过宝箱 - 使用持久化的PlayerVariables字段
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                if (!variables.hasReceivedGhostEyeLootChest) {
                    // 生成宝箱（一个存档一个玩家只能拿一次）
                    spawnLootChest(serverLevel, player.blockPosition());
                    // 标记玩家已经获得过宝箱
                    variables.hasReceivedGhostEyeLootChest = true;
                    // 同步变量到客户端
                    variables.syncPlayerVariables(player);
                }
            });
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
                ghostEyePlayers.add(player.getUUID().toString());
            } else {
                ghostEyePlayers.remove(player.getUUID().toString());
            }
        }

        return hasPersistentFlag;
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