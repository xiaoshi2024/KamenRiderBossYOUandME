package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.NecromEyexEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record NecromEyexAnimationPacket(int entityId, boolean startAnimation) {
    public static void encode(NecromEyexAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.startAnimation);
    }

    public static NecromEyexAnimationPacket decode(FriendlyByteBuf buf) {
        return new NecromEyexAnimationPacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(NecromEyexAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 在客户端执行动画同步
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                try {
                    net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                    // 添加空值检查，防止NPE
                    if (minecraft != null && minecraft.level != null) {
                        Entity entity = minecraft.level.getEntity(msg.entityId);
                        if (entity instanceof NecromEyexEntity necromEyexEntity) {
                            if (msg.startAnimation) {
                                // 动画逻辑已简化，现在只有idle动画，不需要额外的设置
                                // 空实现，因为我们已经在实体类中简化了动画系统
                            }
                        }
                    }
                } catch (Exception e) {
                    // 记录异常但不中断游戏运行
                    org.apache.logging.log4j.LogManager.getLogger(NecromEyexAnimationPacket.class).error("Failed to handle NecromEyexAnimationPacket: {}", e.getMessage());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}