package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.GhostEyeManager;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.SafeTeleportUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

/**
 * 眼魔维度传送数据包
 * 当玩家手持眼魂按下Shift+V键时发送到服务器，触发主世界和眼魔世界之间的传送
 * 当玩家手持眼魂按下V键时发送到服务器，触发设置眼魔种族
 */
public class GhostEyeDimensionTeleportPacket {
    private final boolean isTeleport; // true表示传送，false表示设置眼魔种族
    
    // 直接在这个类中定义眼魔维度的键
    private static final ResourceKey<Level> GHOST_EYE_DIM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "ghost_eye")
    );

    public GhostEyeDimensionTeleportPacket(boolean isTeleport) {
        this.isTeleport = isTeleport;
    }

    public static void encode(GhostEyeDimensionTeleportPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isTeleport);
    }

    public static GhostEyeDimensionTeleportPacket decode(FriendlyByteBuf buffer) {
        boolean isTeleport = buffer.readBoolean();
        return new GhostEyeDimensionTeleportPacket(isTeleport);
    }

    public static void handle(GhostEyeDimensionTeleportPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 获取发送数据包的玩家
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (packet.isTeleport) {
                    // 执行传送操作
                    if (player.level().dimension() == Level.OVERWORLD) {
                        // 从主世界传送到眼魔世界
                        transferToGhostEyeDimension(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已传送到眼魔世界！"), true);
                    } else if (player.level().dimension() == GHOST_EYE_DIM) {
                        // 从眼魔世界传送到主世界
                        transferToOverworld(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已传送到主世界！"), true);
                    } else {
                        // 其他维度的情况，默认传送到眼魔世界
                        transferToGhostEyeDimension(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已传送到眼魔世界！"), true);
                    }
                } else {
                    // 执行设置眼魔种族操作
                    if (player.level().dimension() == Level.OVERWORLD && isHoldingGhostEye(player)) {
                        setPlayerAsGhostEye(player);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("已获得眼魂种族！"), true);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
    
    // 将玩家传送到眼魔世界
    private static void transferToGhostEyeDimension(ServerPlayer player) {
        // 检查玩家是否持有Ridernecrom手环或相关装备
        if (!hasRidernecromBracelet(player)) {
            player.displayClientMessage(Component.literal("你需要持有手环或眼魂才能传送到眼魔世界！"), true);
            return;
        }

        // 检查玩家是否已经是眼魔状态
        if (GhostEyeManager.isGhostEye(player)) {
            player.displayClientMessage(Component.literal("你已经处于眼魔状态！"), true);
            return;
        }

        ServerLevel ghostEyeLevel = player.server.getLevel(GHOST_EYE_DIM);
        if (ghostEyeLevel == null) return;

        // 创建一个安全的传送位置
        BlockPos teleportPos = new BlockPos(0, 64, 0);
        BlockPos safeTeleportPos = SafeTeleportUtil.findOrCreateSafePosition(ghostEyeLevel, teleportPos);

        // 设置眼魔世界的重生点
        player.setRespawnPosition(GHOST_EYE_DIM, safeTeleportPos, 0.0F, true, false);

        // 使用安全传送工具执行传送
        SafeTeleportUtil.safelyTeleport(player, ghostEyeLevel, safeTeleportPos);

        // 恢复生命值
        player.setHealth(player.getMaxHealth());

        // 使用GhostEyeManager统一设置眼魔状态和相关buff效果
        boolean isFirstTime = !GhostEyeManager.isGhostEye(player);
        GhostEyeManager.setGhostEyeState(player, isFirstTime);
    }


    // 检查玩家是否持有Ridernecrom手环或相关装备
    private static boolean hasRidernecromBracelet(Player player) {
        // 检查手中物品
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean inHands = mainHand.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.MEGA_UIORDER_ITEM.get() ||
                offHand.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.MEGA_UIORDER_ITEM.get() ||
                mainHand.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.NECROM_EYE.get() ||
                offHand.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.NECROM_EYE.get();

        if (inHands) return true;

        // 检查Curios装备槽
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).map(inv -> {
            return inv.findFirstCurio(stack ->
                    stack.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.MEGA_UIORDER_ITEM.get() ||
                            stack.getItem() == com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.NECROM_EYE.get()
            ).isPresent();
        }).orElse(false);
    }

    
    // 将玩家传送到主世界
    private static void transferToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        
        // 使用主世界的出生点或随机位置
        BlockPos spawnPos = overworld.getSharedSpawnPos();
        
        // 使用安全传送工具执行传送
        SafeTeleportUtil.safelyTeleport(player, overworld, spawnPos);
        
        // 使用GhostEyeManager统一移除眼魔状态和相关buff效果
        GhostEyeManager.removeGhostEyeState(player);
    }
    
    // 设置玩家为眼魔种族
    private static void setPlayerAsGhostEye(ServerPlayer player) {
        // 检查玩家是否已经是眼魔状态
        if (GhostEyeManager.isGhostEye(player)) {
            player.displayClientMessage(Component.literal("你已经处于眼魔状态！"), true);
            return;
        }

        // 保存玩家当前位置
        BlockPos currentPos = player.blockPosition();

        // 使用GhostEyeManager统一设置眼魔状态和相关buff效果
        boolean isFirstTime = !GhostEyeManager.isGhostEye(player);
        GhostEyeManager.setGhostEyeState(player, isFirstTime);
    }

    // 检查玩家是否手持眼魂
    private static boolean isHoldingGhostEye(Player player) {
        // 检查主手或副手是否持有眼魂
        if (player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye ||
                player.getOffhandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye) {
            return true;
        }

        // 检查Curios装备槽
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).map(inv -> {
            return inv.findFirstCurio(stack ->
                    stack.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property.Necrom_eye
            ).isPresent();
        }).orElse(false);
    }

}