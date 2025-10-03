package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.KeybindHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.DarkKivaSequence;
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
        switch (riderType) {
            case "GENESIS"      -> KeybindHandler.completeBeltRelease(player, "GENESIS");
            case "BARONS"       -> KeybindHandler.completeBeltRelease(player, "BARONS");
            case "MELON_ENERGY" -> KeybindHandler.completeBeltRelease(player, "MELON_ENERGY");
            case "DUKE"         -> KeybindHandler.completeBeltRelease(player, "DUKE");
            case "DARK_KIVA" -> {
                // 直接调用 DarkKivaSequence 的解除方法
                DarkKivaSequence.startDisassembly(player);
            }
            case "EVIL_BATS" -> {
                // 处理Evil Bats解除变身
                KeybindHandler.completeBeltRelease(player, "EVIL_BATS");
            }
            case "RIDERNECROM" -> {
                KeybindHandler.completeBeltRelease(player, "RIDERNECROM");
            }
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
                if ("DARK_KIVA".equals(beltType)) {
                    // Dark Kiva：服务端直接触发解除动画和后续流程
                    DarkKivaSequence.startDisassembly(player);
                } else if ("EVIL_BATS".equals(beltType)) {
                    // 处理Evil Bats解除变身
                    KeybindHandler.completeBeltRelease(player, beltType);
                } else {
                    KeybindHandler.completeBeltRelease(player, beltType);
                }
            } else if (triggerAnimation) {
                if ("GENESIS".equals(beltType)) {
                    CurioUtils.findFirstCurio(player, s -> s.getItem() instanceof Genesis_driver)
                            .ifPresent(curio -> {
                                Genesis_driver belt = (Genesis_driver) curio.stack().getItem();
                                belt.startHenshinAnimation(player, curio.stack());
                            });
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
                    String animationName;
                    switch (belt.getMode(curio.stack())) {
                        case LEMON -> animationName = "start";
                        case MELON -> animationName = "melon_start";
                        case CHERRY -> animationName = "cherry_start";
                        default -> animationName = "start";
                    }
                    PacketHandler.sendToAllTracking(
                            new BeltAnimationPacket(
                                    player.getId(),
                                    animationName,
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