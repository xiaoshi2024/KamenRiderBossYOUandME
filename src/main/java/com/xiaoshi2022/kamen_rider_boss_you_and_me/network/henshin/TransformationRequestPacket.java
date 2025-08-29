package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.KivatBatTwoNdItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ReleaseBeltPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.DarkKivaSequence;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;
import java.util.function.Supplier;

public class TransformationRequestPacket {
    private final UUID playerId;
    private final String riderType; // 变身类型
    private final boolean isRelease; // 是否是解除变身请求

    public TransformationRequestPacket(UUID playerId, String riderType, boolean isRelease) {
        this.playerId = playerId;
        this.riderType = riderType;
        this.isRelease = isRelease;
    }

    public static void encode(TransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
        buffer.writeUtf(msg.riderType); // 编码变身类型
        buffer.writeBoolean(msg.isRelease); // 编码是否是解除变身请求
    }

    public static TransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        String riderType = buffer.readUtf(); // 解码变身类型
        boolean isRelease = buffer.readBoolean(); // 解码是否是解除变身请求
        return new TransformationRequestPacket(playerId, riderType, isRelease);
    }

    public static void handle(TransformationRequestPacket msg,
                              Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getUUID().equals(msg.playerId))
                return;

            if (msg.isRelease) {
                ReleaseBeltPacket.handleRelease(player, msg.riderType);
            } else {
                switch (msg.riderType) {
                    case RiderTypes.LEMON_ENERGY -> LemonTransformationRequestPacket.handle(
                            new LemonTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.BANANA -> BananaTransformationRequestPacket.handle(
                            new BananaTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.MELON_ENERGY -> MelonTransformationRequestPacket.handle(
                            new MelonTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.CHERRY_ENERGY -> CherryTransformationRequestPacket.handle(
                            new CherryTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.ORANGELS -> DarkOrangeTransformationRequestPacket.handle(
                            new DarkOrangeTransformationRequestPacket(player.getUUID()), ctx);
                    case RiderTypes.DRAGONFRUIT_ENERGY -> DragonfruitTransformationRequestPacket.handle(
                            new DragonfruitTransformationRequestPacket(player.getUUID()), ctx);
                    case "DARK_KIVA" -> {
                        if (msg.isRelease) {
                            ReleaseBeltPacket.handleRelease(player, "DARK_KIVA");
                        } else {
                            CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof DrakKivaBelt)
                                    .ifPresent(curio -> {
                                        DrakKivaBelt belt = (DrakKivaBelt) curio.stack().getItem();

                                        // 1. 设置状态（本地）
                                        DrakKivaBelt.setHenshin(curio.stack(), true);

                                        // 2. 让服务端发送动画包给所有附近的客户端
                                        PacketHandler.sendToAllTracking(
                                                new BeltAnimationPacket(player.getId(), "henshin", "drakkiva", DrakKivaBelt.DrakKivaBeltMode.DEFAULT.name()),
                                                player
                                        );
                                    });
                        }
                    }
                    case "KIVAT_BAT_ITEM" -> {
                        if (msg.isRelease) {
                            ReleaseBeltPacket.handleRelease(player, "DARK_KIVA");
                            return;
                        }

                        /* 1. 拿到手里的蝙蝠物品（主手或副手） */
                        ItemStack kivatItem = player.getMainHandItem().getItem() instanceof KivatBatTwoNdItem
                                ? player.getMainHandItem()
                                : player.getOffhandItem();
                        if (kivatItem.isEmpty()) return;

                        CompoundTag tag = kivatItem.getOrCreateTag();
                        UUID oldBatId = tag.hasUUID("UUID") ? tag.getUUID("UUID") : null;

                        /* 2. 如果有旧蝙蝠 UUID，则移除对应实体，保证唯一 */
                        if (oldBatId != null) {
                            ((ServerLevel) player.level())
                                    .getEntitiesOfClass(KivatBatTwoNd.class, player.getBoundingBox().inflate(32))
                                    .stream()
                                    .filter(b -> b.getUUID().equals(oldBatId))
                                    .forEach(Entity::discard);
                        }

                        /* 3. 记录恢复信息（null 表示解除后生成新蝙蝠并驯服） */
                        DarkKivaSequence.BAT_RESTORE_MAP.put(player.getUUID(), oldBatId);

                        /* 4. 给玩家装备腰带并直接变身 */
                        ItemStack belt = new ItemStack(ModItems.DRAK_KIVA_BELT.get());
                        CurioUtils.forceEquipBelt(player, belt);
                        DarkKivaSequence.startHenshin(player);

                        /* 5. 消耗物品 */
                        kivatItem.shrink(1);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}