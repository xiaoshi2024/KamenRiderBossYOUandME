package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.KnightInvokerBuckle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Supplier;

public class KnightInvokerReleasePacket {
    
    public KnightInvokerReleasePacket() {
        // 默认构造函数
    }
    
    public KnightInvokerReleasePacket(FriendlyByteBuf buf) {
        // 从缓冲区读取数据（这个数据包不需要额外数据）
    }
    
    public void toBytes(FriendlyByteBuf buf) {
        // 写入数据到缓冲区（这个数据包不需要额外数据）
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            boolean success = false;
            // 1. 找到装备的KnightInvokerBuckle
            Optional<SlotResult> slotResultOpt = CurioUtils.findFirstCurio(player,
                            stack -> stack.getItem() instanceof KnightInvokerBuckle);
            
            if (slotResultOpt.isPresent()) {
                success = true;
                SlotResult slotResult = slotResultOpt.get();
                ItemStack beltStack = slotResult.stack();
                KnightInvokerBuckle belt = (KnightInvokerBuckle) beltStack.getItem();
                
                // 停止nox_b音效
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "nox_b"
                );
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player
                );


                // 3. 播放解除音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                
                // 4. 触发解除动画
                belt.startReleaseAnimation(player, beltStack);
                
                // 5. 清空NOX骑士盔甲
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                        ItemStack armorStack = player.getItemBySlot(slot);
                        if (armorStack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight.NoxKnight) {
                            player.setItemSlot(slot, ItemStack.EMPTY);
                        }
                    }
                }
                
                // 6. 重置腰带状态
                belt.setMode(beltStack, KnightInvokerBuckle.BeltMode.DEFAULT);
                belt.setPressState(beltStack, false);
                belt.setRelease(beltStack, false);
                
                // 7. 同步腰带状态 - NOX驱动器装备在背部槽位
                // 使用slotResult.slotContext().index()获取槽位索引
                CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(), slotResult.slotContext().index(), beltStack);
                
                // 8. 给予玩家一个Erase胶囊
                ItemStack eraseCapsem = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.ERASE_CAPSEM.get());
                if (!player.getInventory().add(eraseCapsem)) {
                    player.drop(eraseCapsem, false);
                }
                
                // 9. 收回变身武器
                com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager.clearTransformationWeapons(player);
            }
            
            // 9. 广播解除变身状态 - 关键步骤！
            if (success) {
                // 使用sendToAll确保所有客户端（包括新加入的玩家）都能接收解除变身状态同步信息
                PacketHandler.sendToAll(
                        new SyncTransformationPacket(player.getId(), "NONE", false)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}