package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class SoundStopPacket {
    private final int playerId;
    private final ResourceLocation soundName;

    public SoundStopPacket(int playerId, ResourceLocation soundName) {
        this.playerId = playerId;
        this.soundName = soundName;
    }

    // 编码解码方法保持不变
    public static void encode(SoundStopPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
        buffer.writeResourceLocation(msg.soundName);
    }

    public static SoundStopPacket decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readInt();
        ResourceLocation soundName = buffer.readResourceLocation();
        return new SoundStopPacket(playerId, soundName);
    }

    public static class Handler {
        public static void handle(SoundStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection().getReceptionSide().isServer()) {
                    ServerPlayer sender = ctx.get().getSender();

                    // 关键修改1：使用TRACKING_ENTITY_AND_SELF确保包括自己在内的所有相关客户端
                    PacketHandler.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> {
                                if (sender != null) return sender;
                                // 备用方案：通过ID获取实体（需要level实例）
                                ServerPlayer player = (ServerPlayer) ctx.get().getSender();
                                return player != null ? player : null;
                            }),
                            new SoundStopPacket(msg.playerId, msg.soundName)
                    );

                    // 关键修改2：优化命令执行逻辑
                    if (sender != null) {
                        CommandSourceStack source = sender.createCommandSourceStack()
                                .withPermission(2)  // 降低所需权限等级
                                .withSuppressedOutput();

                        // 使用范围参数确保同步
                        String command = String.format(
                                "/stopsound @a[distance=..64] player %s",  // 扩大同步范围
                                msg.soundName.toString()
                        );
                        sender.server.getCommands().performPrefixedCommand(source, command);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class ClientHandler {
        public static void handle(SoundStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null || mc.player == null) return;

                // 关键修改3：停止所有匹配音效，不限定特定玩家
                SoundManager soundManager = mc.getSoundManager();
                for (SoundInstance sound : getPlayingSounds(soundManager)) {
                    if (sound.getSource() == SoundSource.PLAYERS &&
                            sound.getLocation().equals(msg.soundName)) {
                        soundManager.stop(sound);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

        // 优化的反射方法
        private static Collection<SoundInstance> getPlayingSounds(SoundManager manager) {
            try {
                Field field = SoundManager.class.getDeclaredField("instanceToChannel");
                field.setAccessible(true);
                return ((Map<SoundInstance, ?>) field.get(manager)).keySet();
            } catch (Exception e) {
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal("音效停止错误: " + e.getMessage()),
                        false
                );
                return Collections.emptyList();
            }
        }
    }
}