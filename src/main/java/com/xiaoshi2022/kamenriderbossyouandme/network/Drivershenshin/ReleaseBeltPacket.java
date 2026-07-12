package com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record ReleaseBeltPacket(boolean shouldComplete, boolean triggerAnimation, String beltType) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "release_belt");
    public static final Type<ReleaseBeltPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ReleaseBeltPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ReleaseBeltPacket::shouldComplete,
            ByteBufCodecs.BOOL,
            ReleaseBeltPacket::triggerAnimation,
            ByteBufCodecs.STRING_UTF8,
            ReleaseBeltPacket::beltType,
            ReleaseBeltPacket::new
    );

    public ReleaseBeltPacket(boolean shouldComplete, String beltType) {
        this(shouldComplete, !shouldComplete, beltType);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleRelease(ServerPlayer player, String riderType) {
        // 处理BrainDriver解除变身
        if ("BRAIN".equals(riderType)) {
            CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof BrainDriver)
                    .ifPresent(curio -> {
                        BrainDriver belt = (BrainDriver) curio.stack().getItem();
                        // 播放解除变身动画
                        belt.startReleaseAnimation(player, curio.stack());
                    });
        }
    }

    public static void handle(ReleaseBeltPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            if (packet.shouldComplete()) {
                if ("BRAIN".equals(packet.beltType())) {
                    // 处理Brain解除变身
                    handleRelease(player, packet.beltType());
                }
            } else if (packet.triggerAnimation()) {
                if ("BRAIN".equals(packet.beltType())) {
                    CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof BrainDriver)
                            .ifPresent(curio -> {
                                BrainDriver belt = (BrainDriver) curio.stack().getItem();
                                belt.startReleaseAnimation(player, curio.stack());
                            });
                }
            }
        });
    }
}