package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye;


import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GhostEyeEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import tocraft.walkers.api.PlayerShape;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 眼魔变身为眼魂实体的数据包
 * 当玩家按下B键时发送到服务器，触发形态转换
 */
public class GhostEyeTransformPacket {
    
    // 存储玩家的飞行限制和变身开始时间
    public static Map<UUID, Integer> playerFlightHeightLimits = new HashMap<>();
    public static Map<UUID, double[]> playerInitialPositions = new HashMap<>(); // 存储玩家的初始X和Z坐标
    public static Map<UUID, Long> playerTransformTimes = new HashMap<>();
    public static Map<UUID, MobEffectInstance> playerInvisibilityEffects = new HashMap<>(); // 存储玩家的隐身buff信息
    
    // 注册定时任务处理器
    @Mod.EventBusSubscriber
    public static class FlightHeightChecker {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
                ServerPlayer player = (ServerPlayer) event.player;
                UUID playerId = player.getUUID();
                
                // 检查飞行限制（高度和方位）
                if (playerFlightHeightLimits.containsKey(playerId) && playerInitialPositions.containsKey(playerId)) {
                    int maxHeight = playerFlightHeightLimits.get(playerId);
                    double[] initialPos = playerInitialPositions.get(playerId);
                    double initialX = initialPos[0];
                    double initialZ = initialPos[1];
                    
                    // 设置水平飞行范围限制（25格半径）
                        double maxHorizontalDistance = 25.0;
                    
                    // 检查高度限制
                    if (player.getY() > maxHeight) {
                        // 超过最大高度，直接恢复人形
                        CompoundTag playerData = player.getPersistentData();
                        playerData.putBoolean("isGhostEyeForm", false);
                        
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                        
                        // 移除所有效果
                        player.removeAllEffects();
                        
                        // 恢复之前的隐身buff（如果有的话）
                        if (playerInvisibilityEffects.containsKey(playerId)) {
                            player.addEffect(playerInvisibilityEffects.get(playerId));
                            playerInvisibilityEffects.remove(playerId);
                        }
                        
                        // 通知玩家
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("超出最大飞行高度，眼魂变身已解除！"), true);
                        
                        // 恢复人形
                        try {
                            LivingEntity currentShape = PlayerShape.getCurrentShape(player);
                            if (currentShape != null) {
                                PlayerShape.updateShapes(player, null);
                            }
                        } catch (Exception e) {
                            // 捕获可能的异常，避免服务器崩溃
                            e.printStackTrace();
                        }
                        
                        // 清理数据
                        playerFlightHeightLimits.remove(playerId);
                        playerInitialPositions.remove(playerId);
                        playerTransformTimes.remove(playerId);
                        return; // 结束检查，避免重复处理
                    }
                    
                    // 检查水平方位限制
                    double distanceX = player.getX() - initialX;
                    double distanceZ = player.getZ() - initialZ;
                    double horizontalDistance = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);
                    
                    if (horizontalDistance > maxHorizontalDistance) {
                        // 超出水平范围，直接恢复人形
                        CompoundTag playerData = player.getPersistentData();
                        playerData.putBoolean("isGhostEyeForm", false);
                        
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                        
                        // 移除所有效果
                        player.removeAllEffects();
                        
                        // 恢复之前的隐身buff（如果有的话）
                        if (playerInvisibilityEffects.containsKey(playerId)) {
                            player.addEffect(playerInvisibilityEffects.get(playerId));
                            playerInvisibilityEffects.remove(playerId);
                        }
                        
                        // 通知玩家
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("超出最大飞行范围，眼魂变身已解除！"), true);
                        
                        // 恢复人形
                        try {
                            LivingEntity currentShape = PlayerShape.getCurrentShape(player);
                            if (currentShape != null) {
                                PlayerShape.updateShapes(player, null);
                            }
                        } catch (Exception e) {
                            // 捕获可能的异常，避免服务器崩溃
                            e.printStackTrace();
                        }
                        
