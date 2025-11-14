package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;
import java.util.function.Supplier;

public class TempRemoveNecromEyePacket {
    
    public TempRemoveNecromEyePacket() {
        // 空构造函数，用于序列化
    }
    
    public static void encode(TempRemoveNecromEyePacket packet, FriendlyByteBuf buffer) {
        // 不需要编码任何数据
    }
    
    public static TempRemoveNecromEyePacket decode(FriendlyByteBuf buffer) {
        return new TempRemoveNecromEyePacket();
    }
    
    public static void handle(TempRemoveNecromEyePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 检查玩家是否装备了Mega_uiorder
                Optional<SlotResult> megaSlot = Optional.empty();
                
                // 使用正确的方式处理Curios Inventory
                LazyOptional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);
                if (curiosInventory.isPresent()) {
                    ICuriosItemHandler handler = curiosInventory.orElseThrow(IllegalStateException::new);
                    megaSlot = handler.findFirstCurio(stack -> stack.getItem() instanceof Mega_uiorder);
                }
                
                if (megaSlot.isPresent()) {
                    ItemStack beltStack = megaSlot.get().stack();
                    Mega_uiorder belt = (Mega_uiorder) beltStack.getItem();
                    
                    // 如果当前模式是NECROM_EYE，切换回DEFAULT模式
                    if (belt.getCurrentMode(beltStack) == Mega_uiorder.Mode.NECROM_EYE) {
                        // 切换回DEFAULT模式
                        belt.switchMode(beltStack, Mega_uiorder.Mode.DEFAULT);
                        
                        // 给玩家一个眼魂物品
                        ItemStack necromEyeStack = new ItemStack(ModItems.NECROM_EYE.get());
                        if (!player.getInventory().add(necromEyeStack)) {
                            // 如果物品栏满了，掉落物品
                            player.drop(necromEyeStack, false);
                        }
                        
                        // 发送同步数据包给客户端
                        PacketHandler.sendToClient(new BeltAnimationPacket(
                                player.getId(), "idle", "mega_uiorder", Mega_uiorder.Mode.DEFAULT.name()), player);
                        
                        // 清除待机状态
                        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                .ifPresent(vars -> {
                                    vars.isNecromStandby = false;
                                    vars.syncPlayerVariables(player);
                                });
                        
                        // 给玩家消息提示
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("眼魂已取下，手环已恢复空白形态！"), true);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}