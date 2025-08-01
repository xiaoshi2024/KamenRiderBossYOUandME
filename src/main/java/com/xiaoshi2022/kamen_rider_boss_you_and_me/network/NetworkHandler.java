package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    public static void register() {
        // 使用PacketHandler的INSTANCE来注册ReleaseBeltPacket
        PacketHandler.INSTANCE.registerMessage(
                PacketHandler.getNextId(),
                ReleaseBeltPacket.class,
                ReleaseBeltPacket::encode,
                ReleaseBeltPacket::decode,
                ReleaseBeltPacket::handle
        );
    }
}
