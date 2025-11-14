package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.BananasEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.HeartCoreEvent;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager;
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

public class BananaTransformationRequestPacket {
    private final UUID playerId;

    public BananaTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(BananaTransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
    }

    public static BananaTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        return new BananaTransformationRequestPacket(playerId);
    }

    public static void handle(BananaTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                handleBananaTransformation(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleBananaTransformation(ServerPlayer player) {
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty));

        if (beltOptional.isPresent()) {
            handleBaronsBananaTransformation(player, beltOptional.get());
        }
    }

    static void handleBaronsBananaTransformation(ServerPlayer player, SlotResult beltOptional) {
        ItemStack beltStack = beltOptional.stack();
        sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

        // 检查玩家是否已经装备了全套香蕉变身盔甲
        boolean isFullBananaArmor = rider_baronsItem.isArmorEquipped(player, ModItems.RIDER_BARONS_HELMET.get()) &&
                                    rider_baronsItem.isArmorEquipped(player, ModItems.RIDER_BARONS_CHESTPLATE.get()) &&
                                    rider_baronsItem.isArmorEquipped(player, ModItems.RIDER_BARONS_LEGGINGS.get());
        
        if (isFullBananaArmor) {
            System.out.println("玩家已经装备了香蕉变身盔甲，不允许再次变身");
            return;
        }

        clearBananasEntities(player);

        // 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "bananaby"
        );
        PacketHandler.sendToAllTracking(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));

        // 广播变身动画
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(
                        player.getId(),
                        "cut",
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

        // 给予玩家对应的武器（如果配置启用了武器给予功能）
        TransformationWeaponManager.giveWeaponOnSengokuDriverTransformation(player, sengokudrivers_epmty.BeltMode.BANANA);

        belt.setEquipped(beltStack, true);
        belt.setHenshin(beltStack, true);
    }

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
}