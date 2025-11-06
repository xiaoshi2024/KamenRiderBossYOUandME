package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
                            ItemStack beltStack = result.stack();
                            
                            // 如果腰带当前有眼魂存在，取下当前眼魂
                            GhostDriver.BeltMode currentMode = driver.getMode(beltStack);
                            if (currentMode != GhostDriver.BeltMode.DEFAULT) {
                                // 取下当前眼魂 - 只处理拿破仑眼魂模式
                                ItemStack removedEyecon = null;
                                if (currentMode == GhostDriver.BeltMode.NAPOLEON_GHOST) {
                                    // 取下拿破仑眼魂
                                    removedEyecon = new ItemStack(ModItems.NAPOLEON_EYECON.get());
                                }
                                
                                // 如果有眼魂被取下，添加到玩家物品栏
                                if (removedEyecon != null && !player.addItem(removedEyecon)) {
                                    // 如果玩家物品栏已满，将眼魂掉落地上
                                    player.drop(removedEyecon, false);
                                }
                            }
                            
                            // 检查腰带模式是否为黑暗骑士眼魂模式
                            if (driver.getMode(beltStack) == GhostDriver.BeltMode.DARK_RIDER_EYE) {
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