package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.tyrant;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.TyrantAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 火龙果虚化技能切换数据包
 * 当玩家按下B键时发送到服务器，触发或解除虚化状态
 */
public class TyrantIntangibilityTogglePacket {

    public TyrantIntangibilityTogglePacket() {
        // 空构造函数，用于网络传输
    }

    public static void encode(TyrantIntangibilityTogglePacket packet, FriendlyByteBuf buffer) {
        // 不需要编码任何数据
    }

    public static TyrantIntangibilityTogglePacket decode(FriendlyByteBuf buffer) {
        return new TyrantIntangibilityTogglePacket();
    }

    public static void handle(TyrantIntangibilityTogglePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 调用火龙果能力处理器中的虚化切换方法
                TyrantAbilityHandler.toggleIntangibility(player);
            }
        });
        context.setPacketHandled(true);
    }
}