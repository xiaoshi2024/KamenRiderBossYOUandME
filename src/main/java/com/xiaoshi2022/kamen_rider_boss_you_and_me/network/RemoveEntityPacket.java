package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RemoveEntityPacket {
    private final int entityId;

    public RemoveEntityPacket(int entityId) {
        this.entityId = entityId;
    }

    public RemoveEntityPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                net.minecraft.world.entity.Entity entity = player.level().getEntity(entityId);
                if (entity != null) {
                    entity.discard();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}