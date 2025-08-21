package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.melonxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.MelonRiderHenshin;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
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

public class MelonTransformationRequestPacket {

    private final UUID playerId;

    public MelonTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(MelonTransformationRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
    }

    public static MelonTransformationRequestPacket decode(FriendlyByteBuf buf) {
        return new MelonTransformationRequestPacket(buf.readUUID());
    }

    public static void handle(MelonTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                handleMelonTransformation(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleMelonTransformation(ServerPlayer player) {
        // 1. 取得创世纪驱动器
        Optional<SlotResult> genesisDriver = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(item -> item.getItem() instanceof Genesis_driver));

        if (genesisDriver.isEmpty()) return;

        ItemStack beltStack = genesisDriver.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        /* --------------------------------- 重复变身检测 --------------------------------- */
        // 检查是否装备全套蜜瓜装甲
        boolean isMelonArmor = player.getInventory().armor.get(3).getItem() == ModItems.ZANGETSU_SHIN_HELMET.get() &&
                               player.getInventory().armor.get(2).getItem() == ModItems.ZANGETSU_SHIN_CHESTPLATE.get() &&
                               player.getInventory().armor.get(1).getItem() == ModItems.ZANGETSU_SHIN_LEGGINGS.get();
        
        if (isMelonArmor) {
            System.out.println("玩家已身着蜜瓜装甲，忽略再次变身");
            return;
        }

        /* --------------------------------- 检查蜜瓜锁种准备状态 --------------------------------- */
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        if (!variables.melon_ready) {
            player.sendSystemMessage(Component.literal("请先装备蜜瓜锁种！"));
            return;
        }

        /* --------------------------------- 检查腰带模式是否为蜜瓜 --------------------------------- */
        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.MELON) {
            player.sendSystemMessage(Component.literal("腰带未设置为蜜瓜模式！"));
            return;
        }

        /* --------------------------------- 清理蜜瓜特效方块 --------------------------------- */
        clearMelonEntities(player);

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
//        belt.startHenshinAnimation(player,beltStack);   // 腰带自身动画
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "melon_move", belt.getMode(beltStack)),
                player
        );
        /* --------------------------------- 播放变身音效 --------------------------------- */
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModBossSounds.MELONX_ARMS.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);

        /* --------------------------------- 换装 & 标记状态 --------------------------------- */
        MelonRiderHenshin.trigger(player);   // 穿 3 件蜜瓜装甲
        belt.setEquipped(beltStack, false);
        // 设置腰带模式
        belt.setMode(beltStack, Genesis_driver.BeltMode.MELON);
        belt.setHenshin(beltStack, true);
        belt.setShowing(beltStack, false);

// 发送动画包
        belt.startHenshinAnimation(player, beltStack);
        /* --------------------------------- 清除蜜瓜锁种准备状态 --------------------------------- */
        variables.melon_ready = false;
        variables.syncPlayerVariables(player); // 同步变量到客户端

        SlotResult slotResult = genesisDriver.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                beltStack);

        /* --------------------------------- 发送变身成功提示 --------------------------------- */
        player.sendSystemMessage(Component.literal("蜜瓜能量已激活！"));
    }

    /* ========= 清理蜜瓜特效方块 ========= */
    private static void clearMelonEntities(ServerPlayer player) {
        Level level = player.level();
        int radius = 10;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = player.blockPosition().offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof melonxEntity) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }
}