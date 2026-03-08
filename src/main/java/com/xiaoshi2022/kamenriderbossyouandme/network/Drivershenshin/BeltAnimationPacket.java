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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;

public record BeltAnimationPacket(int entityId, String animationName, String beltType, String beltMode) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, "belt_animation");
    public static final Type<BeltAnimationPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, BeltAnimationPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            BeltAnimationPacket::entityId,
            ByteBufCodecs.STRING_UTF8,
            BeltAnimationPacket::animationName,
            ByteBufCodecs.STRING_UTF8,
            BeltAnimationPacket::beltType,
            ByteBufCodecs.STRING_UTF8,
            BeltAnimationPacket::beltMode,
            BeltAnimationPacket::new
    );

    public BeltAnimationPacket(int entityId, String animationName, BrainDriver.BeltMode mode) {
        this(entityId, animationName, "braindriver", mode.name());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(BeltAnimationPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() == null) return;
            if (net.minecraft.client.Minecraft.getInstance().level == null) return;

            Entity e = net.minecraft.client.Minecraft.getInstance().level.getEntity(packet.entityId());
            if (!(e instanceof LivingEntity living)) return;

            CuriosApi.getCuriosInventory(living).ifPresent(inv -> {
                inv.findFirstCurio(item -> item.getItem() instanceof BrainDriver).ifPresent(slot -> {
                    ItemStack stack = slot.stack();
                    Item item = stack.getItem();
                    if (item instanceof BrainDriver brain) {
                        // 修复：使用客户端触发方法
                        brain.triggerClientAnim(living, packet.animationName());
                    }
                });
            });
        });
    }
}