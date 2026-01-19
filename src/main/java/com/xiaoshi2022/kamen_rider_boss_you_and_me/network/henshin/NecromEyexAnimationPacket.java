package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.NecromEyexEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;

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
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg.entityId, msg.startAnimation));
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(int entityId, boolean startAnimation) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return;
        net.minecraft.world.entity.Entity e = mc.level.getEntity(entityId);
        if (e instanceof NecromEyexEntity necromEyexEntity) {
            if (startAnimation) {
                // 动画逻辑已简化，现在只有idle动画，不需要额外的设置
                // 空实现，因为我们已经在实体类中简化了动画系统
            }
        }
    }
}