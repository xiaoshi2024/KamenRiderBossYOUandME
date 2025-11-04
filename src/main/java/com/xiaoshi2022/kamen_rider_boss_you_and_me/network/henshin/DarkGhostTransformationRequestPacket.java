package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.DarkGhostRiderHenshin;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.function.Supplier;

public class DarkGhostTransformationRequestPacket {
    private final boolean isRelease;

    // 为了兼容客户端调用，添加一个不需要参数的构造函数
    public DarkGhostTransformationRequestPacket() {
        this(false); // 默认不是解除变身
    }

    public DarkGhostTransformationRequestPacket(boolean isRelease) {
        this.isRelease = isRelease;
    }

    public static void encode(DarkGhostTransformationRequestPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isRelease);
    }

    public static DarkGhostTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        return new DarkGhostTransformationRequestPacket(buffer.readBoolean());
    }

    public static void handle(DarkGhostTransformationRequestPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // 检查玩家是否装备了魂灵驱动器
            SlotResult ghostDriver = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof GhostDriver))
                    .orElse(null);

            if (ghostDriver == null) {
                player.displayClientMessage(Component.literal("你需要装备魂灵驱动器才能变身！"), true);
                return;
            }

            ItemStack beltStack = ghostDriver.stack();
            GhostDriver belt = (GhostDriver) beltStack.getItem();

            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());

            if (!message.isRelease) {
                // 变身逻辑
                // 检查是否手持暗眼魂
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() != ModItems.DARK_RIDER_EYECON.get() && 
                    player.getOffhandItem().getItem() != ModItems.DARK_RIDER_EYECON.get()) {
                    player.displayClientMessage(Component.literal("你需要手持暗眼魂才能变身黑暗Ghost！"), true);
                    return;
                }

                // 播放变身音效
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModBossSounds.DARK_GHOST.get(), // 使用DARK_GHOST音效
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);

                // 标记状态
                belt.setHenshin(beltStack, true);
                belt.setMode(beltStack, GhostDriver.BeltMode.DARK_RIDER_EYE);
                belt.setShowing(beltStack, false);
                belt.setActive(beltStack, true);
                
                // 标记玩家已变身
                variables.isDarkGhostTransformed = true;
                variables.syncPlayerVariables(player);
                
                // 延迟12秒后装备盔甲
                final ServerPlayer finalPlayer = player;
                final net.minecraft.server.MinecraftServer server = player.getServer();
                if (server != null) {
                    // 创建一个新线程来处理延迟
                    new Thread(() -> {
                        try {
                            // 休眠12秒
                            Thread.sleep(12000);
                            
                            // 确保在服务器线程上执行盔甲装备
                            server.execute(() -> {
                                // 检查玩家是否仍然在线且状态未变
                                if (finalPlayer.isAlive() && finalPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                        .map(v -> v.isDarkGhostTransformed).orElse(false)) {
                                    // 延迟12秒后装备盔甲
                                    DarkGhostRiderHenshin.trigger(finalPlayer);
                                    
                                    // 给予黑暗Ghost的武器
                                    TransformationWeaponManager.giveWeaponOnDarkGhostTransformation(finalPlayer);
                                }
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }

                // 发送动画包
                // 使用setModeAndTriggerHenshin方法来触发变身动画
                belt.setMode(beltStack, GhostDriver.BeltMode.DARK_RIDER_EYE);

                // 更新腰带状态
                CurioUtils.updateCurioSlot(
                        player,
                        ghostDriver.slotContext().identifier(),
                        ghostDriver.slotContext().index(),
                        beltStack);

                // 发送变身成功提示
                player.displayClientMessage(Component.literal("变身！黑暗Ghost！"), true);

                // 消耗眼魂（可选，根据需要决定是否消耗）
                if (heldItem.getItem() == ModItems.DARK_RIDER_EYECON.get()) {
                    heldItem.shrink(1);
                } else if (player.getOffhandItem().getItem() == ModItems.DARK_RIDER_EYECON.get()) {
                    player.getOffhandItem().shrink(1);
                }

            } else {
                // 解除变身逻辑
                // 检查是否已经变身
                if (!variables.isDarkGhostTransformed) {
                    player.displayClientMessage(Component.literal("你还没有变身！"), true);
                    return;
                }

                // 停止待机音
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "ghost_idle"); // 假设有这个音效，实际需要根据mod中的音效名称调整
                PacketHandler.sendToAllTracking(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player);

                // 播放解除变身音效
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModBossSounds.LOCKOFF.get(), // 临时解除变身音效
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);

                // 清空盔甲
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, ItemStack.EMPTY);
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, ItemStack.EMPTY);
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, ItemStack.EMPTY);

                // 重置腰带状态
                belt.setHenshin(beltStack, false);
                belt.setMode(beltStack, GhostDriver.BeltMode.DEFAULT);
                belt.setShowing(beltStack, true);
                belt.setActive(beltStack, false);

                // 开始解除变身动画
                belt.startReleaseAnimation(player, beltStack);

                // 更新腰带状态
                CurioUtils.updateCurioSlot(
                        player,
                        ghostDriver.slotContext().identifier(),
                        ghostDriver.slotContext().index(),
                        beltStack);

                // 重置玩家状态
                variables.isDarkGhostTransformed = false;
                variables.syncPlayerVariables(player);
                
                // 清理武器
                TransformationWeaponManager.clearWeaponsOnGhostDriverDemorph(player);

                // 返回黑暗骑士眼魂
                ItemStack darkEyecon = new ItemStack(ModItems.DARK_RIDER_EYECON.get());
                if (!player.addItem(darkEyecon)) {
                    // 如果玩家物品栏已满，将眼魂掉落地上
                    player.drop(darkEyecon, false);
                }

                // 发送解除变身提示
                player.displayClientMessage(Component.literal("解除变身！"), true);
            }
        });
        context.setPacketHandled(true);
    }
}