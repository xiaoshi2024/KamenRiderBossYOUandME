package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BuildDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class BlackBuildTransformationRequestPacket {
    private final UUID playerId;

    public BlackBuildTransformationRequestPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public static void encode(BlackBuildTransformationRequestPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerId);
    }

    public static BlackBuildTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        UUID playerId = buffer.readUUID();
        return new BlackBuildTransformationRequestPacket(playerId);
    }

    public static void handle(BlackBuildTransformationRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.getUUID().equals(msg.playerId)) {
                handleBlackBuildTransformation(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * 服务器端直接调用的BlackBuild变身处理方法
     */
    public static void handleBlackBuildTransformation(ServerPlayer player) {
        Optional<SlotResult> buildDriver = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof BuildDriver));

        if (buildDriver.isPresent()) {
            handleBuildDriverBlackBuildTransformation(player, buildDriver.get());
        }
    }

    static void handleBuildDriverBlackBuildTransformation(ServerPlayer player, SlotResult buildDriver) {
        ItemStack beltStack = buildDriver.stack();
        BuildDriver belt = (BuildDriver) beltStack.getItem();

        // 检查玩家是否已经装备了BlackBuild盔甲
        boolean isBlackBuildArmor = player.getInventory().armor.get(3).getItem() == ModItems.BLACK_BUILD_HELMET.get() &&
                player.getInventory().armor.get(2).getItem() == ModItems.BLACK_BUILD_CHESTPLATE.get() &&
                player.getInventory().armor.get(1).getItem() == ModItems.BLACK_BUILD_LEGGINGS.get();

        if (isBlackBuildArmor) {
            System.out.println("玩家已经装备了BlackBuild盔甲，不允许再次变身");
            return;
        }

        // 检查腰带模式是否为HAZARD_RT
        if (belt.getMode(beltStack) != BuildDriver.BeltMode.HAZARD_RT) {
            player.sendSystemMessage(Component.literal("腰带未设置为HAZARD_RT模式！"));
            return;
        }

        // 动作已在客户端松开X键时触发，此处不再重复执行

        // 装备BlackBuild盔甲
        equipBlackBuildArmor(player);

        // 更新状态和变量
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        variables.isBlackBuildTransformed = true;
        variables.syncPlayerVariables(player);

        // 发送变身成功提示
        player.sendSystemMessage(Component.literal("变身！危险BlackBuild！"));

        System.out.println("已触发BlackBuild变身: 玩家=" + player.getName().getString());
    }

    /**
     * 装备BlackBuild盔甲
     */
    private static void equipBlackBuildArmor(ServerPlayer player) {
        // 装备头盔
        ItemStack helmet = new ItemStack(ModItems.BLACK_BUILD_HELMET.get());
        player.setItemSlot(EquipmentSlot.HEAD, helmet);

        // 装备胸甲
        ItemStack chestplate = new ItemStack(ModItems.BLACK_BUILD_CHESTPLATE.get());
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);

        // 装备护腿
        ItemStack leggings = new ItemStack(ModItems.BLACK_BUILD_LEGGINGS.get());
        player.setItemSlot(EquipmentSlot.LEGS, leggings);
    }
}
