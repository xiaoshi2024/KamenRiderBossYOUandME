package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.BerserkMovementInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BerserkMovementPacket {
    private final float forwardImpulse;
    private final float leftImpulse;
    private final boolean jumping;
    private final float yaw;
    private final float pitch;
    
    public BerserkMovementPacket(float forwardImpulse, float leftImpulse, boolean jumping, float yaw, float pitch) {
        this.forwardImpulse = forwardImpulse;
        this.leftImpulse = leftImpulse;
        this.jumping = jumping;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public static void encode(BerserkMovementPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.forwardImpulse);
        buffer.writeFloat(packet.leftImpulse);
        buffer.writeBoolean(packet.jumping);
        buffer.writeFloat(packet.yaw);
        buffer.writeFloat(packet.pitch);
    }
    
    public static BerserkMovementPacket decode(FriendlyByteBuf buffer) {
        return new BerserkMovementPacket(
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readBoolean(),
            buffer.readFloat(),
            buffer.readFloat()
        );
    }
    
    public static void handle(BerserkMovementPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                if (player.input instanceof BerserkMovementInput) {
                    BerserkMovementInput input =
                        (BerserkMovementInput) player.input;
                    input.setMovement(packet.forwardImpulse, packet.leftImpulse, packet.jumping);
                }
                
                player.setYRot(packet.yaw);
                player.setXRot(packet.pitch);
            }
        });
        context.get().setPacketHandled(true);
    }
}
