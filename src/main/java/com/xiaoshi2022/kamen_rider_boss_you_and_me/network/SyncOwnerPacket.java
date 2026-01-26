package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public record SyncOwnerPacket(int entityId, @Nullable UUID ownerId) {
    public static void encode(SyncOwnerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId());
        buffer.writeNullable(packet.ownerId(), FriendlyByteBuf::writeUUID);
    }

    public static SyncOwnerPacket decode(FriendlyByteBuf buffer) {
        return new SyncOwnerPacket(
                buffer.readInt(),
                buffer.readNullable(FriendlyByteBuf::readUUID)
        );
    }

    public static void handle(SyncOwnerPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 只在客户端处理，使用完整类名避免服务器导入错误
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
                if (level != null) {
                    Entity entity = level.getEntity(packet.entityId());
                    if (entity != null && entity instanceof LordBaronEntity baron) {
                        Player owner = packet.ownerId() != null ?
                                level.getPlayerByUUID(packet.ownerId()) : null;
                        baron.setOwner(owner);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}