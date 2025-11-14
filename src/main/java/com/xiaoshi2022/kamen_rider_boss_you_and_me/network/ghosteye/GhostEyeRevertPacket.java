package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ghosteye;


import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import tocraft.walkers.api.PlayerShape;

import java.util.function.Supplier;

/**
 * 眼魂实体变回人形的数据包
 * 当玩家按下Shift+B键时发送到服务器，触发形态转换回人形
 */
public class GhostEyeRevertPacket {

    public GhostEyeRevertPacket() {
        // 空构造函数，用于网络传输
    }

    public static void encode(GhostEyeRevertPacket packet, FriendlyByteBuf buffer) {
        // 不需要编码任何数据
    }

    public static GhostEyeRevertPacket decode(FriendlyByteBuf buffer) {
        return new GhostEyeRevertPacket();
    }

    public static void handle(GhostEyeRevertPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 使用PlayerShape API解除变形
                PlayerShape.updateShapes(player, null);
                
                // 获取玩家变量
                KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                
                // 只有当当前飞行控制器是GhostEye时才清除飞行能力，避免影响其他模组的飞行功能
                if (variables.currentFlightController != null && variables.currentFlightController.equals("GhostEye")) {
                    variables.currentFlightController = null;
                    
                    // 禁用飞行能力（如果不是创造模式或旁观者模式）
                    if (!player.isCreative() && !player.isSpectator()) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                    }
                    player.onUpdateAbilities();
                    
                    // 同步变量
                    variables.syncPlayerVariables(player);
                }
                
                // 清除玩家NBT中的眼魂实体状态标记
                CompoundTag playerData = player.getPersistentData();
                playerData.remove("isGhostEyeForm");
                
                // 保留玩家状态和效果，只解除变形
                
                // 给玩家发送提示消息
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("已变回人形！"), true);
            }
        });
        context.setPacketHandled(true);
    }
}