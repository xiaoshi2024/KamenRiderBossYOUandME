package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.LemonxEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.HeartCoreEvent;
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

    /**
     * 服务器端直接调用的柠檬变身处理方法
     */
    public static void handleLemonTransformation(ServerPlayer player) {
        Optional<SlotResult> genesisDriver = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof Genesis_driver));

        if (genesisDriver.isPresent()) {
            handleGenesisLemonTransformation(player, genesisDriver.get());
        }
    }

    static void handleGenesisLemonTransformation(ServerPlayer player, SlotResult genesisDriver) {
        ItemStack beltStack = genesisDriver.stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();
        
        // 检查腰带是否处于冷却状态（被瘫痪）
        if (beltStack.hasTag() && beltStack.getTag().contains("cooldownUntil")) {
            long cooldownUntil = beltStack.getTag().getLong("cooldownUntil");
            if (player.level().getGameTime() < cooldownUntil) {
                long remaining = (cooldownUntil - player.level().getGameTime()) / 20;
                player.sendSystemMessage(Component.literal("腰带已被瘫痪！剩余时间：" + remaining + " 秒"));
                return;
            }
        }
        
        // 检查玩家是否已经装备了变身盔甲
        boolean isBaronLemonArmor = player.getInventory().armor.get(3).is(ModItems.BARON_LEMON_HELMET.get()) &&
                player.getInventory().armor.get(2).is(ModItems.BARON_LEMON_CHESTPLATE.get()) &&
                player.getInventory().armor.get(1).is(ModItems.BARON_LEMON_LEGGINGS.get());

        boolean isDukeArmor = player.getInventory().armor.get(3).is(ModItems.DUKE_HELMET.get()) &&
                player.getInventory().armor.get(2).is(ModItems.DUKE_CHESTPLATE.get()) &&
                player.getInventory().armor.get(1).is(ModItems.DUKE_LEGGINGS.get());

        if (isBaronLemonArmor || isDukeArmor) {
            System.out.println("玩家已经装备了变身盔甲，不允许再次变身");
            return;
        }

        // 检测玩家副手是否持有香蕉锁种
        boolean hasBananaLockseed = !player.getOffhandItem().isEmpty() &&
                player.getOffhandItem().getItem() == ModItems.BANANAFRUIT.get();
        
        // 检测玩家是否穿着巴隆香蕉盔甲
        boolean isWearingBaronBananaArmor = com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem.isArmorEquipped(player, ModItems.RIDER_BARONS_HELMET.get()) &&
                com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem.isArmorEquipped(player, ModItems.RIDER_BARONS_CHESTPLATE.get()) &&
                com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_barons.rider_baronsItem.isArmorEquipped(player, ModItems.RIDER_BARONS_LEGGINGS.get());

        // 根据是否持有香蕉锁种或穿着巴隆香蕉盔甲选择不同的变身音效和形态类型
        SoundEvent sound;
        String transformationType;
        if (hasBananaLockseed || isWearingBaronBananaArmor) {
            sound = ModBossSounds.LEMON_BARON.get();
            transformationType = "BARON_LEMON";
        } else {
            // 使用公爵形态音效（假设已存在，如果没有需要添加）
            sound = ModBossSounds.LEMON_BARON.get(); // 临时使用相同音效
            transformationType = "DUKE";
        }

        // 检查柠檬锁种准备状态
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        if (!variables.lemon_ready) {
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
//        belt.startHenshinAnimation(player,beltStack);   // 腰带自身动画
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "lemon_move", belt.getMode(beltStack)),
                player
        );
        // 播放变身音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 1.0F, 1.0F);

        // 触发变身效果，并传入形态类型
        new HeartCoreEvent(player, "LEMON_ENERGY" + ":" + transformationType);

        // 设置玩家为已变身状态
        belt.setEquipped(beltStack, false);
        // 设置腰带模式
        belt.setMode(beltStack, Genesis_driver.BeltMode.LEMON);
        belt.setHenshin(beltStack, true);
        belt.setShowing(beltStack, false);

// 发送动画包
        belt.startHenshinAnimation(player, beltStack);

        // 清除柠檬锁种准备状态
        variables.lemon_ready = false;
        variables.syncPlayerVariables(player); // 同步变量到客户端

        // 写完 NBT 立刻同步回 Curios
        CurioUtils.updateCurioSlot(
                player,
                genesisDriver.slotContext().identifier(),
                genesisDriver.slotContext().index(),
                beltStack);

        // 根据形态类型发送不同的变身成功提示
        if (hasBananaLockseed) {
            player.sendSystemMessage(Component.literal("巴隆柠檬形态已激活！"));
        } else {
            player.sendSystemMessage(Component.literal("公爵形态已激活！"));
        }
        
        // 给予玩家对应的武器（如果配置启用了武器给予功能）
        TransformationWeaponManager.giveWeaponOnGenesisDriverTransformation(player, Genesis_driver.BeltMode.LEMON);
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