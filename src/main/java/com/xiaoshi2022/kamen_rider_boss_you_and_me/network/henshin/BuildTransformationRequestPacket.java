package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.SlotResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class BuildTransformationRequestPacket {
    private final boolean isPressing;

    // 存储玩家的音效任务
    private static final Map<ServerPlayer, ScheduledFuture<?>> playerSoundTasks = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public BuildTransformationRequestPacket(boolean isPressing) {
        this.isPressing = isPressing;
    }

    public static void encode(BuildTransformationRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isPressing);
    }

    public static BuildTransformationRequestPacket decode(FriendlyByteBuf buf) {
        return new BuildTransformationRequestPacket(buf.readBoolean());
    }

    public static void handle(BuildTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (msg.isPressing) {
                    handleXKeyPress(player);
                } else {
                    handleXKeyRelease(player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * 处理X键按下事件（服务器端）
     */
    private static void handleXKeyPress(ServerPlayer player) {
        System.out.println("[BuildTrans] X键按下，玩家: " + player.getName().getString());

        // 检查玩家是否装备了BuildDriver腰带
        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player,
                item -> item != null && item.getItem() instanceof BuildDriver);

        if (beltOpt.isPresent()) {
            ItemStack beltStack = beltOpt.get().stack();
            BuildDriver belt = (BuildDriver) beltStack.getItem();
            BuildDriver.BeltMode currentMode = belt.getMode(beltStack);

            // 检查当前模式是否为HAZARD_RT或HAZARD_KR模式
            if (currentMode == BuildDriver.BeltMode.HAZARD_RT || currentMode == BuildDriver.BeltMode.HAZARD_RT_MOULD) {
                System.out.println("[BuildTrans] 进入危险RT模式，开始变身流程");

                // 1. 切换到HAZARD_RT_MOULD模式
                belt.setMode(beltStack, BuildDriver.BeltMode.HAZARD_RT_MOULD);

                // 2. 设置播放mould动画状态
                belt.setIsPlayingMould(beltStack, true);
                belt.setIsPlayingMouldB(beltStack, false);
                belt.setIsTurning(beltStack, true);

                // 3. 同步模式变更到所有玩家
                PacketHandler.sendToAllTracking(
                        new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket(
                                player.getId(), "mould", BuildDriver.BeltMode.HAZARD_RT_MOULD), player);

                // 4. 触发mould动画
                belt.triggerAnim(player, "controller", "mould");

                // 5. 触发玩家turn动画
                belt.triggerPlayerAnim(player, "turn");

                // 6. 延迟3秒后停止SUPER_BEST_MATCH音效并持续播放turn动画
                scheduleDelayedActions(player, belt, beltStack);
            } else if (currentMode == BuildDriver.BeltMode.HAZARD_KR) {
                System.out.println("[BuildTrans] 进入危险KR模式，开始变身流程");

                // 1. 保持HAZARD_KR模式
                // 不需要切换模式，保持当前的HAZARD_KR模式

                // 2. 设置播放mould动画状态
                belt.setIsPlayingMould(beltStack, true);
                belt.setIsPlayingMouldB(beltStack, false);
                belt.setIsTurning(beltStack, true);

                // 3. 同步模式变更到所有玩家
                PacketHandler.sendToAllTracking(
                        new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket(
                                player.getId(), "mould", BuildDriver.BeltMode.HAZARD_KR), player);

                // 4. 触发mould动画
                belt.triggerAnim(player, "controller", "mould");

                // 5. 触发玩家turn动画
                belt.triggerPlayerAnim(player, "turn");

                // 6. 延迟3秒后停止SUPER_BEST_MATCH音效并持续播放turn动画
                scheduleDelayedActions(player, belt, beltStack);
            } else {
                System.out.println("[BuildTrans] 当前模式不是HAZARD_RT或HAZARD_KR: " + currentMode);
            }
        } else {
            System.out.println("[BuildTrans] 未找到BuildDriver腰带");
        }
    }

    /**
     * 调度延迟动作：3秒后停止音效并持续播放turn动画
     */
    private static void scheduleDelayedActions(ServerPlayer player, BuildDriver belt, ItemStack beltStack) {
        // 取消已有的任务
        cancelPlayerTasks(player);
        
        // 获取当前模式
        BuildDriver.BeltMode currentMode = belt.getMode(beltStack);

        // 延迟3秒后执行的动作
        ScheduledFuture<?> superBestMatchTask = scheduler.schedule(() -> {
            try {
                System.out.println("[BuildTrans] 延迟3秒后：停止SUPER_BEST_MATCH音效，持续播放turn动画");

                // 停止播放SUPER_BEST_MATCH音效
                ResourceLocation superBestMatchSoundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "super_best_match"
                );
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), superBestMatchSoundLoc),
                        player
                );

                // 持续保持转动状态
                belt.setIsTurning(beltStack, true);

                // 同步转动状态，保持当前模式
                PacketHandler.sendToAllTracking(
                        new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket(
                                player.getId(), "turn", currentMode), player);

            } catch (Exception e) {
                System.err.println("[BuildTrans] 延迟任务执行错误: " + e.getMessage());
                e.printStackTrace();
            }
        }, 3, TimeUnit.SECONDS);

        playerSoundTasks.put(player, superBestMatchTask);
    }

    /**
     * 处理X键释放事件（服务器端）
     */
    private static void handleXKeyRelease(ServerPlayer player) {
        System.out.println("[BuildTrans] X键释放，玩家: " + player.getName().getString());

        // 1. 停止所有任务
        cancelPlayerTasks(player);

        // 2. 检查玩家是否装备了BuildDriver腰带
        Optional<SlotResult> beltOpt = CurioUtils.findFirstCurio(player,
                item -> item != null && item.getItem() instanceof BuildDriver);

        if (beltOpt.isPresent()) {
                ItemStack beltStack = beltOpt.get().stack();
                BuildDriver belt = (BuildDriver) beltStack.getItem();
                BuildDriver.BeltMode currentMode = belt.getMode(beltStack);

                // 检查当前模式是否为HAZARD_RT_MOULD或HAZARD_KR模式
                if (currentMode == BuildDriver.BeltMode.HAZARD_RT_MOULD || currentMode == BuildDriver.BeltMode.HAZARD_KR) {
                    System.out.println("[BuildTrans] 结束变身流程，播放完成动画和音效");

                    // 3. 停止转动状态
                    belt.setIsTurning(beltStack, false);

                    // 4. 设置播放mould_b动画状态
                    belt.setIsPlayingMouldB(beltStack, true);
                    belt.setIsPlayingMould(beltStack, false);

                    // 5. 同步模式变更到所有玩家，保持当前模式
                    PacketHandler.sendToAllTracking(
                            new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket(
                                    player.getId(), "mould_b", currentMode), player);

                    // 6. 触发mould_b动画
                    belt.triggerAnim(player, "controller", "mould_b");

                    // 7. 触发玩家build_up动画
                    belt.triggerPlayerAnim(player, "build_up");

                    // 8. 直接播放音效不需要复杂的发包，因为音效是全局的，以玩家位置为中心播放
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.HAZARD_HENSHIN.get(),
                            SoundSource.PLAYERS, 1.0F, 1.0F);

                    System.out.println("[BuildTrans] 播放HAZARD_HENSHIN变身音效");

                    // 9. 延迟8秒后穿戴盔甲
                    scheduler.schedule(() -> {
                        try {
                            System.out.println("[BuildTrans] 延迟8秒后装备盔甲");
                            // 检查玩家之前的模式，装备对应的盔甲
                            Optional<SlotResult> beltOpt2 = CurioUtils.findFirstCurio(player,
                                    item -> item != null && item.getItem() instanceof BuildDriver);
                            if (beltOpt2.isPresent()) {
                                ItemStack beltStack2 = beltOpt2.get().stack();
                                BuildDriver belt2 = (BuildDriver) beltStack2.getItem();
                                // 根据当前模式决定装备哪种盔甲
                                BuildDriver.BeltMode beltMode = belt2.getMode(beltStack2);
                                if (beltMode == BuildDriver.BeltMode.HAZARD_KR) {
                                    System.out.println("[BuildTrans] 装备BlackBuildKr盔甲");
                                    // 发送BlackBuildKr变身请求
                                    com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToServer(
                                            new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.BlackBuildKrTransformationRequestPacket(player.getUUID())
                                    );
                                } else {
                                    System.out.println("[BuildTrans] 装备BlackBuild盔甲");
                                    // 装备BlackBuild盔甲
                                    equipBlackBuildArmor(player);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("[BuildTrans] 装备盔甲错误: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }, 8, TimeUnit.SECONDS);
                } else {
                    System.out.println("[BuildTrans] 当前模式不是HAZARD_RT_MOULD: " + currentMode);
                }
        }
    }

    /**
     * 取消玩家的所有任务
     */
    private static void cancelPlayerTasks(ServerPlayer player) {
        ScheduledFuture<?> task = playerSoundTasks.get(player);
        if (task != null && !task.isDone()) {
            task.cancel(true);
            System.out.println("[BuildTrans] 取消玩家的音效任务");
        }
        playerSoundTasks.remove(player);
    }

    /**
     * 清理玩家数据（当玩家退出游戏时调用）
     */
    public static void cleanupPlayer(ServerPlayer player) {
        cancelPlayerTasks(player);
        System.out.println("[BuildTrans] 清理玩家数据: " + player.getName().getString());
    }

    /**
     * 装备BlackBuild盔甲
     */
    private static void equipBlackBuildArmor(ServerPlayer player) {
        System.out.println("[BuildTrans] 装备BlackBuild盔甲");
        // 直接调用BlackBuild变身处理方法
        BlackBuildTransformationRequestPacket.handleBlackBuildTransformation(player);
    }
}