package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class BuildDriverReleasePacket {
    private final UUID playerId;

    public BuildDriverReleasePacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(BuildDriverReleasePacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
    }

    public static BuildDriverReleasePacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        return new BuildDriverReleasePacket(playerId);
    }

    public static void handle(BuildDriverReleasePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                boolean success = handleBuildDriverRelease(player);

                if (success) {
                    // 广播解除变身状态
                    PacketHandler.sendToAllTrackingAndSelf(
                            new SyncTransformationPacket(player.getId(), "NONE", false),
                            player
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean handleBuildDriverRelease(ServerPlayer player) {
        Optional<ItemStack> beltStackOptional = CurioUtils.findFirstCurio(player,
                s -> s.getItem() instanceof BuildDriver)
                .map(curio -> curio.stack());

        if (beltStackOptional.isPresent()) {
            handleBuildDriverReleaseLogic(player, beltStackOptional.get());
            return true; // 解除变身成功
        }
        return false; // 没有腰带，解除失败
    }

    private static void handleBuildDriverReleaseLogic(ServerPlayer player, ItemStack beltStack) {
        BuildDriver belt = (BuildDriver) beltStack.getItem();

        // 开始解除变身动画
        belt.startReleaseAnimation(player, beltStack);

        // 清除变身盔甲
        clearTransformationArmor(player);

        // 给予玩家物品奖励
        player.getInventory().add(new ItemStack(ModItems.HAZARD_TRIGGER.get()));
        player.getInventory().add(new ItemStack(ModItems.RABBIT_ITEM.get()));
        player.getInventory().add(new ItemStack(ModItems.TANK_ITEM.get()));

        // 重置变身状态
        belt.setRelease(beltStack, false);
        // 重置腰带模式为默认
        belt.setMode(beltStack, BuildDriver.BeltMode.DEFAULT);
    }

    private static void clearTransformationArmor(ServerPlayer player) {
        for (int i = 0; i < 4; i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
    }
}
