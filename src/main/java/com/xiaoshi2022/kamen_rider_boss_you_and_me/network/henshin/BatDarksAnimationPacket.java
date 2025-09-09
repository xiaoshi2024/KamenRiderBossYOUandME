// BatDarksAnimationPacket.java
package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatDarksEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record BatDarksAnimationPacket(int entityId, boolean startHenshin) {
    public static void encode(BatDarksAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.startHenshin);
    }

    public static BatDarksAnimationPacket decode(FriendlyByteBuf buf) {
        return new BatDarksAnimationPacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(BatDarksAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 在客户端执行动画同步
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Entity entity = net.minecraft.client.Minecraft.getInstance().level.getEntity(msg.entityId);
                if (entity instanceof BatDarksEntity batDarks) {
                    if (msg.startHenshin) {
                        batDarks.startHenshin();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}