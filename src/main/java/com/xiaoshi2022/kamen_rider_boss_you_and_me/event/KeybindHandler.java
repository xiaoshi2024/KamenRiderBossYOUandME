package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ReleaseBeltPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.TransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me", value = Dist.CLIENT)
public class KeybindHandler {
    private static boolean keyCooldown = false;
    private static int delayTicks = 0;
    private static ItemStack delayedBeltStack = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            if (KeyBinding.RELIEVE_KEY.isDown() && !keyCooldown) {
                keyCooldown = true;
                handleKeyPress(player);
            } else if (!KeyBinding.RELIEVE_KEY.isDown()) {
                keyCooldown = false;
            }

            if (delayTicks > 0) {
                delayTicks--;
                if (delayTicks == 0 && !delayedBeltStack.isEmpty()) {
                    PacketHandler.sendToServer(new ReleaseBeltPacket(true, getBeltType(delayedBeltStack)));
                    delayedBeltStack = ItemStack.EMPTY;
                }
            }
        }
    }

    private static String getBeltType(ItemStack beltStack) {
        if (beltStack.getItem() instanceof Genesis_driver) {
            return "GENESIS";
        }
        return "BARONS";
    }

    private static void handleKeyPress(Player player) {
        // 柠檬逻辑
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver)
                .ifPresent(curio -> {
                    ItemStack beltStack = curio.stack();
                    Genesis_driver belt = (Genesis_driver) beltStack.getItem();

                    if (belt.getMode(beltStack) == Genesis_driver.BeltMode.LEMON) {
                        // 发送解除变身请求
                        PacketHandler.sendToServer(new TransformationRequestPacket(player.getUUID(), "GENESIS", true)); // true 表示是解除变身请求
                        belt.startReleaseAnimation(player);
                        delayTicks = 40;
                        delayedBeltStack = beltStack.copy();
                    }
                });

        // 香蕉逻辑
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof sengokudrivers_epmty)
                .ifPresent(curio -> {
                    ItemStack beltStack = curio.stack();
                    sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

                    if (belt.getMode(beltStack) == sengokudrivers_epmty.BeltMode.BANANA) {
                        // 发送解除变身请求
                        PacketHandler.sendToServer(new TransformationRequestPacket(player.getUUID(), "BARONS", true)); // true 表示是解除变身请求
                        belt.startReleaseAnimation(player);
                        delayTicks = 40;
                        delayedBeltStack = beltStack.copy();
                    }
                });
    }

    public static void completeBeltRelease(ServerPlayer player, String beltType) {
        if ("GENESIS".equals(beltType)) {
            handleGenesisBeltRelease(player);
        } else if ("BARONS".equals(beltType)) {
            handleBaronsBeltRelease(player);
        }
    }

    private static void handleGenesisBeltRelease(ServerPlayer player) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();

            if (belt.getMode(beltStack) == Genesis_driver.BeltMode.LEMON) {
                System.out.println("玩家解除柠檬变身");
                ItemStack newStack = beltStack.copy();
                belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);

                CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(), newStack);

                // 播放解除音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);

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

                // 清除变身装甲
                clearTransformationArmor(player);

                // 返还柠檬锁种
                ItemStack lemonLockSeed = new ItemStack(ModItems.LEMON_ENERGY.get());
                if (!player.getInventory().add(lemonLockSeed)) {
                    player.spawnAtLocation(lemonLockSeed);
                } else {
                    System.out.println("柠檬锁种已添加到玩家背包");
                }

                // 重置腰带状态
                belt.isEquipped = false;
                belt.isHenshining = false;

                // 同步状态
                player.inventoryMenu.broadcastChanges();
            }
        }
    }

    private static void handleBaronsBeltRelease(ServerPlayer player) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof sengokudrivers_epmty));

        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();

            if (belt.getMode(beltStack) == sengokudrivers_epmty.BeltMode.BANANA) {
                ItemStack newStack = beltStack.copy();
                belt.setMode(newStack, sengokudrivers_epmty.BeltMode.DEFAULT); // 重置为默认模式

                CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(), newStack);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);

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

                clearTransformationArmor(player);

                ItemStack bananaLockSeed = new ItemStack(ModItems.BANANAFRUIT.get());
                if (!player.getInventory().add(bananaLockSeed)) {
                    player.spawnAtLocation(bananaLockSeed);
                }

                belt.isEquipped = false;
                belt.isHenshining = false;

                player.inventoryMenu.broadcastChanges();
            }
        }
    }

    private static void clearTransformationArmor(ServerPlayer player) {
        for (int i = 1; i < 4; i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
    }
}