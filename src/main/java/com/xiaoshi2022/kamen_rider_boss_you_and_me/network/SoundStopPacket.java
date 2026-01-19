package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class SoundStopPacket {
    private final int playerId;
    private final ResourceLocation soundName;

    public SoundStopPacket(int playerId, ResourceLocation soundName) {
        this.playerId = playerId;
        this.soundName = soundName;
    }

    // 编码方法
    public static void encode(SoundStopPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
        buffer.writeResourceLocation(msg.soundName);
    }

    // 解码方法
    public static SoundStopPacket decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readInt();
        ResourceLocation soundName = buffer.readResourceLocation();
        return new SoundStopPacket(playerId, soundName);
    }

    // 服务器端处理器 - 确保正确发送
    public static class Handler {
        public static void handle(SoundStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (ctx.get().getDirection().getReceptionSide().isServer()) {
                    ServerPlayer sender = ctx.get().getSender();

                    if (sender != null) {
                        // 关键：使用传入的msg，不要重新创建
                        PacketHandler.INSTANCE.send(
                                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> sender),
                                msg  // 直接使用传入的msg，不是new SoundStopPacket(...)
                        );
                        System.out.println("[SoundStop] 服务器发送停止音效包: " + msg.soundName + " for player: " + sender.getName().getString());
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    // 客户端处理器 - 改进版本
    public static class ClientHandler {
        public static void handle(SoundStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null || mc.player == null) return;

                System.out.println("[SoundStop] 客户端收到停止音效: " + msg.soundName);

                SoundManager soundManager = mc.getSoundManager();
                stopSoundByName(soundManager, msg.soundName);
            });
            ctx.get().setPacketHandled(true);
        }

        // 改进的声音停止方法
        private static void stopSoundByName(SoundManager manager, ResourceLocation soundName) {
            try {
                // 方法1：尝试使用反射获取正在播放的声音
                Collection<SoundInstance> playingSounds = getPlayingSoundsByReflection(manager);
                int stoppedCount = 0;

                for (SoundInstance sound : playingSounds) {
                    if (sound.getLocation().equals(soundName)) {
                        manager.stop(sound);
                        stoppedCount++;
                        System.out.println("[SoundStop] 停止音效: " + soundName);
                    }
                }

                if (stoppedCount == 0) {
                    // 尝试另一种方法：直接停止所有音源类别下的该音效
                    stopSoundAllSources(manager, soundName);
                }

            } catch (Exception e) {
                System.err.println("[SoundStop] 停止音效失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 改进的反射方法
        private static Collection<SoundInstance> getPlayingSoundsByReflection(SoundManager manager) {
            try {
                // 尝试不同的字段名
                String[] possibleFieldNames = {
                        "instanceToChannel",
                        "playingSounds",
                        "sounds",
                        "soundSystem"
                };

                for (String fieldName : possibleFieldNames) {
                    try {
                        Field field = SoundManager.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object value = field.get(manager);

                        if (value instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) value;
                            if (!map.isEmpty()) {
                                Set<SoundInstance> sounds = new HashSet<>();
                                for (Object key : map.keySet()) {
                                    if (key instanceof SoundInstance) {
                                        sounds.add((SoundInstance) key);
                                    }
                                }
                                return sounds;
                            }
                        }
                    } catch (NoSuchFieldException ignored) {
                        // 继续尝试下一个字段名
                    }
                }

                // 如果以上都不行，尝试通过SoundEngine
                return getSoundsViaSoundEngine(manager);

            } catch (Exception e) {
                System.err.println("[SoundStop] 反射获取音效失败: " + e.getMessage());
                return Collections.emptyList();
            }
        }

        // 尝试通过SoundEngine获取声音
        private static Collection<SoundInstance> getSoundsViaSoundEngine(SoundManager manager) {
            try {
                // 尝试获取soundEngine字段
                Field engineField = SoundManager.class.getDeclaredField("soundEngine");
                engineField.setAccessible(true);
                Object soundEngine = engineField.get(manager);

                // 尝试获取声音实例
                Field instancesField = soundEngine.getClass().getDeclaredField("instances");
                instancesField.setAccessible(true);
                Map<?, ?> instances = (Map<?, ?>) instancesField.get(soundEngine);

                Set<SoundInstance> sounds = new HashSet<>();
                for (Object sound : instances.keySet()) {
                    if (sound instanceof SoundInstance) {
                        sounds.add((SoundInstance) sound);
                    }
                }
                return sounds;

            } catch (Exception e) {
                System.err.println("[SoundStop] 通过SoundEngine获取音效失败: " + e.getMessage());
                return Collections.emptyList();
            }
        }

        // 更激进的方法：停止所有音源类别下的指定音效
        private static void stopSoundAllSources(SoundManager manager, ResourceLocation soundName) {
            // 遍历所有可能的音源类别
            for (SoundSource source : SoundSource.values()) {
                try {
                    // 尝试停止该音效
                    manager.stop(soundName, source);
                    System.out.println("[SoundStop] 尝试停止音效 (source=" + source + "): " + soundName);
                } catch (Exception e) {
                    // 忽略错误，继续尝试其他音源
                }
            }
        }
    }
}