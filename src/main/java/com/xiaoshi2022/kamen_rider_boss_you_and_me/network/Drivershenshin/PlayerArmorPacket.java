package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerArmorPacket {
    private final int entityId;
    private final ItemStack[] armor;

    public PlayerArmorPacket(int entityId, ItemStack[] armor) {
        this.entityId = entityId;
        this.armor = armor;
    }

    public static void encode(PlayerArmorPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
        for (ItemStack stack : packet.armor) {
            buffer.writeItem(stack);
        }
    }

    public static PlayerArmorPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armor[i] = buffer.readItem();
        }
        return new PlayerArmorPacket(entityId, armor);
    }

    public static void handle(PlayerArmorPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkEvent.Context context = ctx.get();
            // 确保只处理来自服务器的数据包
            if (context.getDirection().getReceptionSide().isClient() && context.getDirection().getOriginationSide().isServer()) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null) {
                    Player targetPlayer = (Player) mc.level.getEntity(packet.entityId);
                    if (targetPlayer != null) {
                        // 使用正确的装备槽枚举值
                        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                        for (int i = 0; i < 4; i++) {
                            targetPlayer.setItemSlot(slots[i], packet.armor[i]);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}