package com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 盔甲动画包：保留注册，但动画已改为 Tyrant 物品默认控制器自动播放，无需触发。
 */
public record ArmorAnimationPacket(int entityId, String animationName) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            KamenRiderBossYOUandME.MODID, "armor_animation");
    public static final Type<ArmorAnimationPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorAnimationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ArmorAnimationPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, ArmorAnimationPacket::animationName,
                    ArmorAnimationPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ArmorAnimationPacket packet,
                              net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        // 动画已由 Tyrant 物品默认控制器自动播放，无需处理
    }
}
