package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.KnightInvokerBuckle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Supplier;

public class KnightInvokerPressPacket {
    
    public KnightInvokerPressPacket() {
        // 默认构造函数
    }
    
    public static void encode(KnightInvokerPressPacket message, FriendlyByteBuf buffer) {
        // 无需编码任何数据
    }
    
    public static KnightInvokerPressPacket decode(FriendlyByteBuf buffer) {
        return new KnightInvokerPressPacket();
    }
    
    public static void handle(KnightInvokerPressPacket message, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            
            // 检查玩家是否装备了KnightInvokerBuckle且处于NOX模式
            Optional<ItemStack> beltStackOpt = CurioUtils.findFirstCurio(player,
                    stack -> stack.getItem() instanceof KnightInvokerBuckle)
                    .map(SlotResult::stack);
            
            if (beltStackOpt.isPresent()) {
                ItemStack beltStack = beltStackOpt.get();
                KnightInvokerBuckle belt = (KnightInvokerBuckle) beltStack.getItem();
                
                // 只有NOX模式下才播放音效
                // 移除isPressed检查，因为客户端设置的状态不会立即同步到服务端
                if (belt.getMode(beltStack) == KnightInvokerBuckle.BeltMode.NOX) {
                    // 停止nox_a音效
                    ResourceLocation noxASoundLoc = new ResourceLocation(
                            "kamen_rider_boss_you_and_me",
                            "nox_a"
                    );
                    // 只发送给当前玩家
                PacketHandler.sendToClient(
                        new SoundStopPacket(player.getId(),noxASoundLoc),
                        player
                );

                    // 播放erase音效
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.ERASE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 播放NOX_B音效
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.NOX_B.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 手动在服务端设置Press状态为true，确保状态一致
                    belt.setPressState(beltStack, true);
                    
                    // 播放玩家动画 - pressone
                    com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KnightInvokerSequence.playPlayerAnimation(player, "pressone");
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}