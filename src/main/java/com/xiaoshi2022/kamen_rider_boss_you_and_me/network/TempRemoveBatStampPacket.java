package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TempRemoveBatStampPacket {

    public TempRemoveBatStampPacket() {
        // 空构造函数用于网络传输
    }

    public void encode(FriendlyByteBuf buffer) {
        // 这个数据包不需要额外的数据，所以encode方法为空
    }

    public static TempRemoveBatStampPacket decode(FriendlyByteBuf buffer) {
        // 解码方法返回新的数据包实例
        return new TempRemoveBatStampPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // 检查玩家主手和副手是否拿着TwoWeaponItem的BAT变种
            ItemStack weaponStack = null;
            
            // 检查主手
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof TwoWeaponItem && 
                TwoWeaponItem.getVariant(mainHand) == TwoWeaponItem.Variant.BAT) {
                weaponStack = mainHand;
            }
            
            // 如果主手不是，检查副手
            if (weaponStack == null) {
                ItemStack offHand = player.getOffhandItem();
                if (offHand.getItem() instanceof TwoWeaponItem && 
                    TwoWeaponItem.getVariant(offHand) == TwoWeaponItem.Variant.BAT) {
                    weaponStack = offHand;
                }
            }
            
            if (weaponStack != null) {
                // 创建蝙蝠印章物品
                ItemStack batStamp = new ItemStack(ModItems.BAT_STAMP.get());
                
                // 播放印章取下音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARMOR_EQUIP_NETHERITE,
                        SoundSource.PLAYERS, 1.0F, 0.8F);
                
                // 给玩家蝙蝠印章物品
                if (!player.getInventory().add(batStamp)) {
                    // 如果背包满了，将印章掉落地上
                    player.spawnAtLocation(batStamp);
                }
                
                // 将武器从BAT变种切换回DEFAULT变种
                TwoWeaponItem.setVariant(weaponStack, TwoWeaponItem.Variant.DEFAULT);
                
                // 发送消息提示玩家
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("已从武器中取下蝙蝠印章，可用于释放超音波攻击！")
                );
            } else {
                // 如果玩家手中没有TwoWeaponItem的BAT变种
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("手中未持有装有蝙蝠印章的武器！")
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}