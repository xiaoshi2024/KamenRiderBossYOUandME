package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BerserkAttackPacket {
    public BerserkAttackPacket() {
    }
    
    public static void encode(BerserkAttackPacket packet, FriendlyByteBuf buffer) {
    }
    
    public static BerserkAttackPacket decode(FriendlyByteBuf buffer) {
        return new BerserkAttackPacket();
    }
    
    public static void handle(BerserkAttackPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
            if (player != null) {
                player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
        });
        context.get().setPacketHandled(true);
    }
}
