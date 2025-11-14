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

    public PlayerArmorPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            this.armor[i] = buf.readItem();
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        for (ItemStack stack : armor) {
            buf.writeItem(stack);
        }
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
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}