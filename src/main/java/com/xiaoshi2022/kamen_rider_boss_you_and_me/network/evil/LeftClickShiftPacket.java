package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.evil;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.UUID;
import java.util.function.Supplier;

public record LeftClickShiftPacket(UUID playerId) {
    public static void encode(LeftClickShiftPacket msg, FriendlyByteBuf buf) { buf.writeUUID(msg.playerId); }
    public static LeftClickShiftPacket decode(FriendlyByteBuf buf) { return new LeftClickShiftPacket(buf.readUUID()); }

    public static void handle(LeftClickShiftPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null || !sp.getUUID().equals(msg.playerId)) return;

            ItemStack stack = sp.getMainHandItem();
            if (!(stack.getItem() instanceof TwoWeaponItem weapon)) return;

            // 切换到刀模式
            weapon.setBladeMode(stack, true);

            // 使用正确的控制器名称触发动画
            weapon.triggerAnim(sp, GeoItem.getOrAssignId(stack, (ServerLevel) sp.level()),
                    "blade_controller", "blade");
            sp.level().playSound(null, sp.blockPosition(),
                    SoundEvents.PISTON_CONTRACT, SoundSource.PLAYERS, 0.8F, 1.0F);
        });
        ctx.get().setPacketHandled(true);
    }
}