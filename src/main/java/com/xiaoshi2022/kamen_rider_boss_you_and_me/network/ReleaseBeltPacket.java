package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
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
    private final String beltType; // 新增：腰带类型字段

    // 新增构造函数，支持指定腰带类型
    public ReleaseBeltPacket(boolean shouldComplete, String beltType) {
        this(shouldComplete, !shouldComplete, beltType);
    }

    // 私有主构造函数
    private ReleaseBeltPacket(boolean shouldComplete, boolean triggerAnimation, String beltType) {
        this.shouldComplete = shouldComplete;
        this.triggerAnimation = triggerAnimation;
        this.beltType = beltType;
    }

    public static void handleRelease(ServerPlayer player, String riderType) {
        if ("GENESIS".equals(riderType)) {
            KeybindHandler.completeBeltRelease(player, "GENESIS");
        } else if ("BARONS".equals(riderType)) {
            KeybindHandler.completeBeltRelease(player, "BARONS");
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(shouldComplete);
        buffer.writeBoolean(triggerAnimation);
        buffer.writeUtf(beltType); // 编码腰带类型
    }

    public static ReleaseBeltPacket decode(FriendlyByteBuf buffer) {
        boolean shouldComplete = buffer.readBoolean();
        boolean triggerAnimation = buffer.readBoolean();
        String beltType = buffer.readUtf(); // 解码腰带类型
        return new ReleaseBeltPacket(shouldComplete, triggerAnimation, beltType);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (shouldComplete) {
                // 根据腰带类型调用不同的解除方法
                KeybindHandler.completeBeltRelease(player, beltType);
            } else if (triggerAnimation) {
                // 根据腰带类型处理不同动画
                if ("GENESIS".equals(beltType)) {
                    handleGenesisAnimation(player);
                } else {
                    handleBaronsAnimation(player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // 处理创世纪驱动器动画
    private void handleGenesisAnimation(ServerPlayer player) {
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver)
                .ifPresent(curio -> {
                    Genesis_driver belt = (Genesis_driver) curio.stack().getItem();
                    PacketHandler.sendToAllTracking(
                            new BeltAnimationPacket(
                                    player.getId(),
                                    "start", // 创世纪特有解除动画
                                    belt.getMode(curio.stack())
                            ),
                            player
                    );
                });
    }

    // 处理战极腰带动画
    private void handleBaronsAnimation(ServerPlayer player) {
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
}