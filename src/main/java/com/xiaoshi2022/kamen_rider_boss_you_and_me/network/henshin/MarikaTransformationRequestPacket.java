package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.PeachxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.MarikaRiderHenshin;
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

public class MarikaTransformationRequestPacket {

    private final UUID playerId;

    public MarikaTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(MarikaTransformationRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerId);
    }

    public static MarikaTransformationRequestPacket decode(FriendlyByteBuf buf) {
        return new MarikaTransformationRequestPacket(buf.readUUID());
    }

    public static void handle(MarikaTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                handleMarikaTransformation(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleMarikaTransformation(ServerPlayer player) {
        // 1. 检查是否准备好变身
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        if (!variables.peach_ready) {
            player.displayClientMessage(Component.literal("请先装备桃子锁种！"), true);
            return;
        }

        // 2. 取得创世纪驱动器
        Optional<SlotResult> genesisDriver = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(item -> item.getItem() instanceof Genesis_driver));

        System.out.println(">>>>> 1. found belt = " + genesisDriver.isPresent());

        if (genesisDriver.isEmpty()) return;

        ItemStack beltStack = genesisDriver.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        System.out.println(">>>>> 2. mode check = " + belt.getMode(beltStack));

        // 3. 检查腰带模式
        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.PEACH) {
            player.displayClientMessage(Component.literal("腰带未设置为桃子模式！"), true);
            return;
        }

        /* --------------------------------- 重复变身检测 --------------------------------- */
        // 检查是否装备全套Marika装甲
        boolean isMarikaArmor = player.getInventory().armor.get(3).getItem() == ModItems.MARIKA_HELMET.get() &&
                                player.getInventory().armor.get(2).getItem() == ModItems.MARIKA_CHESTPLATE.get() &&
                                player.getInventory().armor.get(1).getItem() == ModItems.MARIKA_LEGGINGS.get();

        if (isMarikaArmor) {
            System.out.println("玩家已身着Marika装甲，忽略再次变身");
            return;
        }

        System.out.println(">>>>> 3. armor check = " + isMarikaArmor);

        /* --------------------------------- 清理桃子特效方块 --------------------------------- */
        clearPeachEntities(player);

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

        System.out.println(">>>>> 4. will call startHenshinAnimation");

        /* --------------------------------- 播放腰带动画 --------------------------------- */
//        belt.startHenshinAnimation(player,beltStack);   // 腰带自身动画
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "peach_move", belt.getMode(beltStack)),
                player
        );
        /* --------------------------------- 播放变身音效 --------------------------------- */
        // 注意：桃子变身音效
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModBossSounds.PEACH_ARMS.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);

        /* --------------------------------- 换装 & 标记状态 --------------------------------- */
        MarikaRiderHenshin.trigger(player);   // 穿 3 件Marika装甲
        belt.setEquipped(beltStack, false);
        // 设置腰带模式
        belt.setMode(beltStack, Genesis_driver.BeltMode.PEACH);
        belt.setHenshin(beltStack, true);
        belt.setShowing(beltStack, false);

// 发送动画包
        belt.startHenshinAnimation(player, beltStack);

        // 清除桃子就绪标记
        variables.peach_ready = false;
        variables.syncPlayerVariables(player); // 同步变量到客户端

        // 写完 NBT 立刻同步回 Curios
        SlotResult slotResult = genesisDriver.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                beltStack);

        // 通知玩家获得的新能力
        player.sendSystemMessage(Component.literal("桃子模式已激活！获得以下能力："));
        player.sendSystemMessage(Component.literal("1. 每5秒自动恢复1点生命值"));
        player.sendSystemMessage(Component.literal("2. 受到伤害时有20%几率触发额外治愈效果"));
        
        // 给予玩家对应的武器（如果配置启用了武器给予功能）
        TransformationWeaponManager.giveWeaponOnGenesisDriverTransformation(player, Genesis_driver.BeltMode.PEACH);
    }

    /* ========= 清理桃子特效方块 ========= */
    private static void clearPeachEntities(ServerPlayer player) {
        Level level = player.level();
        int radius = 10;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = player.blockPosition().offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof PeachxEntity) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }
}