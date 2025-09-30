package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Supplier;

public class TempRemoveLockSeedPacket {
    
    public TempRemoveLockSeedPacket() {
        // 空构造函数用于网络传输
    }
    
    public void encode(FriendlyByteBuf buffer) {
        // 这个数据包不需要额外的数据，所以encode方法为空
    }
    
    public static TempRemoveLockSeedPacket decode(FriendlyByteBuf buffer) {
        // 解码方法返回新的数据包实例
        return new TempRemoveLockSeedPacket();
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            // 找到玩家装备的创世纪驱动器
            Optional<SlotResult> curioOptional = CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver);
            
            if (curioOptional.isPresent()) {
                SlotResult slotResult = curioOptional.get();
                ItemStack beltStack = slotResult.stack();
                Genesis_driver belt = (Genesis_driver) beltStack.getItem();
                Genesis_driver.BeltMode mode = belt.getMode(beltStack);
                
                // 如果腰带中有锁种（不是默认模式）
                if (mode != Genesis_driver.BeltMode.DEFAULT) {
                    // 创建对应的锁种物品
                    ItemStack lockSeed = null;
                    
                    switch (mode) {
                        case LEMON:
                            lockSeed = new ItemStack(ModItems.LEMON_ENERGY.get());
                            break;
                        case MELON:
                            lockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.MELON.get());
                            break;
                        case CHERRY:
                            lockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY.get());
                            break;
                        case PEACH:
                            lockSeed = new ItemStack(ModItems.PEACH_ENERGY.get());
                            break;
                        case DRAGONFRUIT:
                            lockSeed = new ItemStack(ModItems.DRAGONFRUIT.get());
                            break;
                    }
                    
                    // 如果成功创建了锁种
                    if (lockSeed != null) {
                        // 重置腰带模式为默认，但保持玩家的盔甲（不解除变身）
                        ItemStack newBeltStack = beltStack.copy();
                        belt.setMode(newBeltStack, Genesis_driver.BeltMode.DEFAULT);
                        
                        // 更新腰带槽位
                        CurioUtils.updateCurioSlot(player, 
                                slotResult.slotContext().identifier(),
                                slotResult.slotContext().index(), 
                                newBeltStack);
                        
                        // 播放LOCK OFF音效
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds.LOCKOFF.get(),
                                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                        
                        // 给玩家锁种物品
                        if (!player.getInventory().add(lockSeed)) {
                            // 如果背包满了，将锁种掉落地上
                            player.spawnAtLocation(lockSeed);
                        }
                        
                        // 发送消息提示玩家
                        player.sendSystemMessage(
                                net.minecraft.network.chat.Component.literal("已临时取下锁种，可用于装置音速弓！")
                        );
                    }
                } else {
                    // 如果腰带中没有锁种，发送提示消息
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("腰带中没有锁种可取下！")
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}