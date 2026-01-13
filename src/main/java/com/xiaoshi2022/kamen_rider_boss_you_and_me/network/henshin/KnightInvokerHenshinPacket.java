package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.KnightInvokerBuckle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.noxknight.NoxKnight;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KnightInvokerSequence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Supplier;

public class KnightInvokerHenshinPacket {
    
    public KnightInvokerHenshinPacket() {
        // 默认构造函数
    }
    
    public static void encode(KnightInvokerHenshinPacket message, FriendlyByteBuf buffer) {
        // 无需编码任何数据
    }
    
    public static KnightInvokerHenshinPacket decode(FriendlyByteBuf buffer) {
        return new KnightInvokerHenshinPacket();
    }
    
    public static void handle(KnightInvokerHenshinPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            
            // 启动变身序列
            KnightInvokerSequence.startHenshin(player);
        });
        ctx.setPacketHandled(true);
    }
}