package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 眼魂隐身效果数据包
 * 当玩家手持眼魂按下或松开按键时发送到服务器，触发隐身效果的添加或移除
 */
public class GhostEyeInvisibilityPacket {
    private final boolean isInvisible; // true表示开启隐身，false表示关闭隐身

    public GhostEyeInvisibilityPacket(boolean isInvisible) {
        this.isInvisible = isInvisible;
    }

    public static void encode(GhostEyeInvisibilityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isInvisible);
    }

    public static GhostEyeInvisibilityPacket decode(FriendlyByteBuf buffer) {
        boolean isInvisible = buffer.readBoolean();
        return new GhostEyeInvisibilityPacket(isInvisible);
    }

    public static void handle(GhostEyeInvisibilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 检查玩家是否手持眼魂（在服务器端再次检查，确保安全）
                if (isHoldingGhostEye(player)) {
                    if (packet.isInvisible) {
                        // 添加隐身效果 - 增加持续时间为8分钟(9999刻)，不再需要客户端持续发送数据包维持
                        player.addEffect(new MobEffectInstance(
                                MobEffects.INVISIBILITY,
                                9999, // 8分多钟，不再需要客户端持续发送数据包
                                0,  // 等级1
                                false, // 不显示粒子效果
                                false  // 不显示图标
                        ));
                    } else {
                        // 移除隐身效果
                        player.removeEffect(MobEffects.INVISIBILITY);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    // 检查玩家是否手持眼魂
    private static boolean isHoldingGhostEye(ServerPlayer player) {
        // 检查主手或副手是否持有眼魂
        return player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye ||
                player.getOffhandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye;
    }
}