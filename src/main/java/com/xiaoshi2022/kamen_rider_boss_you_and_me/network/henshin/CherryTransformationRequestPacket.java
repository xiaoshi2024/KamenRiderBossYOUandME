package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.cherryxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.CherryRiderHenshin;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

public class CherryTransformationRequestPacket {

    private final UUID playerId;

    public CherryTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(CherryTransformationRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
    }

    public static CherryTransformationRequestPacket decode(FriendlyByteBuf buf) {
        return new CherryTransformationRequestPacket(buf.readUUID());
    }

    public static void handle(CherryTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                handleCherryTransformation(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleCherryTransformation(ServerPlayer player) {
        // 1. 检查是否准备好变身
        if (!player.getPersistentData().getBoolean("cherry_ready")) {
            player.displayClientMessage(Component.literal("请先装备樱桃锁种！"), true);
            return;
        }

        // 2. 取得创世纪驱动器
        Optional<SlotResult> genesisDriver = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(item -> item.getItem() instanceof Genesis_driver));

        if (genesisDriver.isEmpty()) return;

        ItemStack beltStack = genesisDriver.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        // 3. 检查腰带模式
        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.CHERRY) {
            player.displayClientMessage(Component.literal("腰带未设置为樱桃模式！"), true);
            return;
        }

        /* --------------------------------- 重复变身检测 --------------------------------- */
        // 这里用樱桃装甲的头盔做判定
        if (player.getInventory().armor.get(3).getItem() == ModItems.SIGURD_HELMET.get()) {
            System.out.println("玩家已身着樱桃装甲，忽略再次变身");
            return;
        }

        /* --------------------------------- 清理樱桃特效方块 --------------------------------- */
        clearCherryEntities(player);

        /* --------------------------------- 停止待机音效 --------------------------------- */
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "lemon_lockonby"
        );
        PacketHandler.sendToAllTracking(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));


        /* --------------------------------- 播放腰带动画 --------------------------------- */
        belt.startHenshinAnimation(player);   // 腰带自身动画
        // 动画已由startHenshinAnimation方法内部处理，无需额外发送数据包

        /* --------------------------------- 播放变身音效 --------------------------------- */
        // 注意：樱桃变身音效
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModBossSounds.CHERRY_ARMS.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);

        /* --------------------------------- 换装 & 标记状态 --------------------------------- */
        CherryRiderHenshin.trigger(player);   // 穿 3 件樱桃装甲
        belt.isEquipped = true;
        belt.isHenshining = true;

        // 清除樱桃就绪标记
        player.getPersistentData().remove("cherry_ready");
        player.getPersistentData().remove("cherry_ready_time");
    }

    /* ========= 清理樱桃特效方块 ========= */
    private static void clearCherryEntities(ServerPlayer player) {
        Level level = player.level();
        int radius = 10;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = player.blockPosition().offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof cherryxEntity) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }
}