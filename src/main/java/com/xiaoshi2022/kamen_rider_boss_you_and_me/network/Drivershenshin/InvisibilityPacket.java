package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InvisibilityPacket {
    private final boolean addEffect;

    public InvisibilityPacket(boolean addEffect) {
        this.addEffect = addEffect;
    }

    public static void encode(InvisibilityPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.addEffect);
    }

    public static InvisibilityPacket decode(FriendlyByteBuf buffer) {
        return new InvisibilityPacket(buffer.readBoolean());
    }

    public static void handle(InvisibilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (msg.addEffect) {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1000000, 0, false, false));
                } else {
                    player.removeEffect(MobEffects.INVISIBILITY);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}