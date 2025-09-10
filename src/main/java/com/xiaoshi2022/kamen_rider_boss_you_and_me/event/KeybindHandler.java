package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
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

            // 检查各锁种超时
            checkCherryLockSeedTimeout(player);
            checkLemonLockSeedTimeout(player);
            checkMelonLockSeedTimeout(player);
            checkPeachLockSeedTimeout(player);
            checkDragonfruitLockSeedTimeout(player);

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
        if (beltStack.getItem() instanceof DrakKivaBelt) {
            return "DARK_KIVA";
        }
        if (beltStack.getItem() instanceof Genesis_driver gd) {
            return switch (gd.getMode(beltStack)) {
                case LEMON -> "GENESIS";
                case MELON -> "GENESIS_MELON";
                case CHERRY -> "GENESIS_CHERRY";
                case PEACH -> "GENESIS_PEACH";
                case DRAGONFRUIT -> "GENESIS_DRAGONFRUIT";
                default -> "GENESIS";
            };
        }
        return "BARONS";
    }

    private static void checkCherryLockSeedTimeout(Player player) {
        // 获取PlayerVariables实例
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());

        // 如果玩家有樱桃就绪标记且不在冷却中
        if (variables.cherry_ready && !keyCooldown) {
            long currentTime = player.level().getGameTime();

            // 如果超过30秒未变身，清除标记
            if (currentTime - variables.cherry_ready_time > 600) { // 30秒 = 600 tick
                variables.cherry_ready = false;
                variables.cherry_ready_time = 0L;
                player.displayClientMessage(Component.literal("樱桃锁种已过期，请重新装备"), true);
            }
        }
    }

    // 检查柠檬锁种超时
    private static void checkLemonLockSeedTimeout(Player player) {
        // 获取PlayerVariables实例
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());

        if (variables.lemon_ready) {
            if (player.level().getGameTime() - variables.lemon_ready_time > 600) { // 30秒 = 600tick
                variables.lemon_ready = false;
                variables.lemon_ready_time = 0L;
                player.displayClientMessage(Component.literal("柠檬锁种已过期！"), true);
            }
        }
    }

    // 检查火龙果锁种超时
    private static void checkDragonfruitLockSeedTimeout(Player player) {
        // 获取PlayerVariables实例
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());

        if (variables.dragonfruit_ready) {
            if (player.level().getGameTime() - variables.dragonfruit_ready_time > 600) { // 30秒 = 600tick
                variables.dragonfruit_ready = false;
                variables.dragonfruit_ready_time = 0L;
                player.displayClientMessage(Component.literal("火龙果锁种已过期！"), true);
            }
        }
    }

    // 检查桃子锁种超时
    private static void checkPeachLockSeedTimeout(Player player) {
        // 获取PlayerVariables实例
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());

        if (variables.peach_ready) {
            if (player.level().getGameTime() - variables.peach_ready_time > 600) { // 30秒 = 600tick
                variables.peach_ready = false;
                variables.peach_ready_time = 0L;
                player.displayClientMessage(Component.literal("桃子锁种已过期！"), true);
            }
        }
    }

    // 检查蜜瓜锁种超时
    private static void checkMelonLockSeedTimeout(Player player) {
        // 获取PlayerVariables实例
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());

        if (variables.melon_ready) {
            if (player.level().getGameTime() - variables.melon_ready_time > 600) { // 30秒 = 600tick
                variables.melon_ready = false;
                variables.melon_ready_time = 0L;
                player.displayClientMessage(Component.literal("蜜瓜锁种已过期！"), true);
            }
        }
    }

    private static void handleKeyPress(Player player) {
        // 检测双面武器是否为bat形态，实现C键解除EvilBats变身或取下武器
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        // 获取玩家变身状态
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        boolean isTransformed = variables.isEvilBatsTransformed;

        // 检查是否佩戴Two_sidriver腰带
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));

        if (opt.isPresent()) {
            ItemStack belt = opt.get().stack();
            Two_sidriver.DriverType type = Two_sidriver.getDriverType(belt);
            Two_sidriver beltItem = (Two_sidriver) belt.getItem();

            if (type == Two_sidriver.DriverType.BAT) {
                // 腰带为bat形态，发送请求取下武器或解除变身
                if (isTransformed) {
                    // 如果玩家已变身，返回蝙蝠印章并解除变身
                    PacketHandler.sendToServer(new ReleaseBeltPacket(true, "EVIL_BATS"));

//                    // 将武器变回普通形态
//                    if (mainHandItem.getItem() instanceof TwoWeaponItem &&
//                            ((TwoWeaponItem)mainHandItem.getItem()).getVariant(mainHandItem) == TwoWeaponItem.Variant.BAT) {
//                        TwoWeaponItem.setVariant(mainHandItem, TwoWeaponItem.Variant.DEFAULT);
//                    }
//                    if (offHandItem.getItem() instanceof TwoWeaponItem &&
//                            ((TwoWeaponItem)offHandItem.getItem()).getVariant(offHandItem) == TwoWeaponItem.Variant.BAT) {
//                        TwoWeaponItem.setVariant(offHandItem, TwoWeaponItem.Variant.DEFAULT);
//                    }

                    // 发送武器取下数据包
                    PacketHandler.sendToServer(new WeaponRemovePacket(true));
                    return;
                } else {
                    // 如果玩家未变身，发送武器取下数据包
                    PacketHandler.sendToServer(new WeaponRemovePacket(true));
                    return;
                }
            }
        }

