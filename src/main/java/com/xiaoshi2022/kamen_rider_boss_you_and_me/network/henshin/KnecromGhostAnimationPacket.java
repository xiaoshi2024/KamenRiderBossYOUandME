package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.KnecromghostEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Supplier;

public record KnecromGhostAnimationPacket(int entityId, boolean startHenshin) {
    public static void encode(KnecromGhostAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.startHenshin);
    }

    public static KnecromGhostAnimationPacket decode(FriendlyByteBuf buf) {
        return new KnecromGhostAnimationPacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(KnecromGhostAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg.entityId, msg.startHenshin));
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(int entityId, boolean startHenshin) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return;
        net.minecraft.world.entity.Entity e = mc.level.getEntity(entityId);
        if (e instanceof KnecromghostEntity ghost) {
            if (startHenshin) {
                // 使用公共方法设置动画状态，避免循环调用startHenshin()
                ghost.setHenshinAnimation(true);
            }
        }
    }
}