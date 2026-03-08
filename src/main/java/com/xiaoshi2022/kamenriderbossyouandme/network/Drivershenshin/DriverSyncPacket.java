package com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

public record DriverSyncPacket(int entityId, BrainDriver.BeltMode beltMode) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "driver_sync");
    public static final Type<DriverSyncPacket> TYPE = new Type<>(ID);

    // 正确的枚举编解码器，使用 RegistryFriendlyByteBuf
    private static final StreamCodec<RegistryFriendlyByteBuf, BrainDriver.BeltMode> BELT_MODE_STREAM_CODEC =
            new StreamCodec<RegistryFriendlyByteBuf, BrainDriver.BeltMode>() {
                @Override
                public BrainDriver.BeltMode decode(RegistryFriendlyByteBuf buf) {
                    return BrainDriver.BeltMode.values()[buf.readInt()];
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, BrainDriver.BeltMode mode) {
                    buf.writeInt(mode.ordinal());
                }
            };

    public static final StreamCodec<RegistryFriendlyByteBuf, DriverSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            DriverSyncPacket::entityId,
            BELT_MODE_STREAM_CODEC,
            DriverSyncPacket::beltMode,
            DriverSyncPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DriverSyncPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() == null) return;
            if (net.minecraft.client.Minecraft.getInstance().level == null) return;
            Entity e = net.minecraft.client.Minecraft.getInstance().level.getEntity(packet.entityId());
            if (e == null || !(e instanceof LivingEntity living)) return;

            CuriosApi.getCuriosInventory(living).ifPresent(inv ->
                    inv.findFirstCurio(item -> item.getItem() instanceof BrainDriver)
                            .ifPresent(slot -> {
                                ItemStack stack = slot.stack();

                                BrainDriver brainDriver = (BrainDriver) stack.getItem();
                                brainDriver.setMode(stack, packet.beltMode());
                            })
            );
        });
    }
}