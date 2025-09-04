package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.ORANGEFRUIT;

public class DarkOrangeReleaseRequestPacket {
    private final UUID playerId;

    public DarkOrangeReleaseRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(DarkOrangeReleaseRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
    }

    public static DarkOrangeReleaseRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        return new DarkOrangeReleaseRequestPacket(playerId);
    }

    public static void handle(DarkOrangeReleaseRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                boolean success = handleDarkOrangeRelease(player);

                if (success) {
                    // 广播解除变身状态
                    // 使用sendToAll确保所有客户端（包括新加入的玩家）都能接收解除变身状态同步信息
                    PacketHandler.sendToAll(
                            new SyncTransformationPacket(player.getId(), "NONE", false)
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean handleDarkOrangeRelease(ServerPlayer player) {
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty));

        if (beltOptional.isPresent()) {
            handleDarkOrangelsRelease(player, beltOptional.get());
            return true; // 解除变身成功
        }
        return false; // 没有腰带，解除失败
    }

    private static void clearTransformationArmor(ServerPlayer player) {
        for (int i = 1; i < 4; i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
    }

    static void handleDarkOrangelsRelease(ServerPlayer player, SlotResult beltOptional) {
        ItemStack beltStack = beltOptional.stack();
        sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

        // 检查玩家是否装备了Dark_orangels盔甲
        boolean isDarkOrangeArmorEquipped = Dark_orangels.isArmorEquipped(player, ModItems.DARK_ORANGELS_HELMET.get()) ||
                                           Dark_orangels.isArmorEquipped(player, ModItems.DARK_ORANGELS_CHESTPLATE.get()) ||
                                           Dark_orangels.isArmorEquipped(player, ModItems.DARK_ORANGELS_LEGGINGS.get());

        if (!isDarkOrangeArmorEquipped) {
            System.out.println("玩家未装备Dark_orangels盔甲，无法解除变身");
            return;
        }

        // 停止变身状态下的音效
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "orangeby"
        );
        // 使用sendToAll确保所有客户端（包括新加入的玩家）都能接收音效停止指令
        PacketHandler.sendToAllTracking(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));


        // 播放解除变身音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);

        // 触发解除变身动画
        belt.startReleaseAnimation(player, beltStack);

        // 清除变身装甲
        clearTransformationArmor(player);

        // 更新隐身状态
        com.xiaoshi2022.kamen_rider_boss_you_and_me.util.RiderInvisibilityManager.updateInvisibility(player);

        // 同时返还橘子和柠檬锁种
        belt.setMode(beltStack, sengokudrivers_epmty.BeltMode.DEFAULT);

        // 返还橘子锁种
        ItemStack orangeLockSeed = new ItemStack(ORANGEFRUIT.get());
        orangeLockSeed.getOrCreateTag().putBoolean("isDarkVariant", true);
        if (!player.getInventory().add(orangeLockSeed)) {
            player.spawnAtLocation(orangeLockSeed);
        }

        // 返还柠檬锁种
        ItemStack lemonLockSeed = new ItemStack(ModItems.LEMON_ENERGY.get());
        if (!player.getInventory().add(lemonLockSeed)) {
            player.spawnAtLocation(lemonLockSeed);
        }

        // 重置变身状态
        belt.setHenshin(beltStack, false);
        belt.setEquipped(beltStack, false);
    }
}