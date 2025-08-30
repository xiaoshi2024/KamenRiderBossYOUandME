package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosCapability;

import java.util.function.Supplier;

public class PlayerJoinSyncPacket {
    private final int targetPlayerId; // 目标玩家ID（谁的腰带）
    private final String animationName;
    private final String beltType;
    private final String beltMode;

    public PlayerJoinSyncPacket(int playerId, String animationName, String beltType, String beltMode) {
        this.targetPlayerId = playerId;
        this.animationName = animationName;
        this.beltType = beltType;
        this.beltMode = beltMode;
    }

    // 必须要有这个静态编码方法
    public static void encode(PlayerJoinSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.targetPlayerId);
        buf.writeUtf(msg.animationName);
        buf.writeUtf(msg.beltType);
        buf.writeUtf(msg.beltMode);
    }

    // 必须要有这个静态解码方法
    public static PlayerJoinSyncPacket decode(FriendlyByteBuf buf) {
        int playerId = buf.readInt();
        String animationName = buf.readUtf();
        String beltType = buf.readUtf();
        String beltMode = buf.readUtf();
        return new PlayerJoinSyncPacket(playerId, animationName, beltType, beltMode);
    }

    // encode/decode 方法相应修改...

    public static void handle(PlayerJoinSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) return;

            // 这里的关键：根据 targetPlayerId 找到正确的玩家实体
            Entity targetEntity = Minecraft.getInstance().level.getEntity(msg.targetPlayerId);
            if (!(targetEntity instanceof LivingEntity targetLiving)) return;

            // 只处理目标玩家的腰带
            targetLiving.getCapability(CuriosCapability.INVENTORY).ifPresent(curios ->
                    curios.findCurio("belt", 0).ifPresent(slot -> {
                        ItemStack stack = slot.stack();
                        if (stack.getItem() instanceof DrakKivaBelt dk) {
                            dk.triggerAnim(targetLiving, "controller", msg.animationName);
                        }
                    })
            );
        });
        ctx.get().setPacketHandled(true);
    }
}