// 检测主手或副手武器是否为bat形态
        if ((mainHandItem.getItem() instanceof TwoWeaponItem &&
                ((TwoWeaponItem)mainHandItem.getItem()).getVariant(mainHandItem) == TwoWeaponItem.Variant.BAT) ||
                (offHandItem.getItem() instanceof TwoWeaponItem &&
                        ((TwoWeaponItem)offHandItem.getItem()).getVariant(offHandItem) == TwoWeaponItem.Variant.BAT)) {
            // 发送解除EvilBats变身请求到服务端
            PacketHandler.sendToServer(new ReleaseBeltPacket(true, "EVIL_BATS"));
            return; // 处理完EvilBats变身后直接返回
        }
        
        // 柠檬逻辑
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver)
                .ifPresent(curio -> {
                    ItemStack beltStack = curio.stack();
                    Genesis_driver belt = (Genesis_driver) beltStack.getItem();

                    if (belt.getMode(beltStack) == Genesis_driver.BeltMode.LEMON) {
                        // 发送解除变身请求
                        PacketHandler.sendToServer(new TransformationRequestPacket(player.getUUID(), "GENESIS", true)); // true 表示是解除变身请求
                        belt.startReleaseAnimation(player, beltStack);
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
                            belt.startReleaseAnimation(player, beltStack);
                            delayTicks = 40;
                            delayedBeltStack = beltStack.copy();
                        }
                        case MELON -> {
                            // 先判断是变身还是解除
                            boolean isTransformeds =
                                    player.getInventory().armor.get(3).getItem() == ModItems.ZANGETSU_SHIN_HELMET.get();
                            if (isTransformeds) {
                                // 解除
                                PacketHandler.sendToServer(
                                        new TransformationRequestPacket(player.getUUID(), "GENESIS_MELON", true));
                                belt.startReleaseAnimation(player, beltStack);
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
                            boolean isTransformeds =
                                    player.getInventory().armor.get(3).getItem() == ModItems.SIGURD_HELMET.get();
                            if (isTransformeds) {
                                // 解除
                                PacketHandler.sendToServer(
                                        new TransformationRequestPacket(player.getUUID(), "GENESIS_CHERRY", true));
                                belt.startReleaseAnimation(player, beltStack);
                                delayTicks = 40;
                                delayedBeltStack = beltStack.copy();
                            } else {
                                // 变身
                                PacketHandler.sendToServer(
                                        new CherryTransformationRequestPacket(player.getUUID()));
                            }
                        }
                        case PEACH -> {
                            // 先判断是变身还是解除
                            boolean isTransformeds =
                                    player.getInventory().armor.get(3).getItem() == ModItems.MARIKA_HELMET.get();
                            if (isTransformeds) {
                                // 解除
                                PacketHandler.sendToServer(
                                        new TransformationRequestPacket(player.getUUID(), "GENESIS_PEACH", true));
                                belt.startReleaseAnimation(player, beltStack);
                                delayTicks = 40;
                                delayedBeltStack = beltStack.copy();
                            } else {
                                // 变身
                                PacketHandler.sendToServer(
                                        new MarikaTransformationRequestPacket(player.getUUID()));
                            }
                        }
                        case DRAGONFRUIT -> {
                            // 先判断是变身还是解除
                            boolean isTransformeds =
                                    player.getInventory().armor.get(3).getItem() == ModItems.TYRANT_HELMET.get();
                            if (isTransformeds) {
                                // 解除
                                PacketHandler.sendToServer(
                                        new TransformationRequestPacket(player.getUUID(), "GENESIS_DRAGONFRUIT", true));
                                belt.startReleaseAnimation(player, beltStack);
                                delayTicks = 40;
                                delayedBeltStack = beltStack.copy();
                            } else {
                                // 变身
                                PacketHandler.sendToServer(
                                        new DragonfruitTransformationRequestPacket(player.getUUID()));
                            }
                        }
                        default -> {}
                    }
                });

        // 在 handleKeyPress 方法中，找到 Dark Kiva 部分：
        CurioUtils.findFirstCurio(player,
                        stack -> stack.getItem() instanceof DrakKivaBelt)
                .ifPresent(curio -> {
                    ItemStack beltStack = curio.stack();
                    boolean isDarkKiva = player.getInventory().armor.get(3).getItem() == ModItems.DARK_KIVA_HELMET.get();

                    if (isDarkKiva) {
                        // 发送解除变身请求
                        PacketHandler.sendToServer(new TransformationRequestPacket(player.getUUID(), "DARK_KIVA", true));
                        delayTicks = 60;
                        delayedBeltStack = beltStack.copy();
                    }
                });

        // 香蕉和DarkOrange逻辑
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof sengokudrivers_epmty)
                .ifPresent(curio -> {
                    ItemStack beltStack = curio.stack();
                    sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();
                    sengokudrivers_epmty.BeltMode mode = belt.getMode(beltStack);

                    if (mode == sengokudrivers_epmty.BeltMode.BANANA) {
                        // 发送香蕉解除变身请求
                        PacketHandler.sendToServer(new TransformationRequestPacket(player.getUUID(), "BARONS", true)); // true 表示是解除变身请求
                        belt.startReleaseAnimation(player,beltStack);
                        delayTicks = 40;
                        delayedBeltStack = beltStack.copy();
                    } else if (mode == sengokudrivers_epmty.BeltMode.ORANGELS) {
                        // 发送DarkOrange解除变身请求
                        PacketHandler.sendToServer(new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkOrangeReleaseRequestPacket(player.getUUID()));
                        belt.startReleaseAnimation(player,beltStack);
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
            case "GENESIS_PEACH"   -> handlePeachRelease(player);         // 桃子
            case "GENESIS_DRAGONFRUIT" -> handleDragonfruitRelease(player); // 火龙果
            case "BARONS"          -> handleBaronsBeltRelease(player);    // 香蕉
            case "DUKE"            -> handleDukeBeltRelease(player);      // 公爵
            case "EVIL_BATS"       -> handleEvilBatsRelease(player);      // Evil Bats
        }
    }
    
    // 新增：Evil Bats解除变身逻辑
    private static void handleEvilBatsRelease(ServerPlayer player) {
        // 检查玩家是否已变身
        KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        boolean isTransformed = variables.isEvilBatsTransformed;

        // 清除EvilBats盔甲
        clearTransformationArmor(player);

        // 更新玩家变量，将EvilBats变身状态设置为false
        variables.isEvilBatsTransformed = false;
        variables.syncPlayerVariables(player);

        // 检查并更新玩家手持的双面武器，从BAT形态变回普通形态（包括主手和副手）
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        // 处理主手武器
        if (mainHandItem.getItem() instanceof TwoWeaponItem) {
            TwoWeaponItem weapon = (TwoWeaponItem) mainHandItem.getItem();

            if (weapon.getVariant(mainHandItem) == TwoWeaponItem.Variant.BAT) {
                // 将武器形态从BAT改回DEFAULT
                weapon.setVariant(mainHandItem, TwoWeaponItem.Variant.DEFAULT);
            }
        }

        // 处理副手武器
        if (offHandItem.getItem() instanceof TwoWeaponItem) {
            TwoWeaponItem weapon = (TwoWeaponItem) offHandItem.getItem();

            if (weapon.getVariant(offHandItem) == TwoWeaponItem.Variant.BAT) {
                // 将武器形态从BAT改回DEFAULT
                weapon.setVariant(offHandItem, TwoWeaponItem.Variant.DEFAULT);
            }
        }

        // 检查并更新腰带状态
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));

        if (opt.isPresent()) {
            ItemStack belt = opt.get().stack();
            Two_sidriver beltItem = (Two_sidriver) belt.getItem();

            // 检查腰带是否为BAT形态
            if (beltItem.getDriverType(belt) == Two_sidriver.DriverType.BAT) {
                // 将腰带从BAT形态改回DEFAULT
                beltItem.setDriverType(belt, Two_sidriver.DriverType.DEFAULT);

                // 更新腰带槽位
                SlotResult slotResult = opt.get();
                boolean updated = CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(), belt);

                if (!updated) {
                    // 如果更新失败，尝试直接设置
                    player.getInventory().setItem(slotResult.slotContext().index(), belt);
                }
            }
        }

        // 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        player.inventoryMenu.broadcastChanges();

        // 返还蝙蝠印章
        ItemStack batStamp = new ItemStack(ModItems.BAT_STAMP.get());
        if (!player.getInventory().add(batStamp)) {
            player.spawnAtLocation(batStamp);

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
                // 只发送给当前玩家
                PacketHandler.sendToClient(
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
                belt.setEquipped(beltStack, false);
                belt.setHenshin(beltStack, false);

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
        // 只发送给当前玩家
        PacketHandler.sendToClient(new SoundStopPacket(player.getId(), soundLoc), player);
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));


        // 4. 卸掉蜜瓜装甲（3 件）
        clearTransformationArmor(player);

        // 5. 返还蜜瓜锁种
        ItemStack melonLockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.MELON.get());
        if (!player.getInventory().add(melonLockSeed)) player.spawnAtLocation(melonLockSeed);

        // 6. 重置腰带状态
        belt.setEquipped(beltStack, false);
        belt.setHenshin(beltStack, false);

        // 7. 重置火龙果变身时间
        KRBVariables.PlayerVariables vars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        vars.dragonfruit_time = 0L;
        vars.syncPlayerVariables(player);

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
        // 只发送给当前玩家
        PacketHandler.sendToClient(new SoundStopPacket(player.getId(), soundLoc), player);
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));

        // 4. 卸掉樱桃装甲
        clearTransformationArmor(player);

        // 5. 返还樱桃锁种
        ItemStack cherryLockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY.get());
        if (!player.getInventory().add(cherryLockSeed)) player.spawnAtLocation(cherryLockSeed);

        // 6. 重置腰带状态
        belt.setEquipped(beltStack, false);
        belt.setHenshin(beltStack, false);
        player.inventoryMenu.broadcastChanges();
    }

    private static void handleDragonfruitRelease(ServerPlayer player) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

        if (curio.isEmpty()) return;

        ItemStack beltStack = curio.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.DRAGONFRUIT) return;

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
        // 只发送给当前玩家
        PacketHandler.sendToClient(new SoundStopPacket(player.getId(), soundLoc), player);
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));

        // 4. 卸掉Tyrant装甲
        clearTransformationArmor(player);

        // 5. 返还火龙果锁种
        ItemStack dragonfruitLockSeed = new ItemStack(ModItems.DRAGONFRUIT.get());
        if (!player.getInventory().add(dragonfruitLockSeed)) player.spawnAtLocation(dragonfruitLockSeed);

        // 6. 重置腰带状态
        belt.setEquipped(beltStack, false);
        belt.setHenshin(beltStack, false);
        player.inventoryMenu.broadcastChanges();
    }

    private static void handlePeachRelease(ServerPlayer player) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

        if (curio.isEmpty()) return;

        ItemStack beltStack = curio.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.PEACH) return;

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
        // 只发送给当前玩家
        PacketHandler.sendToClient(new SoundStopPacket(player.getId(), soundLoc), player);
        PacketHandler.sendToServer(new SoundStopPacket(player.getId(), soundLoc));

        // 4. 卸掉Marika装甲
        clearTransformationArmor(player);

        // 5. 返还桃子锁种
        ItemStack peachLockSeed = new ItemStack(ModItems.PEACH_ENERGY.get());
        if (!player.getInventory().add(peachLockSeed)) player.spawnAtLocation(peachLockSeed);

        // 6. 重置腰带状态
        belt.setEquipped(beltStack, false);
        belt.setHenshin(beltStack, false);
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

                // 6. 重置腰带状态
                belt.setEquipped(beltStack, false);
                belt.setHenshin(beltStack, false);

                player.inventoryMenu.broadcastChanges();
            }
        }
    }

    private static void handleDukeBeltRelease(ServerPlayer player) {
        // 公爵解除变身逻辑
        player.getInventory().armor.set(3, ItemStack.EMPTY); // 头盔
        player.getInventory().armor.set(2, ItemStack.EMPTY); // 胸甲
        player.getInventory().armor.set(1, ItemStack.EMPTY); // 护腿
        player.getInventory().armor.set(0, ItemStack.EMPTY); // 靴子

        // 清除公爵相关状态
        player.getPersistentData().remove("duke_energy_sword_active");
        player.getPersistentData().remove("duke_energy_shield_active");
        player.getPersistentData().remove("duke_energy_sword_cooldown");
        player.getPersistentData().remove("duke_tech_buff_time");

    }

    private static void clearTransformationArmor(ServerPlayer player) {
        for (int i = 0; i < 4; i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
    }
}