package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.cherryxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.CherryRiderHenshin;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager;
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

    /**
     * 服务器端直接调用的樱桃变身处理方法
     */
    public static void handleCherryTransformation(ServerPlayer player) {
        // 1. 检查是否准备好变身
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        if (!variables.cherry_ready) {
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
        // 检查是否装备全套樱桃装甲
        boolean isCherryArmor = player.getInventory().armor.get(3).getItem() == ModItems.SIGURD_HELMET.get() &&
                                player.getInventory().armor.get(2).getItem() == ModItems.SIGURD_CHESTPLATE.get() &&
                                player.getInventory().armor.get(1).getItem() == ModItems.SIGURD_LEGGINGS.get();
        
        if (isCherryArmor) {
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
//        belt.startHenshinAnimation(player, beltStack);   // 腰带自身动画

        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "cherry_move", belt.getMode(beltStack)),
                player
        );
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
        belt.setEquipped(beltStack, false);

// 设置腰带模式
        belt.setMode(beltStack, Genesis_driver.BeltMode.CHERRY);
        belt.setHenshin(beltStack, true);
        belt.setShowing(beltStack, false);

// 发送动画包
        belt.startHenshinAnimation(player, beltStack);

        SlotResult slotResult = genesisDriver.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                beltStack);

        // 清除樱桃就绪标记
        variables.cherry_ready = false;
        variables.syncPlayerVariables(player);
        
        // 给予玩家对应的武器（如果配置启用了武器给予功能）
        TransformationWeaponManager.giveWeaponOnGenesisDriverTransformation(player, Genesis_driver.BeltMode.CHERRY);
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