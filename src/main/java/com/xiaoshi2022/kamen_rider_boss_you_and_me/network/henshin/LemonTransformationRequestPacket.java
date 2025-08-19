package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.LemonxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.baron_lemons.baron_lemonItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.HeartCoreEvent;
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
import net.minecraft.sounds.SoundEvent;
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

public class LemonTransformationRequestPacket {
    private final UUID playerId;

    public LemonTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(LemonTransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
    }

    public static LemonTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        return new LemonTransformationRequestPacket(playerId);
    }

    public static void handle(LemonTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                handleLemonTransformation(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleLemonTransformation(ServerPlayer player) {
        Optional<SlotResult> genesisDriver = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof Genesis_driver));

        if (genesisDriver.isPresent()) {
            handleGenesisLemonTransformation(player, genesisDriver.get());
        }
    }

    static void handleGenesisLemonTransformation(ServerPlayer player, SlotResult genesisDriver) {
        ItemStack beltStack = genesisDriver.stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        // 检查玩家是否已经装备了柠檬变身盔甲
        if (baron_lemonItem.isArmorEquipped(player, ModItems.BARON_LEMON_HELMET.get())) {
            System.out.println("玩家已经装备了柠檬变身盔甲，不允许再次变身");
            return;
        }

        // 检查柠檬锁种准备状态
        if (!player.getPersistentData().getBoolean("lemon_ready")) {
            player.sendSystemMessage(Component.literal("请先装备柠檬锁种！"));
            return;
        }

        // 检查腰带模式是否为柠檬
        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.LEMON) {
            player.sendSystemMessage(Component.literal("腰带未设置为柠檬模式！"));
            return;
        }

        clearLemonsEntities(player);

        // 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "lemon_lockonby"
        );
        PacketHandler.sendToAllTracking(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));

        // 播放变身动画
        belt.startHenshinAnimation(player);
        // 动画已由startHenshinAnimation方法内部处理，无需额外发送数据包

        // 播放变身音效
        SoundEvent sound = ModBossSounds.LEMON_BARON.get();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 1.0F, 1.0F);

        // 触发变身效果
        new HeartCoreEvent(player, "LEMON_ENERGY");

        // 设置玩家为已变身状态
        belt.isEquipped = true;
        belt.isHenshining = true;

        // 清除柠檬锁种准备状态
        player.getPersistentData().remove("lemon_ready");
        player.getPersistentData().remove("lemon_ready_time");

        // 发送变身成功提示
        player.sendSystemMessage(Component.literal("柠檬能量已激活！"));
    }

    private static void clearLemonsEntities(ServerPlayer player) {
        Level level = player.level();
        int radius = 10;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = player.blockPosition().offset(x, y, z);
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    if (blockEntity instanceof LemonxEntity) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }
}