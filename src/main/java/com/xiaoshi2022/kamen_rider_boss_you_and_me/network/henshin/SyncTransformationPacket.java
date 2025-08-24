// SyncTransformationPacket.java
package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.ClientTransformationHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncTransformationPacket {
    private final int playerId;
    private final String form;
    private final boolean isTransforming;

    public SyncTransformationPacket(int playerId, String form, boolean isTransforming) {
        this.playerId = playerId;
        this.form = form;
        this.isTransforming = isTransforming;
    }

    public static void encode(SyncTransformationPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
        buffer.writeUtf(msg.form);
        buffer.writeBoolean(msg.isTransforming);
    }

    public static SyncTransformationPacket decode(FriendlyByteBuf buffer) {
        return new SyncTransformationPacket(
                buffer.readInt(),
                buffer.readUtf(),
                buffer.readBoolean()
        );
    }

    public static void handle(SyncTransformationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 客户端处理变身同步
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                ClientTransformationHandler.handleTransformationSync(msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public int getPlayerId() { return playerId; }
    public String getForm() { return form; }
    public boolean isTransforming() { return isTransforming; }
}