// network/FusionSkinUpdatePacket.java
package com.xiaoshi2022.kamenriderbossyouandme.network;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.client.skin.SkinState;
import com.xiaoshi2022.kamenriderbossyouandme.entity.FusionEffectEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FusionSkinUpdatePacket(int entityId, ResourceLocation skinTexture, int skinStateCode) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FusionSkinUpdatePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "fusion_skin_update"));

    public static final StreamCodec<FriendlyByteBuf, FusionSkinUpdatePacket> STREAM_CODEC =
            StreamCodec.ofMember(FusionSkinUpdatePacket::write, FusionSkinUpdatePacket::new);

    public FusionSkinUpdatePacket(FriendlyByteBuf buf) {
        this(buf.readInt(),
                buf.readBoolean() ? buf.readResourceLocation() : null,
                buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(skinTexture != null);
        if (skinTexture != null) {
            buf.writeResourceLocation(skinTexture);
        }
        buf.writeInt(skinStateCode);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final FusionSkinUpdatePacket data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player == null) return;

            ServerLevel level = (ServerLevel) player.level();
            Entity entity = level.getEntity(data.entityId());

            if (entity instanceof FusionEffectEntity fusionEntity) {
                if (data.skinTexture() != null) {
                    // 服务端发送皮肤纹理到客户端
                    fusionEntity.setPlayerSkinFromServer(data.skinTexture());
                }
                fusionEntity.setSkinStateFromServer(SkinState.fromCode(data.skinStateCode()));
            }
        });
    }
}