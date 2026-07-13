package com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BuildDriver;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
            Player player = ctx.player();
            if (player == null) return;

            Entity e = player.level().getEntity(packet.entityId());
            if (!(e instanceof LivingEntity living)) return;

            String beltType = packet.beltType();

            CuriosApi.getCuriosInventory(living).ifPresent(inv -> {
                if ("braindriver".equals(beltType)) {
                    inv.findFirstCurio(item -> item.getItem() instanceof BrainDriver).ifPresent(slot -> {
                        ItemStack stack = slot.stack();
                        if (stack.getItem() instanceof BrainDriver brain) {
                            if (!living.level().isClientSide()) return;

                            String animName = packet.animationName();
                            switch (animName) {
                                case "henshin":
                                    brain.setRelease(stack, false);
                                    brain.setShowing(stack, false);
                                    brain.setHenshin(stack, true);
                                    break;
                                case "cancel":
                                    brain.setHenshin(stack, false);
                                    brain.setShowing(stack, false);
                                    brain.setRelease(stack, true);
                                    break;
                                case "show":
                                    brain.setShowing(stack, true);
                                    break;
                                default:
                                    break;
                            }

                            long id = living.getId();
                            brain.triggerAnim(living, id, "controller", animName);

                            KamenRiderBossYOUandME.LOGGER.debug("客户端触发动画: {} -> {}",
                                    living.getName().getString(), animName);
                        }
                    });
                } else if ("builddriver".equals(beltType)) {
                    inv.findFirstCurio(item -> item.getItem() instanceof BuildDriver).ifPresent(slot -> {
                        ItemStack stack = slot.stack();
                        if (stack.getItem() instanceof BuildDriver build) {
                            if (!living.level().isClientSide()) return;

                            String animName = packet.animationName();
                            switch (animName) {
                                case "cancel":
                                    build.setRelease(stack, true);
                                    break;
                                case "show":
                                    build.setShowing(stack, true);
                                    break;
                                case "idles":
                                    build.setShowing(stack, false);
                                    break;
                                case "sync_state":
                                    try {
                                        BuildDriver.BeltMode mode = BuildDriver.BeltMode.valueOf(packet.beltMode());
                                        build.setMode(stack, mode);
                                    } catch (IllegalArgumentException ex) {
                                        build.setMode(stack, BuildDriver.BeltMode.DEFAULT);
                                    }
                                    break;
                                default:
                                    break;
                            }

                            long id = living.getId();
                            build.triggerAnim(living, id, "controller", animName);

                            KamenRiderBossYOUandME.LOGGER.debug("客户端触发动画: {} -> {}",
                                    living.getName().getString(), animName);
                        }
                    });
                }
            });
        });
    }
}