package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.Optional;

public class GhostTransformationRequestPacket {
    private final UUID playerId;

    public GhostTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void buffer(GhostTransformationRequestPacket message, FriendlyByteBuf buffer) {
        buffer.writeUUID(message.playerId);
    }

    public static GhostTransformationRequestPacket newInstance(FriendlyByteBuf buffer) {
        return new GhostTransformationRequestPacket(buffer.readUUID());
    }

    public static void handler(GhostTransformationRequestPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getUUID().equals(message.playerId)) {
                // 获取玩家的PlayerVariables
                player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                    // 检查是否装备了Ghost驱动器
                    if (variables.isGhostDriverEquipped && !variables.isDarkGhostTransformed) {
                        // 从Curio槽位获取腰带
                        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
                                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof GhostDriver));
                        
                        beltOptional.ifPresent(result -> {
                            GhostDriver driver = (GhostDriver) result.stack().getItem();
                            // 检查腰带模式是否为黑暗骑士眼魂模式
                            if (driver.getMode(result.stack()) == GhostDriver.BeltMode.DARK_RIDER_EYE) {
                                // 触发变身
                                variables.isDarkGhostTransformed = true;
                                variables.syncPlayerVariables(player);
                                
                                // 触发变身动画
                                driver.setHenshin(result.stack(), true);
                                driver.triggerAnim(player, "controller", "henshin");
                                
                                // 同步腰带状态到所有跟踪的玩家
                                PacketHandler.sendToAllTracking(
                                        new BeltAnimationPacket(player.getId(), "henshin", driver.getMode(result.stack())),
                                        player);
                            }
                        });
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}