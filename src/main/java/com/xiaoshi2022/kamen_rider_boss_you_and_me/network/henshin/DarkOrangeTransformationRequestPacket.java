package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.OrangelsxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.dark_orangels.Dark_orangels;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.HeartCoreEvent;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class DarkOrangeTransformationRequestPacket {
    private final UUID playerId;

    public DarkOrangeTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(DarkOrangeTransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
    }

    public static DarkOrangeTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        return new DarkOrangeTransformationRequestPacket(playerId);
    }

    public static void handle(DarkOrangeTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                boolean success = handleDarkOrangeTransformation(player);

                if (success) {
                    // 广播给所有客户端
                    // 广播给所有客户端
                    // 使用sendToAll确保所有客户端（包括新加入的玩家）都能接收变身状态同步信息
                PacketHandler.sendToAll(
                            new SyncTransformationPacket(player.getId(), "DARK_ORANGE", true)
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean handleDarkOrangeTransformation(ServerPlayer player) {
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty));

        if (beltOptional.isPresent()) {
            handleDarkOrangelsTransformation(player, beltOptional.get());
            return true; // 变身成功
        }
        return false; // 没有腰带，变身失败
    }

    static void handleDarkOrangelsTransformation(ServerPlayer player, SlotResult beltOptional) {
        ItemStack beltStack = beltOptional.stack();
        sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

        // 检查玩家是否已经装备了全套Dark_orangels盔甲
        boolean isFullDarkOrangeArmor = Dark_orangels.isArmorEquipped(player, ModItems.DARK_ORANGELS_HELMET.get()) &&
                                        Dark_orangels.isArmorEquipped(player, ModItems.DARK_ORANGELS_CHESTPLATE.get()) &&
                                        Dark_orangels.isArmorEquipped(player, ModItems.DARK_ORANGELS_LEGGINGS.get());

        if (isFullDarkOrangeArmor) {
            System.out.println("玩家已经装备了Dark_orangels盔甲，不允许再次变身");
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


        // 播放Dark_orangels变身音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.JIMBAR_LEMON.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);

        // 播放变身动画
        PacketHandler.sendToAll(
                new BeltAnimationPacket(player.getId(), "orange_move", sengokudrivers_epmty.BeltMode.ORANGELS));

        // 设置腰带模式为ORANGELS
        belt.setBeltMode(beltStack, sengokudrivers_epmty.BeltMode.ORANGELS);
        belt.setEquipped(beltStack, true);
        belt.setHenshin(beltStack, true);

        clearOLEntities(player);

        belt.setModeAndTriggerCut(player, beltStack, sengokudrivers_epmty.BeltMode.ORANGELS);

        // 触发Dark_orangels变身事件
        new HeartCoreEvent(player, "DARK_ORANGE");

        // 清除橙子锁种准备状态并设置Dark_orangels变身状态
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        variables.orange_ready = false;
        variables.isDarkOrangelsTransformed = true; // 设置Dark_orangels变身状态为true
        variables.syncPlayerVariables(player); // 同步变量到客户端
    }

    private static void clearOLEntities(ServerPlayer player) {
        Level level = player.level();
        int radius = 10;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = player.blockPosition().offset(x, y, z);
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    if (blockEntity instanceof OrangelsxEntity) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }
}