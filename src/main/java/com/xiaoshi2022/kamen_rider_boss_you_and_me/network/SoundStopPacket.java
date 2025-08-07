package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SoundStopPacket {
    public static void encode(SoundStopPacket msg, FriendlyByteBuf buffer) {
        // 在这里可以添加需要发送的数据
    }

    public static SoundStopPacket decode(FriendlyByteBuf buffer) {
        // 在这里可以读取发送的数据
        return new SoundStopPacket();
    }

    public static class Handler {
        public static void handle(SoundStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                // 获取当前玩家
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    // 执行服务器端的命令
                    player.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(
                            CommandSource.NULL,
                            player.position(),
                            Vec2.ZERO,
                            (ServerLevel) player.level(),
                            4,
                            "",
                            Component.literal(""),
                            player.getServer(),
                            player
                    ).withSuppressedOutput(), "/stopsound @s player kamen_rider_boss_you_and_me:login_by");
                }
                if (player != null) {
                    // 执行服务器端的命令
                    player.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(
                            CommandSource.NULL,
                            player.position(),
                            Vec2.ZERO,
                            (ServerLevel) player.level(),
                            4,
                            "",
                            Component.literal(""),
                            player.getServer(),
                            player
                    ).withSuppressedOutput(), "/stopsound @s player kamen_rider_boss_you_and_me:bananaby");
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}