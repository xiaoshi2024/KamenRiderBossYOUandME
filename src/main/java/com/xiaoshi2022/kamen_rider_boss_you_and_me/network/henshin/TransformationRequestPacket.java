package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.HeartCoreEvent;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Supplier;

public class TransformationRequestPacket {
    private final int playerId;

    public TransformationRequestPacket(int playerId) {
        this.playerId = playerId;
    }

    public static void encode(TransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
    }

    public static TransformationRequestPacket decode(FriendlyByteBuf buffer) {
        return new TransformationRequestPacket(buffer.readInt());
    }

    public static void handle(TransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getId() == msg.playerId) {
                // 服务端验证并处理变身
                handleTransformation(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleTransformation(ServerPlayer player) {
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty));

        if (beltOptional.isPresent()) {
            ItemStack beltStack = beltOptional.get().stack();
            sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

            if (belt.getMode(beltStack) == sengokudrivers_epmty.BeltMode.BANANA && !player.isShiftKeyDown()) {
                // 如果玩家已经处于变身状态，则不允许再次变身
                if (belt.isEquipped) {
                    return;
                }

                // 先停止待机音效
                PacketHandler.sendToAllTracking(
                        new SoundStopPacket(),
                        player
                );

                // 广播变身事件
                PacketHandler.sendToAllTracking(
                        new BeltAnimationPacket(
                                player.getId(),
                                "cut",
                                sengokudrivers_epmty.BeltMode.BANANA
                        ),
                        player
                );

                // 变身完成后发送这个数据包
                PacketHandler.sendToAllTracking(
                        new BeltAnimationPacket(
                                player.getId(),
                                "cut_complete",  // 使用特殊指令表示变身完成
                                sengokudrivers_epmty.BeltMode.BANANA
                        ),
                        player
                );

                // 播放全局音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.BANANAARMS.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);

                // 触发变身效果
                new HeartCoreEvent(player);

                // 设置玩家为已变身状态
                belt.isEquipped = true;
            }
        }
    }
}