// BuildHenshinKeyPacket.java
package com.xiaoshi2022.kamenriderbossyouandme.network.packet;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler.BuildHenshinKeyHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record BuildHenshinKeyPacket(UUID playerId, boolean pressed) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "build_henshin_key");

    public static final Type<BuildHenshinKeyPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, BuildHenshinKeyPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    BuildHenshinKeyPacket::playerId,
                    ByteBufCodecs.BOOL,
                    BuildHenshinKeyPacket::pressed,
                    BuildHenshinKeyPacket::new
            );

    // ✅ 服务端处理
    public static void handle(BuildHenshinKeyPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player().getServer().getPlayerList().getPlayer(packet.playerId());
            if (player != null) {
                BuildHenshinKeyHandler.handleKeyPress(player, packet.pressed());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}