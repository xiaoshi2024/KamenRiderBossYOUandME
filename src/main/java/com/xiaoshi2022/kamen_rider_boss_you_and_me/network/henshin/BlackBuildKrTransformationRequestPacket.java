package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class BlackBuildKrTransformationRequestPacket {
    private final UUID playerUUID;
    
    public BlackBuildKrTransformationRequestPacket(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }
    
    public static void encode(BlackBuildKrTransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerUUID);
    }
    
    public static BlackBuildKrTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerUUID = buffer.readUUID();
        return new BlackBuildKrTransformationRequestPacket(playerUUID);
    }
    
    public static void handle(BlackBuildKrTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getUUID().equals(msg.playerUUID)) return;
            
            // 检查玩家是否装备了BuildDriver且模式为HAZARD_KR
            Optional<SlotResult> buildDriverSlot = CurioUtils.findFirstCurio(player,
                    s -> s.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver);
            
            if (buildDriverSlot.isPresent()) {
                // 装备BlackBuildKr盔甲
                equipBlackBuildKrArmor(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    /**
     * 装备BlackBuildKr盔甲
     */
    private static void equipBlackBuildKrArmor(ServerPlayer player) {
        // 装备头盔
        ItemStack helmet = new ItemStack(ModItems.BLACK_BUILD_KR_HELMET.get());
        player.setItemSlot(EquipmentSlot.HEAD, helmet);

        // 装备胸甲
        ItemStack chestplate = new ItemStack(ModItems.BLACK_BUILD_KR_CHESTPLATE.get());
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);

        // 装备护腿
        ItemStack leggings = new ItemStack(ModItems.BLACK_BUILD_KR_LEGGINGS.get());
        player.setItemSlot(EquipmentSlot.LEGS, leggings);
        
        // 同步盔甲变更
        player.inventoryMenu.broadcastChanges();
        
        // 发送客户端提示消息
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("已切换到BlackBuild海贼列车形态！"));
    }
}
