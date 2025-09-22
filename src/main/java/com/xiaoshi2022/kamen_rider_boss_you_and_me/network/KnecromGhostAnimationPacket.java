package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public class KnecromGhostAnimationPacket {
    private final int entityId;
    private final boolean start;   // true = 播放变身动画

    public KnecromGhostAnimationPacket(int entityId, boolean start) {
        this.entityId = entityId;
        this.start = start;
    }

    public static void encode(KnecromGhostAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.start);
    }

    public static KnecromGhostAnimationPacket decode(FriendlyByteBuf buf) {
        return new KnecromGhostAnimationPacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(KnecromGhostAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Entity e = Minecraft.getInstance().level.getEntity(msg.entityId);
                if (e instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.KnecromghostEntity ghost) {
                    ghost.startHenshin();   // 客户端执行动画
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}