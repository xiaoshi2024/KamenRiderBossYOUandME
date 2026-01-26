package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani;

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
            // 只处理来自服务器端的数据包，避免处理客户端发送的数据包
            if (context.getDirection().getReceptionSide().isClient() && context.getDirection().getOriginationSide().isServer()) {
                handleClientAnimation(packet);
            }
        });
        context.setPacketHandled(true);
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
        ClientAnimationState currentState = clientAnimationStates.get(packet.targetId);
        if (currentState == null) {
            return true; // 没有当前动画，直接播放
        }
        
        // 检查是否可以覆盖当前动画
        return packet.override || packet.priority > currentState.priority;
    }

    // 更新客户端动画状态
    private static void updateClientAnimationState(PlayerAnimationPacket packet) {
        clientAnimationStates.put(packet.targetId, new ClientAnimationState(
                packet.animationName,
                packet.priority,
                System.currentTimeMillis() + packet.duration
        ));
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