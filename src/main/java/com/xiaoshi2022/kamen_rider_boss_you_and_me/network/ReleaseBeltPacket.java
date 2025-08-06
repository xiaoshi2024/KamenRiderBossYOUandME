package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.KeybindHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReleaseBeltPacket {
    private final boolean shouldComplete;
    private final boolean triggerAnimation;

    public ReleaseBeltPacket(boolean shouldComplete) {
        this.shouldComplete = shouldComplete;
        this.triggerAnimation = !shouldComplete;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(shouldComplete);
        buffer.writeBoolean(triggerAnimation);
    }

    public static ReleaseBeltPacket decode(FriendlyByteBuf buffer) {
        return new ReleaseBeltPacket(buffer.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (shouldComplete) {
                KeybindHandler.completeBeltRelease(player);
            } else if (triggerAnimation) {
                // 广播动画给所有玩家
                CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof sengokudrivers_epmty)
                        .ifPresent(curio -> {
                            sengokudrivers_epmty belt = (sengokudrivers_epmty) curio.stack().getItem();
                            PacketHandler.sendToAllTracking(
                                    new BeltAnimationPacket(
                                            player.getId(),
                                            "release",
                                            belt.getMode(curio.stack())
                                    ),
                                    player
                            );
                        });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}