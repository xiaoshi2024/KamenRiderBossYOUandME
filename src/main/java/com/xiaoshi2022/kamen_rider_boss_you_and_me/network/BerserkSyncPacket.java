package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.ClientBerserkState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BerserkSyncPacket {
    private final boolean isBerserk;
    
    public BerserkSyncPacket(boolean isBerserk) {
        this.isBerserk = isBerserk;
    }
    
    public static void encode(BerserkSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isBerserk);
    }
    
    public static BerserkSyncPacket decode(FriendlyByteBuf buffer) {
        return new BerserkSyncPacket(buffer.readBoolean());
    }
    
    public static void handle(BerserkSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientBerserkState.setBerserk(packet.isBerserk);
        });
        context.get().setPacketHandled(true);
    }
}