package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ReleaseBeltPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.CherryTransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TransformationRequestPacket {
    private final UUID playerId;
    private final String riderType; // 变身类型
    private final boolean isRelease; // 是否是解除变身请求

    public TransformationRequestPacket(UUID playerId, String riderType, boolean isRelease) {
        this.playerId = playerId;
        this.riderType = riderType;
        this.isRelease = isRelease;
    }

    public static void encode(TransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
        buffer.writeUtf(msg.riderType); // 编码变身类型
        buffer.writeBoolean(msg.isRelease); // 编码是否是解除变身请求
    }

    public static TransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        String riderType = buffer.readUtf(); // 解码变身类型
        boolean isRelease = buffer.readBoolean(); // 解码是否是解除变身请求
        return new TransformationRequestPacket(playerId, riderType, isRelease);
    }

    public static void handle(TransformationRequestPacket msg,
                              Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getUUID().equals(msg.playerId))
                return;

            if (msg.isRelease) {
                ReleaseBeltPacket.handleRelease(player, msg.riderType);
            } else {
                switch (msg.riderType) {
                    case RiderTypes.LEMON_ENERGY -> LemonTransformationRequestPacket.handle(
                            new LemonTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.BANANA -> BananaTransformationRequestPacket.handle(
                            new BananaTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.MELON_ENERGY -> MelonTransformationRequestPacket.handle(
                            new MelonTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.CHERRY_ENERGY -> CherryTransformationRequestPacket.handle(
                            new CherryTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.ORANGELS -> DarkOrangeTransformationRequestPacket.handle(
                            new DarkOrangeTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.DRAGONFRUIT_ENERGY -> DragonfruitTransformationRequestPacket.handle(
                            new DragonfruitTransformationRequestPacket(player.getUUID()), ctx);
                    case "DARK_KIVA" -> {
                        if (msg.isRelease) {
                            // 解除变身
                            ReleaseBeltPacket.handleRelease(player, "DARK_KIVA");
                        } else {
                            // 变身：设置腰带状态并触发动画
                            CurioUtils.findFirstCurio(player,
                                            stack -> stack.getItem() instanceof DrakKivaBelt)
                                    .ifPresent(curio -> {
                                        DrakKivaBelt belt = (DrakKivaBelt) curio.stack().getItem();
                                        DrakKivaBelt.setHenshin(curio.stack(), true);
                                        // 触发动画
                                        belt.triggerAnim(player, "controller", "henshin");
                                    });
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}