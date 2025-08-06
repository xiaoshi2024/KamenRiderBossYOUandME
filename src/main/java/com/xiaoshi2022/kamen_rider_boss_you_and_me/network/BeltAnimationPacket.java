package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.function.Supplier;

public class BeltAnimationPacket {
    private final int entityId;
    private final String animationName;
    private final sengokudrivers_epmty.BeltMode beltMode;

    public BeltAnimationPacket(int entityId, String animationName, sengokudrivers_epmty.BeltMode beltMode) {
        this.entityId = entityId;
        this.animationName = animationName;
        this.beltMode = beltMode;
    }

    public static void encode(BeltAnimationPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.entityId);
        buffer.writeUtf(msg.animationName);
        buffer.writeEnum(msg.beltMode);
    }

    public static BeltAnimationPacket decode(FriendlyByteBuf buffer) {
        return new BeltAnimationPacket(
                buffer.readInt(),
                buffer.readUtf(),
                buffer.readEnum(sengokudrivers_epmty.BeltMode.class)
        );
    }

    public static void handle(BeltAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                if (Minecraft.getInstance().level == null) return;

                Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
                if (!(entity instanceof LivingEntity livingEntity)) return;

                processBeltAnimation(livingEntity, msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void processBeltAnimation(LivingEntity livingEntity, BeltAnimationPacket msg) {
        livingEntity.getCapability(CuriosCapability.INVENTORY).ifPresent(curios -> {
            for (String slotType : curios.getCurios().keySet()) {
                ICuriosItemHandler handler = (ICuriosItemHandler) curios.getCurios().get(slotType);
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getEquippedCurios().getStackInSlot(i);
                    if (stack.getItem() instanceof sengokudrivers_epmty geoItem) {
                        geoItem.handleAnimationPacket(
                                livingEntity,
                                msg.getAnimationName(),
                                msg.getBeltMode()
                        );
                    }
                }
            }
        });
    }

    public int getEntityId() {
        return entityId;
    }

    public String getAnimationName() {
        return animationName;
    }

    public sengokudrivers_epmty.BeltMode getBeltMode() {
        return beltMode;
    }
}