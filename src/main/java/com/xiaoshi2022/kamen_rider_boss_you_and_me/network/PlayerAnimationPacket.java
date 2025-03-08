package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayerAnimationPacket {
    private UUID playerUUID;
    private ResourceLocation animation;

    public PlayerAnimationPacket(UUID playerUUID, ResourceLocation animation) {
        this.playerUUID = playerUUID;
        this.animation = animation;
    }

    public PlayerAnimationPacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.animation = buf.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeResourceLocation(animation);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getUUID().equals(playerUUID)) {
                // 调用动画播放方法
                PlayerAnimationSetup.playAnimation((ServerLevel) player.level(), player, animation);
            }
        });
        context.setPacketHandled(true);
    }
}