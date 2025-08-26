package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.Bloodline;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.BloodlineManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBloodlinePacket {
    private final float fangPurity;

    public SyncBloodlinePacket(Bloodline bloodline) {
        this.fangPurity = bloodline.getFangPurity();
    }

    public SyncBloodlinePacket(FriendlyByteBuf buf) {
        this.fangPurity = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(fangPurity);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BloodlineManager.get(player).setFangPurity(fangPurity);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}