                        // 清理数据
                        playerFlightHeightLimits.remove(playerId);
                        playerInitialPositions.remove(playerId);
                        playerTransformTimes.remove(playerId);
                        return; // 结束检查，避免重复处理
                    }
                    
                    // 给玩家接近限制的提示
                    if (player.level().getGameTime() % 20 == 0) {
                        // 高度接近限制的提示
                        if (player.getY() >= maxHeight - 1 && player.getY() <= maxHeight) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("已达到最大飞行高度限制！超出将解除变身！"), true);
                        }
                        // 水平距离接近限制的提示
                        if (horizontalDistance >= maxHorizontalDistance - 3 && horizontalDistance <= maxHorizontalDistance) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("已接近最大飞行范围！超出将解除变身！"), true);
                        }
                    }
                }
                
                // 检查变身持续时间
                if (playerTransformTimes.containsKey(playerId)) {
                    long startTime = playerTransformTimes.get(playerId);
                    long currentTime = player.level().getGameTime();
                    long maxDuration = 6000; // 5分钟
                    
                    if (currentTime - startTime > maxDuration) {
                        // 持续时间结束，强制恢复人形
                        CompoundTag playerData = player.getPersistentData();
                        playerData.putBoolean("isGhostEyeForm", false);
                        
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                        
                        // 移除所有效果
                        player.removeAllEffects();
                        
                        // 恢复之前的隐身buff（如果有的话）
                        if (playerInvisibilityEffects.containsKey(playerId)) {
                            player.addEffect(playerInvisibilityEffects.get(playerId));
                            playerInvisibilityEffects.remove(playerId);
                        }
                        
                        // 通知玩家
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("眼魂变身时间已结束！"), true);
                        
                        // 恢复人形
                        try {
                            LivingEntity currentShape = PlayerShape.getCurrentShape(player);
                            if (currentShape != null) {
                                PlayerShape.updateShapes(player, null);
                            }
                        } catch (Exception e) {
                            // 捕获可能的异常，避免服务器崩溃
                            e.printStackTrace();
                        }
                        
                        // 清理数据
                        playerFlightHeightLimits.remove(playerId);
                        playerInitialPositions.remove(playerId);
                        playerTransformTimes.remove(playerId);
                    }
                }
            }
        }
    }

    public GhostEyeTransformPacket() {
        // 空构造函数，用于网络传输
    }

    public static void encode(GhostEyeTransformPacket packet, FriendlyByteBuf buffer) {
        // 不需要编码任何数据
    }

    public static GhostEyeTransformPacket decode(FriendlyByteBuf buffer) {
        return new GhostEyeTransformPacket();
    }

    public static void handle(GhostEyeTransformPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                Level level = player.level();
                
                // 检查是否在冷却时间内
                CompoundTag playerData = player.getPersistentData();
                long currentTime = level.getGameTime();
                long lastTransformTime = playerData.getLong("lastGhostEyeTransformTime");
                int cooldown = 600; // 冷却时间：30秒（600刻）
                
                if (currentTime - lastTransformTime < cooldown) {
                    // 还在冷却时间内
                    int remainingTime = (int) (cooldown - (currentTime - lastTransformTime)) / 20;
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("眼魂变身冷却中！剩余时间：" + remainingTime + "秒"), true);
                    return;
                }
                
                // 创建眼魂实体实例
                GhostEyeEntity ghostEyeEntity = ModEntityTypes.GHOST_EYE_ENTITY.get().create(level);
                if (ghostEyeEntity != null) {
                    // 设置眼魂实体的位置和旋转为玩家的位置和旋转
                    ghostEyeEntity.setPos(player.getX(), player.getY(), player.getZ());
                    ghostEyeEntity.setYRot(player.getYRot());
                    ghostEyeEntity.setXRot(player.getXRot());
                    
                    // 使用PlayerShape API进行变形
                    if (PlayerShape.updateShapes(player, ghostEyeEntity)) {
                        // 获取玩家变量
                        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                        
                        // 保存玩家当前的隐身buff（如果有的话）
                        MobEffectInstance invisibilityEffect = player.getEffect(MobEffects.INVISIBILITY);
                        if (invisibilityEffect != null) {
                            playerInvisibilityEffects.put(player.getUUID(), invisibilityEffect);
                            player.removeEffect(MobEffects.INVISIBILITY);
                        } else {
                            playerInvisibilityEffects.remove(player.getUUID());
                        }
                        
                        // 设置飞行能力（参照KRB模组的NBT模式）
                        variables.currentFlightController = "GhostEye";
                        player.getAbilities().mayfly = true;
                        player.getAbilities().flying = true;
                        player.getAbilities().setFlyingSpeed(0.3F); // 降低飞行速度（原版创造模式是0.6F）
                        player.onUpdateAbilities();
                        
                        // 在玩家NBT中存储飞行状态
                        playerData.putBoolean("isGhostEyeForm", true);
                        playerData.putLong("ghostEyeFormStartTime", currentTime); // 记录形态开始时间
                        playerData.putLong("lastGhostEyeTransformTime", currentTime); // 记录上次变身时间（用于冷却）
                        int maxFlightHeight = (int) player.getY() + 10;
                        playerData.putInt("ghostEyeMaxFlightHeight", maxFlightHeight); // 设置最大飞行高度（当前位置+10格）
                        playerData.putDouble("ghostEyeInitialX", player.getX()); // 记录初始X坐标
                        playerData.putDouble("ghostEyeInitialZ", player.getZ()); // 记录初始Z坐标
                        
                        // 存储到静态地图中用于定时检查
                        playerFlightHeightLimits.put(player.getUUID(), maxFlightHeight);
                        playerInitialPositions.put(player.getUUID(), new double[]{player.getX(), player.getZ()});
                        playerTransformTimes.put(player.getUUID(), currentTime);
                        
                        // 缩短效果持续时间到6000刻(5分钟)
                        int duration = 6000;
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0, false, true)); // 降低速度增益，设置为可见
                        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, duration, 0, false, true)); // 设置为可见
                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration, 0, false, true)); // 设置为可见
                        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, true)); // 添加发光效果，设置为可见
                        
                        // 移除跳跃提升效果，避免过于强大
                        
                        // 同步变量
                        variables.syncPlayerVariables(player);
                        
                        // 给玩家发送提示消息
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已变为眼魂实体！按Shift+B变回人形，按空格键飞行。注意：你现在是发光状态！"), true);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("飞行限制：高度当前位置+10格，水平范围25格半径，持续时间5分钟"), true);
                    } else {
                        // 如果变形失败
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("变形失败！"), true);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}