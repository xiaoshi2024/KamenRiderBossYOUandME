package com.xiaoshi2022.kamenriderbossyouandme.network.packet;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SoundStopPacket(UUID playerId, ResourceLocation soundId) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "sound_stop");

    public static final Type<SoundStopPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SoundStopPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    SoundStopPacket::playerId,
                    ResourceLocation.STREAM_CODEC,
                    SoundStopPacket::soundId,
                    SoundStopPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}