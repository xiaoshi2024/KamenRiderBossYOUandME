package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WeaponSyncPacket {
    private final int entityId;
    private final TwoWeaponItem.Variant mainHandVariant;
    private final TwoWeaponItem.Variant offHandVariant;

    public WeaponSyncPacket(int entityId, TwoWeaponItem.Variant mainHandVariant, TwoWeaponItem.Variant offHandVariant) {
        this.entityId = entityId;
        this.mainHandVariant = mainHandVariant;
        this.offHandVariant = offHandVariant;
    }

    public static void encode(WeaponSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeEnum(msg.mainHandVariant);
        buf.writeEnum(msg.offHandVariant);
    }

    public static WeaponSyncPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        TwoWeaponItem.Variant mainHandVariant = buf.readEnum(TwoWeaponItem.Variant.class);
        TwoWeaponItem.Variant offHandVariant = buf.readEnum(TwoWeaponItem.Variant.class);
        return new WeaponSyncPacket(entityId, mainHandVariant, offHandVariant);
    }

    public static void handle(WeaponSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) return;
            Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
            if (!(entity instanceof Player player)) return;

            // 同步主手武器
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof TwoWeaponItem) {
                TwoWeaponItem.setVariant(mainHand, msg.mainHandVariant);
            }

            // 同步副手武器
            ItemStack offHand = player.getOffhandItem();
            if (offHand.getItem() instanceof TwoWeaponItem) {
                TwoWeaponItem.setVariant(offHand, msg.offHandVariant);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}