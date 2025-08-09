package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.BananasEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.LemonxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.HeartCoreEvent;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class TransformationRequestPacket {
    private final int playerId;
    private final String riderType; // 新增字段

    public TransformationRequestPacket(int playerId) {
        this(playerId, "BARONS"); // 默认为BARONS形态
    }

    public TransformationRequestPacket(int playerId, String riderType) {
        this.playerId = playerId;
        this.riderType = riderType;
    }

    public static void encode(TransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerId);
        buffer.writeUtf(msg.riderType); // 编码riderType
    }

    public static TransformationRequestPacket decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readInt();
        String riderType = buffer.readUtf(); // 解码riderType
        return new TransformationRequestPacket(playerId, riderType);
    }

    public static void handle(TransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getId() == msg.playerId) {
                // 服务端验证并处理变身
                handleTransformation(player, msg.riderType); // 添加riderType参数
            }
        });
        ctx.get().setPacketHandled(true);
    }
    private static void handleTransformation(ServerPlayer player, String riderType) {
        // 优先检查创世纪驱动器
        Optional<SlotResult> genesisDriver = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof Genesis_driver));

        if (genesisDriver.isPresent()) {
            handleGenesisTransformation(player, genesisDriver.get(), riderType);
        } else {
            // 原有腰带变身逻辑
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty));

            if (beltOptional.isPresent()) {
                handleBaronsTransformation(player, beltOptional.get());
            }
        }
    }

    // 处理创世纪驱动器变身
    private static void handleGenesisTransformation(ServerPlayer player, SlotResult genesisDriver, String riderType) {
        ItemStack beltStack = genesisDriver.stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        // 检查是否已经变身
        if (belt.isEquipped) {
            return;
        }

        // 如果是柠檬能量形态，清除周围的柠檬实体
        if (riderType.equals("LEMON_ENERGY")) {
            clearLemonsEntities(player);
        }

        // 根据形态播放不同音效
        SoundEvent sound = riderType.equals("LEMON_ENERGY")
                ? ModBossSounds.LEMON_BARON.get()
                : ModBossSounds.BANANAARMS.get();

        belt.startHenshinAnimation(player);

        // 播放全局音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound,
                SoundSource.PLAYERS, 1.0F, 1.0F);

        // 触发变身效果
        new HeartCoreEvent(player, riderType);

        // 设置玩家为已变身状态
        belt.isEquipped = true;
    }

    // 处理原有腰带变身(保持原有逻辑)
    private static void handleBaronsTransformation(ServerPlayer player, SlotResult beltOptional) {
        ItemStack beltStack = beltOptional.stack();
        sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

        if (belt.getMode(beltStack) == sengokudrivers_epmty.BeltMode.BANANA && !player.isShiftKeyDown()) {
            if (belt.isEquipped) {
                return;
            }

            clearBananasEntities(player);

            // 停止待机音效
            PacketHandler.sendToAllTracking(
                    new SoundStopPacket(),
                    player
            );

            // 广播变身动画
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(
                            player.getId(),
                            "cut",
                            sengokudrivers_epmty.BeltMode.BANANA
                    ),
                    player
            );

            // 变身完成包
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(
                            player.getId(),
                            "cut_complete",
                            sengokudrivers_epmty.BeltMode.BANANA
                    ),
                    player
            );

            // 播放音效
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModBossSounds.BANANAARMS.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);

            // 触发变身
            new HeartCoreEvent(player);

            belt.isEquipped = true;
        }
    }

    // 清除BananasEntity(保持不变)
    private static void clearBananasEntities(ServerPlayer player) {
        Level level = player.level();
        int radius = 10;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = player.blockPosition().offset(x, y, z);
                    BlockEntity blockEntity = level.getBlockEntity(pos);

                    if (blockEntity instanceof BananasEntity) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    // 新增方法：清除玩家周围的柠檬实体
    private static void clearLemonsEntities(ServerPlayer player) {
        Level level = player.level();
        int radius = 10; // 清除半径

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