package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerAnimationPacket {
    private final Component animation;
    private final int targetId;
    private final boolean override;

    public PlayerAnimationPacket(Component animation, int targetId, boolean override) {
        this.animation = animation;
        this.targetId = targetId;
        this.override = override;
    }

    public static void encode(PlayerAnimationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeComponent(packet.animation);
        buffer.writeInt(packet.targetId);
        buffer.writeBoolean(packet.override);
    }

    public static PlayerAnimationPacket decode(FriendlyByteBuf buffer) {
        return new PlayerAnimationPacket(
                buffer.readComponent(),
                buffer.readInt(),
                buffer.readBoolean()
        );
    }

    public static void handle(PlayerAnimationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 客户端处理动画
            if (context.getDirection().getReceptionSide().isClient()) {
                handleClientAnimation(packet);
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleClientAnimation(PlayerAnimationPacket packet) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null) {
            net.minecraft.world.entity.Entity entity = mc.level.getEntity(packet.targetId);
            if (entity instanceof net.minecraft.client.player.AbstractClientPlayer player) {
                PlayerAnimationSetup.playAnimation(player, packet.animation.getString(), packet.override);
            }
        }
    }
}