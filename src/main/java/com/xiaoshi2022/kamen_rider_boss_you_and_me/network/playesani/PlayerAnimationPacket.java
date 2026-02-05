package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PlayerAnimationPacket {
    private final String animationName;
    private final int targetId;
    private final boolean override;
    private final int priority;
    private final int duration;
    private final boolean cancel;

    // 客户端动画状态缓存，用于优先级控制和取消机制
    private static final Map<Integer, ClientAnimationState> clientAnimationStates = new HashMap<>();

    // 构造函数：正常播放动画
    public PlayerAnimationPacket(String animationName, int targetId, boolean override, int priority, int duration) {
        this.animationName = animationName;
        this.targetId = targetId;
        this.override = override;
        this.priority = priority;
        this.duration = duration;
        this.cancel = false;
    }

    // 构造函数：取消动画
    public PlayerAnimationPacket(int targetId) {
        this.animationName = "";
        this.targetId = targetId;
        this.override = false;
        this.priority = 0;
        this.duration = 0;
        this.cancel = true;
    }

    public static void encode(PlayerAnimationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.cancel);
        if (!packet.cancel) {
            buffer.writeUtf(packet.animationName);
            buffer.writeInt(packet.priority);
            buffer.writeInt(packet.duration);
        }
        buffer.writeInt(packet.targetId);
        buffer.writeBoolean(packet.override);
    }

    public static PlayerAnimationPacket decode(FriendlyByteBuf buffer) {
        boolean cancel = buffer.readBoolean();
        if (cancel) {
            int targetId = buffer.readInt();
            buffer.readBoolean(); // 读取override但忽略
            return new PlayerAnimationPacket(targetId);
        } else {
            String animationName = buffer.readUtf();
            int priority = buffer.readInt();
            int duration = buffer.readInt();
            int targetId = buffer.readInt();
            boolean override = buffer.readBoolean();
            return new PlayerAnimationPacket(animationName, targetId, override, priority, duration);
        }
    }

    public static void handle(PlayerAnimationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 处理来自服务器端的数据包（客户端播放动画）
            if (context.getDirection().getReceptionSide().isClient() && context.getDirection().getOriginationSide().isServer()) {
                handleClientAnimation(packet);
            } 
            // 处理来自客户端的数据包（服务器转发动画）
            else if (context.getDirection().getReceptionSide().isServer() && context.getDirection().getOriginationSide().isClient()) {
                handleServerAnimation(packet, context);
            }
        });
        context.setPacketHandled(true);
    }
    
    // 处理服务器端收到的动画数据包（从客户端发送）
    private static void handleServerAnimation(PlayerAnimationPacket packet, NetworkEvent.Context context) {
        // 获取发送数据包的玩家
        net.minecraft.server.level.ServerPlayer sender = context.getSender();
        if (sender != null && !packet.cancel && !packet.animationName.isEmpty()) {
            // 验证发送者是否有权限播放此动画（确保动画ID与发送者ID匹配）
            if (packet.targetId == sender.getId()) {
                // 转发动画到所有跟踪者和自己
                net.minecraft.world.entity.Entity entity = sender.level().getEntity(packet.targetId);
                if (entity != null) {
                    PacketHandler.sendAnimationToAllTrackingAndSelf(packet.animationName, packet.targetId, packet.override, entity, packet.priority, packet.duration);
                }
            }
        }
    }

    private static void handleClientAnimation(PlayerAnimationPacket packet) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null) {
            net.minecraft.world.entity.Entity entity = mc.level.getEntity(packet.targetId);
            if (entity instanceof net.minecraft.client.player.AbstractClientPlayer player) {
                if (packet.cancel) {
                    // 取消指定玩家的动画
                    cancelPlayerAnimation(packet.targetId);
                } else if (!packet.animationName.isEmpty()) {
                    // 检查动画优先级
                    if (shouldPlayAnimation(packet)) {
                        // 更新客户端动画状态
                        updateClientAnimationState(packet);
                        // 播放动画
                        PlayerAnimationSetup.playAnimation(player, packet.animationName, packet.override);
                    }
                }
            }
        }
    }

    // 检查是否应该播放动画（基于优先级和当前状态）
    private static boolean shouldPlayAnimation(PlayerAnimationPacket packet) {
        // 对于变身和解除变身动画，总是允许播放，不管当前动画的优先级如何
        // 这样可以确保动画能够连续播放，不会因为前一个动画未完成而被阻塞
        if (packet.animationName.equals("sodax") || packet.animationName.equals("sodas")) {
            return true;
        }
        
        ClientAnimationState currentState = clientAnimationStates.get(packet.targetId);
        if (currentState == null) {
            return true; // 没有当前动画，直接播放
        }
        
        // 检查是否可以覆盖当前动画
        return packet.override || packet.priority > currentState.priority;
    }

    // 更新客户端动画状态
    private static void updateClientAnimationState(PlayerAnimationPacket packet) {
        // 对于变身和解除变身动画，设置较短的持续时间，确保动画状态能够快速清理
        // 这样后续的动画才能正常播放
        int adjustedDuration = packet.animationName.equals("sodax") || packet.animationName.equals("sodas") ? 2000 : packet.duration;
        
        clientAnimationStates.put(packet.targetId, new ClientAnimationState(
                packet.animationName,
                packet.priority,
                System.currentTimeMillis() + adjustedDuration
        ));
        
        // 启动一个线程，在动画播放完成后清理动画状态
        // 这样可以确保动画状态不会长时间占用，影响后续动画的播放
        new Thread(() -> {
            try {
                Thread.sleep(adjustedDuration);
                if (clientAnimationStates.containsKey(packet.targetId)) {
                    ClientAnimationState state = clientAnimationStates.get(packet.targetId);
                    // 添加null检查，确保state不为null
                    if (state != null && state.animationName.equals(packet.animationName)) {
                        clientAnimationStates.remove(packet.targetId);
                    }
                }
            } catch (InterruptedException e) {
                // 忽略中断异常
            }
        }).start();
    }

    // 取消指定玩家的动画
    private static void cancelPlayerAnimation(int playerId) {
        clientAnimationStates.remove(playerId);
        // 这里可以添加取消动画的逻辑，例如停止当前播放的动画
    }

    // 客户端动画状态类
    private static class ClientAnimationState {
        private final String animationName;
        private final int priority;
        private final long endTime;

        public ClientAnimationState(String animationName, int priority, long endTime) {
            this.animationName = animationName;
            this.priority = priority;
            this.endTime = endTime;
        }
    }
}