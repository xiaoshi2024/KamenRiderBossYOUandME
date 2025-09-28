package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.BatStampFinishEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record BatStampFinishAnimationPacket(int entityId, boolean startAnimation) {
    public static void encode(BatStampFinishAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.startAnimation);
    }

    public static BatStampFinishAnimationPacket decode(FriendlyByteBuf buf) {
        return new BatStampFinishAnimationPacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(BatStampFinishAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 在客户端执行动画同步
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                try {
                    net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                    // 添加空值检查，防止NPE
                    if (minecraft != null && minecraft.level != null) {
                        Entity entity = minecraft.level.getEntity(msg.entityId);
                        if (entity instanceof BatStampFinishEntity finishEntity) {
                            if (msg.startAnimation) {
                                // 使用公共方法设置动画状态，避免循环调用startFinish()
                                finishEntity.setFinishAnimation(true);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 记录异常但不中断游戏运行
                    org.apache.logging.log4j.LogManager.getLogger(BatStampFinishAnimationPacket.class).error("Failed to handle BatStampFinishAnimationPacket: {}", e.getMessage());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}