package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class PlayerEquipmentPacket {
    private final int entityId;
    private final ItemStack[] armor;
    private final ItemStack bracelet;

    public PlayerEquipmentPacket(int entityId, ItemStack[] armor, ItemStack bracelet) {
        this.entityId = entityId;
        this.armor = armor;
        this.bracelet = bracelet;
    }

    public static void encode(PlayerEquipmentPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
        for (ItemStack stack : packet.armor) {
            buffer.writeItem(stack);
        }
        buffer.writeItem(packet.bracelet);
    }

    public static PlayerEquipmentPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armor[i] = buffer.readItem();
        }
        ItemStack bracelet = buffer.readItem();
        return new PlayerEquipmentPacket(entityId, armor, bracelet);
    }

    public static void handle(PlayerEquipmentPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            NetworkEvent.Context context = ctx.get();
            // 确保只处理来自服务器的数据包
            if (context.getDirection().getReceptionSide().isClient() && context.getDirection().getOriginationSide().isServer()) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.level != null) {
                    Player targetPlayer = (Player) mc.level.getEntity(packet.entityId);
                    if (targetPlayer != null) {
                        // 使用正确的装备槽枚举值，对应关系：armor[0] = 头盔, armor[1] = 胸甲, armor[2] = 护腿, armor[3] = 靴子
                        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                        for (int i = 0; i < 4; i++) {
                            targetPlayer.setItemSlot(slots[i], packet.armor[i]);
                        }
                        // 处理bracelet装备
                        CuriosApi.getCuriosInventory(targetPlayer).ifPresent(curiosInventory -> {
                            curiosInventory.getStacksHandler("bracelet").ifPresent(slotInventory -> {
                                slotInventory.getStacks().setStackInSlot(0, packet.bracelet);
                            });
                        });
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}