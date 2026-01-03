package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BrainDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.BrainRiderHenshin;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
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

public class BrainTransformationRequestPacket {
    private final boolean isRelease;

    // 为了兼容客户端调用，添加一个不需要参数的构造函数
    public BrainTransformationRequestPacket() {
        this(false); // 默认不是解除变身
    }

    public BrainTransformationRequestPacket(boolean isRelease) {
        this.isRelease = isRelease;
    }

    public static void encode(BrainTransformationRequestPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isRelease);
    }

    public static BrainTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        return new BrainTransformationRequestPacket(buffer.readBoolean());
    }

    public static void handle(BrainTransformationRequestPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // 检查玩家是否装备了BrainDriver
            SlotResult brainDriver = CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof BrainDriver))
                    .orElse(null);

            if (brainDriver == null) {
                player.displayClientMessage(Component.literal("你需要装备BrainDriver才能变身！"), true);
                return;
            }

            ItemStack beltStack = brainDriver.stack();
            BrainDriver belt = (BrainDriver) beltStack.getItem();

            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());

            if (!message.isRelease) {
                // 变身逻辑
                
                // 播放变身音效
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModBossSounds.BRAINRIDER.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);

                // 标记状态
                belt.setHenshin(beltStack, true);
                belt.setMode(beltStack, BrainDriver.BeltMode.BRAIN);
                belt.setShowing(beltStack, false);
                belt.setActive(beltStack, true);
                
                // 标记玩家已变身
                variables.isBrainTransformed = true;
                variables.syncPlayerVariables(player);
                
                // 延迟后装备盔甲
                final ServerPlayer finalPlayer = player;
                final net.minecraft.server.MinecraftServer server = player.getServer();
                if (server != null) {
                    // 创建一个新线程来处理延迟
                    new Thread(() -> {
                        try {
                            // 休眠12秒，与其他变身保持一致
                            Thread.sleep(12000);
                            
                            // 确保在服务器线程上执行盔甲装备
                            server.execute(() -> {
                                // 检查玩家是否仍然在线且状态未变
                                if (finalPlayer.isAlive() && finalPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                        .map(v -> v.isBrainTransformed).orElse(false)) {
                                    // 延迟后装备盔甲
                                    BrainRiderHenshin.trigger(finalPlayer);
                                }
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }

                // 发送动画包
                belt.setMode(beltStack, BrainDriver.BeltMode.BRAIN);

                // 更新腰带状态
                CurioUtils.updateCurioSlot(
                        player,
                        brainDriver.slotContext().identifier(),
                        brainDriver.slotContext().index(),
                        beltStack);

                // 发送变身成功提示
                player.displayClientMessage(Component.literal("变身！假面骑士Brain！"), true);

            } else {
                // 解除变身逻辑
                // 检查是否已经变身
                if (!variables.isBrainTransformed) {
                    player.displayClientMessage(Component.literal("你还没有变身！"), true);
                    return;
                }

                // 播放解除变身音效
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModBossSounds.LOCKOFF.get(), // 使用通用的解除变身音效
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
                belt.setMode(beltStack, BrainDriver.BeltMode.DEFAULT);
                belt.setShowing(beltStack, true);
                belt.setActive(beltStack, false);

                // 开始解除变身动画
                belt.startReleaseAnimation(player, beltStack);

                // 更新腰带状态
                CurioUtils.updateCurioSlot(
                        player,
                        brainDriver.slotContext().identifier(),
                        brainDriver.slotContext().index(),
                        beltStack);

                // 重置玩家状态
                variables.isBrainTransformed = false;
                variables.syncPlayerVariables(player);

                // 发送解除变身提示
                player.displayClientMessage(Component.literal("解除变身！"), true);
            }
        });
        context.setPacketHandled(true);
    }
}