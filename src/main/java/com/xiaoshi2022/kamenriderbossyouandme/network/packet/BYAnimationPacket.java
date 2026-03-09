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

public record BYAnimationPacket(UUID playerId, String animationId, Integer fadeDuration) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "by_animation");

    public static final Type<BYAnimationPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, BYAnimationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    BYAnimationPacket::playerId,
                    ByteBufCodecs.STRING_UTF8,
                    BYAnimationPacket::animationId,
                    ByteBufCodecs.INT,
                    BYAnimationPacket::fadeDuration,
                    BYAnimationPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
