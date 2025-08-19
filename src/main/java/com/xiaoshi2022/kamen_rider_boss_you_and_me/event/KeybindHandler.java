package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.ReleaseBeltPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.MelonTransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.CherryTransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.TransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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

            // 检查樱桃锁种超时
            checkCherryLockSeedTimeout(player);
            checkLemonLockSeedTimeout(player);
            checkMelonLockSeedTimeout(player);

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
        if (!(beltStack.getItem() instanceof Genesis_driver gd)) return "BARONS";
        return switch (gd.getMode(beltStack)) {
            case LEMON -> "GENESIS";
            case MELON -> "GENESIS_MELON";
            case CHERRY -> "GENESIS_CHERRY";
            default -> "GENESIS";   // 或者 DEFAULT 时直接 return ""
        };
    }

    private static void checkCherryLockSeedTimeout(Player player) {
        // 如果玩家有樱桃就绪标记且不在冷却中
        if (player.getPersistentData().getBoolean("cherry_ready") && !keyCooldown) {
            long readyTime = player.getPersistentData().getLong("cherry_ready_time");
            long currentTime = player.level().getGameTime();

            // 如果超过30秒未变身，清除标记
            if (currentTime - readyTime > 600) { // 30秒 = 600 tick
                player.getPersistentData().remove("cherry_ready");
                player.getPersistentData().remove("cherry_ready_time");
                player.displayClientMessage(Component.literal("樱桃锁种已过期，请重新装备"), true);
            }
        }
    }

    // 检查柠檬锁种超时
    private static void checkLemonLockSeedTimeout(Player player) {
        if (player.getPersistentData().getBoolean("lemon_ready")) {
            long readyTime = player.getPersistentData().getLong("lemon_ready_time");
            if (player.level().getGameTime() - readyTime > 600) { // 30秒 = 600tick
                player.getPersistentData().remove("lemon_ready");
                player.getPersistentData().remove("lemon_ready_time");
                player.displayClientMessage(Component.literal("柠檬锁种已过期！"), true);
            }
        }
    }

    // 检查蜜瓜锁种超时
    private static void checkMelonLockSeedTimeout(Player player) {
        if (player.getPersistentData().getBoolean("melon_ready")) {
            long readyTime = player.getPersistentData().getLong("melon_ready_time");
            if (player.level().getGameTime() - readyTime > 600) { // 30秒 = 600tick
                player.getPersistentData().remove("melon_ready");
                player.getPersistentData().remove("melon_ready_time");
                player.displayClientMessage(Component.literal("蜜瓜锁种已过期！"), true);
            }
        }
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

        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver)
                .ifPresent(curio -> {
                    ItemStack beltStack = curio.stack();
                    Genesis_driver belt = (Genesis_driver) beltStack.getItem();
                    Genesis_driver.BeltMode mode = belt.getMode(beltStack);

                    switch (mode) {
                        case LEMON -> {
                            PacketHandler.sendToServer(
                                    new TransformationRequestPacket(player.getUUID(), "GENESIS", true));
                            belt.startReleaseAnimation(player);
                            delayTicks = 40;
                            delayedBeltStack = beltStack.copy();
                        }
                        case MELON -> {
                            // 先判断是变身还是解除
                            boolean isTransformed = 
                                    player.getInventory().armor.get(3).getItem() == ModItems.ZANGETSU_SHIN_HELMET.get();
                            if (isTransformed) {
                                // 解除
                                PacketHandler.sendToServer(
                                        new TransformationRequestPacket(player.getUUID(), "GENESIS_MELON", true));
                                belt.startReleaseAnimation(player);
                                delayTicks = 40;
                                delayedBeltStack = beltStack.copy();
                            } else {
                                // 变身
                                PacketHandler.sendToServer(
                                        new MelonTransformationRequestPacket(player.getUUID()));
                            }
                        }
                        case CHERRY -> {
                            // 先判断是变身还是解除
                            boolean isTransformed = 
                                    player.getInventory().armor.get(3).getItem() == ModItems.SIGURD_HELMET.get();
                            if (isTransformed) {
                                // 解除
                                PacketHandler.sendToServer(
                                        new TransformationRequestPacket(player.getUUID(), "GENESIS_CHERRY", true));
                                belt.startReleaseAnimation(player);
                                delayTicks = 40;
                                delayedBeltStack = beltStack.copy();
                            } else {
                                // 变身
                                PacketHandler.sendToServer(
                                        new CherryTransformationRequestPacket(player.getUUID()));
                            }
                        }
                        default -> {}
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
        switch (beltType) {
            case "GENESIS"         -> handleGenesisBeltRelease(player);   // 柠檬
            case "GENESIS_MELON"   -> handleMelonRelease(player);         // 蜜瓜
            case "GENESIS_CHERRY"  -> handleCherryRelease(player);        // 樱桃
            case "BARONS"          -> handleBaronsBeltRelease(player);    // 香蕉
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

    private static void handleMelonRelease(ServerPlayer player) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

        if (curio.isEmpty()) return;

        ItemStack beltStack = curio.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.MELON) return;

        // 1. 重置腰带模式
        belt.setMode(beltStack, Genesis_driver.BeltMode.DEFAULT);
        SlotResult slotResult = curio.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                beltStack
        );

        // 2. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // 3. 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_lockonby");
        PacketHandler.sendToAllTracking(new SoundStopPacket(player.getId(), soundLoc), player);
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));


        // 4. 卸掉蜜瓜装甲（3 件）
        clearTransformationArmor(player);

        // 5. 返还蜜瓜锁种
        ItemStack melonLockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.MELON.get());
        if (!player.getInventory().add(melonLockSeed)) player.spawnAtLocation(melonLockSeed);

        // 6. 重置腰带状态
        belt.isEquipped = false;
        belt.isHenshining = false;
        player.inventoryMenu.broadcastChanges();
    }

    private static void handleCherryRelease(ServerPlayer player) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

        if (curio.isEmpty()) return;

        ItemStack beltStack = curio.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.CHERRY) return;

        // 1. 重置腰带模式
        belt.setMode(beltStack, Genesis_driver.BeltMode.DEFAULT);
        SlotResult slotResult = curio.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                beltStack
        );

        // 2. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // 3. 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_lockonby");
        PacketHandler.sendToAllTracking(new SoundStopPacket(player.getId(), soundLoc), player);
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));

        // 4. 卸掉樱桃装甲
        clearTransformationArmor(player);

        // 5. 返还樱桃锁种
        ItemStack cherryLockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY.get());
        if (!player.getInventory().add(cherryLockSeed)) player.spawnAtLocation(cherryLockSeed);

        // 6. 重置腰带状态
        belt.isEquipped = false;
        belt.isHenshining = false;
        player.inventoryMenu.broadcastChanges();
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