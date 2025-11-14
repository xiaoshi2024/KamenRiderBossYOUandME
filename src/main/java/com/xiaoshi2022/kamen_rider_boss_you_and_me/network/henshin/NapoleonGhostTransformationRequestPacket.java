package com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.GhostDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.function.Supplier;

public class NapoleonGhostTransformationRequestPacket {
    private final boolean isRelease;

    // 为了兼容客户端调用，添加一个不需要参数的构造函数
    public NapoleonGhostTransformationRequestPacket() {
        this(false); // 默认不是解除变身
    }

    public NapoleonGhostTransformationRequestPacket(boolean isRelease) {
        this.isRelease = isRelease;
    }

    public static void encode(NapoleonGhostTransformationRequestPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isRelease);
    }

    public static NapoleonGhostTransformationRequestPacket decode(FriendlyByteBuf buffer) {
        return new NapoleonGhostTransformationRequestPacket(buffer.readBoolean());
    }

    public static void handle(NapoleonGhostTransformationRequestPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
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
                // 检查是否手持拿破仑眼魂
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() == ModItems.NAPOLEON_EYECON.get() || 
                    player.getOffhandItem().getItem() == ModItems.NAPOLEON_EYECON.get()) {
                    // 如果腰带当前有眼魂存在，取下当前眼魂
                    GhostDriver.BeltMode currentMode = belt.getMode(beltStack);
                    if (currentMode != GhostDriver.BeltMode.DEFAULT) {
                        // 取下当前眼魂 - 只处理拿破仑眼魂模式
                        ItemStack removedEyecon = null;
                        if (currentMode == GhostDriver.BeltMode.NAPOLEON_GHOST) {
                            // 取下拿破仑眼魂
                            removedEyecon = new ItemStack(ModItems.NAPOLEON_EYECON.get());
                        }
                        
                        // 如果有眼魂被取下，添加到玩家物品栏
                        if (removedEyecon != null && !player.addItem(removedEyecon)) {
                            // 如果玩家物品栏已满，将眼魂掉落地上
                            player.drop(removedEyecon, false);
                        }
                    }
                    
                    // 播放变身音效
                    player.level().playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            ModBossSounds.NAPOLEON_GHOST.get(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);

                    // 标记状态
                    belt.setHenshin(beltStack, true);
                    belt.setMode(beltStack, GhostDriver.BeltMode.NAPOLEON_GHOST);
                    belt.setShowing(beltStack, false);
                    belt.setActive(beltStack, true);
                    
                    // 标记玩家已变身
                    variables.isNapoleonGhostTransformed = true;
                    variables.isGhostEye = false; // 清除眼魔状态，确保状态互斥
                    variables.syncPlayerVariables(player);
                    
                    // 延迟10秒后装备盔甲
                    final ServerPlayer finalPlayer = player;
                    final net.minecraft.server.MinecraftServer server = player.getServer();
                    if (server != null) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(10000); // 10秒延迟
                                
                                server.execute(() -> {
                                    // 检查玩家是否仍然在线且状态未变
                                    if (finalPlayer.isAlive() && finalPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                                            .map(v -> v.isNapoleonGhostTransformed).orElse(false)) {
                                        // 保存玩家当前的盔甲
                                        KRBVariables.PlayerVariables playerVars = finalPlayer.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                                        if (playerVars.originalNapoleonGhostArmor == null) {
                                            playerVars.originalNapoleonGhostArmor = new net.minecraft.nbt.ListTag();
                                            EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                                            for (EquipmentSlot slot : slots) {
                                                ItemStack stack = finalPlayer.getItemBySlot(slot);
                                                if (!stack.isEmpty()) {
                                                    net.minecraft.nbt.CompoundTag itemTag = new net.minecraft.nbt.CompoundTag();
                                                    stack.save(itemTag);
                                                    playerVars.originalNapoleonGhostArmor.add(itemTag);
                                                } else {
                                                    playerVars.originalNapoleonGhostArmor.add(new net.minecraft.nbt.CompoundTag());
                                                }
                                            }
                                            playerVars.syncPlayerVariables(finalPlayer);
                                        }
                                        
                                        // 装备拿破仑魂盔甲
                                        finalPlayer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.NAPOLEON_GHOST_HELMET.get()));
                                        finalPlayer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.NAPOLEON_GHOST_CHESTPLATE.get()));
                                        finalPlayer.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.NAPOLEON_GHOST_LEGGINGS.get()));
                                        finalPlayer.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY); // 假设没有靴子
                                    }
                                });
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }

                    // 更新腰带状态
                    CurioUtils.updateCurioSlot(
                            player,
                            ghostDriver.slotContext().identifier(),
                            ghostDriver.slotContext().index(),
                            beltStack);
                    
                    // 给予拿破仑形态的武器（眼剑枪）
                    TransformationWeaponManager.giveWeaponOnNapoleonGhostTransformation(player);

                    // 发送变身成功提示
                    player.displayClientMessage(Component.literal("变身！拿破仑魂！"), true);

                    // 消耗眼魂
                    if (heldItem.getItem() == ModItems.NAPOLEON_EYECON.get()) {
                        heldItem.shrink(1);
                    } else if (player.getOffhandItem().getItem() == ModItems.NAPOLEON_EYECON.get()) {
                        player.getOffhandItem().shrink(1);
                    }
                } else {
                    player.displayClientMessage(Component.literal("你需要手持拿破仑眼魂才能变身拿破仑魂！"), true);
                }

            } else {
                // 解除变身逻辑
                // 检查是否已经变身
                if (!variables.isNapoleonGhostTransformed) {
                    player.displayClientMessage(Component.literal("你还没有变身！"), true);
                    return;
                }

                // 停止待机音
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "ghost_idle");
                PacketHandler.sendToAllTracking(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player);

                // 播放解除变身音效
                player.level().playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModBossSounds.LOCKOFF.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);

                // 恢复玩家原来的盔甲
                if (variables.originalNapoleonGhostArmor != null) {
                    EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                    for (int i = 0; i < variables.originalNapoleonGhostArmor.size() && i < slots.length; i++) {
                        net.minecraft.nbt.CompoundTag itemTag = variables.originalNapoleonGhostArmor.getCompound(i);
                        ItemStack stack = ItemStack.of(itemTag);
                        player.setItemSlot(slots[i], stack);
                    }
                    variables.originalNapoleonGhostArmor = null;
                } else {
                    // 如果没有保存的盔甲数据，清空所有盔甲槽位
                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                    player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                    player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                }

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
                variables.isNapoleonGhostTransformed = false;
                variables.syncPlayerVariables(player);
                
                // 返回拿破仑眼魂
                ItemStack napoleonEyecon = new ItemStack(ModItems.NAPOLEON_EYECON.get());
                if (!player.addItem(napoleonEyecon)) {
                    // 如果玩家物品栏已满，将眼魂掉落地上
                    player.drop(napoleonEyecon, false);
                }

                // 发送解除变身提示
                player.displayClientMessage(Component.literal("解除变身！"), true);
            }
        });
        context.setPacketHandled(true);
    }
}