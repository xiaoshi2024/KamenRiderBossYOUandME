package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.KivatBatTwoNdItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.kivat.KivatBatTwoNd;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ReleaseBeltPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.DarkKivaSequence;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
                            // 解除变身
                            ReleaseBeltPacket.handleRelease(player, "DARK_KIVA");
                        } else {
                            // 变身：设置腰带状态并触发动画
                            CurioUtils.findFirstCurio(player,
                                            stack -> stack.getItem() instanceof DrakKivaBelt)
                                    .ifPresent(curio -> {
                                        DrakKivaBelt belt = (DrakKivaBelt) curio.stack().getItem();
                                        DrakKivaBelt.setHenshin(curio.stack(), true);
                                        // 触发动画
                                        belt.triggerAnim(player, "controller", "henshin");
                                    });
                        }
                    }
                    case "KIVAT_BAT_ITEM" -> {
                        // 处理手持月蝠物品的变身逻辑
                        if (!msg.isRelease) {
                            // 检查玩家是否手持月蝠物品
                            if (player.getMainHandItem().getItem() instanceof KivatBatTwoNdItem || 
                                player.getOffhandItem().getItem() instanceof KivatBatTwoNdItem) {
                            
                                // 获取手持的月蝠物品
                                ItemStack kivatItem = ItemStack.EMPTY;
                                if (player.getMainHandItem().getItem() instanceof KivatBatTwoNdItem) {
                                    kivatItem = player.getMainHandItem();
                                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                } else if (player.getOffhandItem().getItem() instanceof KivatBatTwoNdItem) {
                                    kivatItem = player.getOffhandItem();
                                    player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                                }
                            
                                // 生成月蝙蝠实体
                                if (!kivatItem.isEmpty() && player.level() instanceof ServerLevel serverLevel) {
                                    KivatBatTwoNd kivat = ModEntityTypes.KIVAT_BAT_II.get().create(serverLevel);
                                    if (kivat != null) {
                                        // 设置月蝙蝠的位置
                                        kivat.moveTo(player.getX(), player.getY() + 1, player.getZ());
                                        kivat.setDeltaMovement(Vec3.ZERO);
                                        
                                        // 检查物品是否有NBT数据
                                        if (kivatItem.hasTag()) {
                                            // 从物品NBT加载数据
                                            CompoundTag itemTag = kivatItem.getTag();
                                            if (itemTag != null) {
                                                // 复制物品NBT到实体
                                                CompoundTag entityTag = new CompoundTag();
                                                // 保留重要的NBT数据
                                                if (itemTag.hasUUID("UUID")) {
                                                    entityTag.putUUID("UUID", itemTag.getUUID("UUID"));
                                                }
                                                if (itemTag.hasUUID("OwnerUUID")) {
                                                    entityTag.putUUID("OwnerUUID", itemTag.getUUID("OwnerUUID"));
                                                    // 如果有主人UUID，设置为已驯服
                                                    UUID ownerUUID = itemTag.getUUID("OwnerUUID");
                                                    if (player.getUUID().equals(ownerUUID)) {
                                                        kivat.tame(player);
                                                    }
                                                }
                                                if (itemTag.contains("Health")) {
                                                    entityTag.putFloat("Health", itemTag.getFloat("Health"));
                                                }
                                                if (itemTag.contains("CustomName")) {
                                                    entityTag.putString("CustomName", itemTag.getString("CustomName"));
                                                }
                                                
                                                kivat.load(entityTag);
                                            }
                                        } else {
                                            // 如果没有NBT数据，创建新的月蝙蝠并驯服
                                            kivat.tame(player);
                                        }
                                        
                                        // 添加月蝙蝠到世界
                                        serverLevel.addFreshEntity(kivat);
                                    }
                                }
                                
                                // 给玩家穿戴腰带
                                ItemStack belt = new ItemStack(ModItems.DRAK_KIVA_BELT.get());
                                
                                // 尝试将腰带放入Curios槽
                                boolean beltEquipped = CuriosApi.getCuriosInventory(player)
                                        .resolve()
                                        .map(handler -> {
                                            var stacksHandler = handler.getStacksHandler("belt").orElse(null);
                                            if (stacksHandler == null) return false;
                                            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                                                if (stacksHandler.getStacks().getStackInSlot(i).isEmpty()) {
                                                    stacksHandler.getStacks().setStackInSlot(i, belt);
                                                    return true;
                                                }
                                            }
                                            return false;
                                        })
                                        .orElse(false);
                                
                                // 如果Curios槽已满，放入主手
                                if (!beltEquipped) {
                                    player.getInventory().setItem(player.getInventory().selected, belt);
                                }
                                
                                // 触发变身动画和音效
                                DarkKivaSequence.startHenshin((ServerPlayer) player);
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}