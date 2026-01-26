package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InvisibilityPacket {
    private final int playerId;
    private final boolean addEffect;

    public InvisibilityPacket(int playerId, boolean addEffect) {
        this.playerId = playerId;
        this.addEffect = addEffect;
    }

    public static void encode(InvisibilityPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
        buffer.writeBoolean(msg.addEffect);
    }

    public static InvisibilityPacket decode(FriendlyByteBuf buffer) {
        return new InvisibilityPacket(buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(InvisibilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                // 服务器端处理
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    if (msg.addEffect) {
                        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1000000, 0, false, false));
                    } else {
                        player.removeEffect(MobEffects.INVISIBILITY);
                    }
                    // 创建一个新的数据包，使用服务器端玩家的 ID，然后广播
                    InvisibilityPacket broadcastPacket = new InvisibilityPacket(player.getId(), msg.addEffect);
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler.sendToAll(broadcastPacket);
                }
            } else if (ctx.get().getDirection().getOriginationSide().isServer()) {
                // 客户端处理，只处理来自服务器端的数据包
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null) {
                    net.minecraft.world.entity.Entity entity = mc.level.getEntity(msg.playerId);
                    if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                        if (msg.addEffect) {
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1000000, 0, false, false));
                        } else {
                            livingEntity.removeEffect(MobEffects.INVISIBILITY);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}