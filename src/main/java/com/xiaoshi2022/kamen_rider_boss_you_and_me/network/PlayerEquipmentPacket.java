package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

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

    public PlayerEquipmentPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            this.armor[i] = buf.readItem();
        }
        this.bracelet = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        for (ItemStack stack : armor) {
            buf.writeItem(stack);
        }
        buf.writeItem(bracelet);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Player targetPlayer = player.level().getPlayerByUUID(player.getUUID());
                if (targetPlayer != null) {
                    for (int i = 0; i < 4; i++) {
                        targetPlayer.setItemSlot(EquipmentSlot.byName(String.valueOf(i)), armor[i]);
                    }
                    CuriosApi.getCuriosInventory(targetPlayer).ifPresent(curiosInventory -> {
                        curiosInventory.getStacksHandler("bracelet").ifPresent(slotInventory -> {
                            slotInventory.getStacks().setStackInSlot(0, bracelet);
                        });
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}