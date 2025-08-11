package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ReleaseBeltPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TransformationRequestPacket {
    private final UUID playerId;
    private final String riderType; // 变身类型
    private final boolean isRelease; // 是否是解除变身请求

    public TransformationRequestPacket(UUID playerId, String riderType, boolean isRelease) {
        this.playerId = playerId;
        this.riderType = riderType;
        this.isRelease = isRelease;
    }

    public static void encode(TransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
        buffer.writeUtf(msg.riderType); // 编码变身类型
        buffer.writeBoolean(msg.isRelease); // 编码是否是解除变身请求
    }

    public static TransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        String riderType = buffer.readUtf(); // 解码变身类型
        boolean isRelease = buffer.readBoolean(); // 解码是否是解除变身请求
        return new TransformationRequestPacket(playerId, riderType, isRelease);
    }

    public static void handle(TransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                if (msg.isRelease) {
                    // 处理解除变身逻辑
                    ReleaseBeltPacket.handleRelease(player, msg.riderType);
                } else {
                    // 根据变身类型调用对应的处理方法
                    if ("LEMON_ENERGY".equals(msg.riderType)) {
                        LemonTransformationRequestPacket.handle(new LemonTransformationRequestPacket(player.getUUID()), ctx);
                    } else if ("BANANA".equals(msg.riderType)) {
                        BananaTransformationRequestPacket.handle(new BananaTransformationRequestPacket(player.getUUID()), ctx);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}