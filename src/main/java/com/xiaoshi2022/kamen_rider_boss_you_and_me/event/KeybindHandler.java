package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.rider_necrom.RidernecromItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.KnecromghostEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.ReleaseBeltPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.WeaponRemovePacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils.findFirstCurio;

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
            
            // Z键临时取下眼魂
            if (KeyBinding.KEY_BARRIER_PULL.isDown() && !keyCooldown) {
                keyCooldown = true;
                handleZKeyPress(player);
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
        if (beltStack.getItem() instanceof Mega_uiorder) {
            return "MEGA_UIORDER";
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
                // 注释掉过期提示，防止刷屏
                // player.displayClientMessage(Component.literal("樱桃锁种已过期，请重新装备"), true);
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
                // 注释掉过期提示，防止刷屏
                // player.displayClientMessage(Component.literal("柠檬锁种已过期！"), true);
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
                // 注释掉过期提示，防止刷屏
                // player.displayClientMessage(Component.literal("火龙果锁种已过期！"), true);
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
                // 注释掉过期提示，防止刷屏
                // player.displayClientMessage(Component.literal("桃子锁种已过期！"), true);
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
                // 注释掉过期提示，防止刷屏
                // player.displayClientMessage(Component.literal("蜜瓜锁种已过期！"), true);
            }
        }
    }

    private static void handleKeyPress(Player player) {
        // 检测双面武器是否为bat形态，实现C键解除EvilBats变身或取下武器
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        // 使用AtomicBoolean来包装handled变量，允许在lambda表达式中修改
        java.util.concurrent.atomic.AtomicBoolean handled = new java.util.concurrent.atomic.AtomicBoolean(false);

        // 获取玩家变量
        KRBVariables.PlayerVariables vars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
        boolean isTransformed = vars.isEvilBatsTransformed;

        // 检查是否佩戴Two_sidriver腰带
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));

        if (!handled.get() && opt.isPresent()) {
            ItemStack belt = opt.get().stack();
            Two_sidriver.DriverType type = Two_sidriver.getDriverType(belt);
            Two_sidriver beltItem = (Two_sidriver) belt.getItem();

            if (type == Two_sidriver.DriverType.BAT) {
                // 腰带为bat形态，发送请求取下武器或解除变身
                if (isTransformed) {
                    // 如果玩家已变身，返回蝙蝠印章并解除变身
                    PacketHandler.sendToServer(new ReleaseBeltPacket(true, "EVIL_BATS"));
                    // 发送武器取下数据包
                    PacketHandler.sendToServer(new WeaponRemovePacket(true));
                    handled.set(true);
                } else {
                    // 如果玩家未变身，发送武器取下数据包
                    PacketHandler.sendToServer(new WeaponRemovePacket(true));
                    handled.set(true);
                }
            }
        }

// 检测主手或副手武器是否为bat形态
        if (!handled.get() && ((mainHandItem.getItem() instanceof TwoWeaponItem &&
                ((TwoWeaponItem)mainHandItem.getItem()).getVariant(mainHandItem) == TwoWeaponItem.Variant.BAT) ||
                (offHandItem.getItem() instanceof TwoWeaponItem &&
                        ((TwoWeaponItem)offHandItem.getItem()).getVariant(offHandItem) == TwoWeaponItem.Variant.BAT) ||
                // 检测新的武器类的BAT形态
                (mainHandItem.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem &&
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.getVariant(mainHandItem) == com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.Variant.BAT) ||
                (offHandItem.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem &&
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.getVariant(offHandItem) == com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.Variant.BAT) ||
                (mainHandItem.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem &&
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.getVariant(mainHandItem) == com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.Variant.BAT) ||
                (offHandItem.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem &&
                        com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.getVariant(offHandItem) == com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem.Variant.BAT))) {
            // 发送解除EvilBats变身请求到服务端
            PacketHandler.sendToServer(new ReleaseBeltPacket(true, "EVIL_BATS"));
            handled.set(true);
        }

        // 黑暗灵骑解除变身逻辑
        if (!handled.get() && vars.isDarkGhostTransformed) {
            // 检查玩家是否装备了魂灵驱动器
            Optional<SlotResult> ghostDriverSlot = findFirstCurio(player,
                    s -> s.getItem() instanceof GhostDriver);
            
            if (ghostDriverSlot.isPresent()) {
                ItemStack beltStack = ghostDriverSlot.get().stack();
                GhostDriver belt = (GhostDriver) beltStack.getItem();
                
                // 检查腰带模式是否为DARK_RIDER_EYE
                if (belt.getMode(beltStack) == GhostDriver.BeltMode.DARK_RIDER_EYE) {
                    // 播放解除变身动画
                    belt.startReleaseAnimation(player, beltStack);
                    // 设置延时，在动画播放完后再发送解除变身请求
                    delayTicks = 40;
                    delayedBeltStack = beltStack.copy();
                    // 发送解除变身请求
                    PacketHandler.sendToServer(new DarkGhostTransformationRequestPacket(true));
                    handled.set(true);
                }
            }
        }
        // 拿破仑魂解除变身逻辑
        else if (!handled.get() && vars.isNapoleonGhostTransformed) {
            // 检查玩家是否装备了魂灵驱动器
            Optional<SlotResult> ghostDriverSlot = findFirstCurio(player,
                    s -> s.getItem() instanceof GhostDriver);
            
            if (ghostDriverSlot.isPresent()) {
                ItemStack beltStack = ghostDriverSlot.get().stack();
                GhostDriver belt = (GhostDriver) beltStack.getItem();
                
                // 检查腰带模式是否为NAPOLEON_GHOST
                if (belt.getMode(beltStack) == GhostDriver.BeltMode.NAPOLEON_GHOST) {
                    // 播放解除变身动画
                    belt.startReleaseAnimation(player, beltStack);
                    // 设置延时，在动画播放完后再发送解除变身请求
                    delayTicks = 40;
                    delayedBeltStack = beltStack.copy();
                    // 发送解除变身请求
                    PacketHandler.sendToServer(new NapoleonGhostTransformationRequestPacket(true));
                    handled.set(true);
                }
            }
        }
        
        //幽冥逻辑
        // ✅ C 键解除 Necrom 变身
        if (!handled.get() && vars.isMegaUiorderTransformed) {
            // 检查玩家是否装备了Mega_uiorder手环
            Optional<SlotResult> megaSlot = findFirstCurio(player,
                    s -> s.getItem() instanceof Mega_uiorder);
            
            // 如果装备了手环，检查是否处于NECROM_EYE模式
            if (megaSlot.isPresent()) {
                ItemStack beltStack = megaSlot.get().stack();
                Mega_uiorder belt = (Mega_uiorder) beltStack.getItem();
                
                if (belt.getCurrentMode(beltStack) == Mega_uiorder.Mode.NECROM_EYE) {
                    // 客户端 KeybindHandler.java
                    PacketHandler.sendToServer(new TransformationRequestPacket(player.getUUID(), RiderTypes.RIDERNECROM, true));
                    handled.set(true);
                } else {
                    // 手环不是眼魂状态，无法解除变身，给出提示
                    player.displayClientMessage(Component.literal("手环不是眼魂状态，无法解除变身！"), true);
                    handled.set(true);
                }
            }
        }
        
        // 检查玩家是否穿着Necrom盔甲并触发眼魔机制
        checkAndTriggerNecromArmorMechanism(player);

        if (!handled.get()) {
            findFirstCurio(player, stack -> stack.getItem() instanceof Genesis_driver)
                    .ifPresent(curio -> {
                        ItemStack beltStack = curio.stack();
                        Genesis_driver belt = (Genesis_driver) beltStack.getItem();
                        Genesis_driver.BeltMode mode = belt.getMode(beltStack);

                        switch (mode) {
                            case LEMON -> {
                                // 先判断是变身还是解除
                                boolean isTransformeds = player.getInventory().armor.get(3).getItem() == ModItems.BARON_LEMON_HELMET.get() || 
                                                        player.getInventory().armor.get(3).getItem() == ModItems.ZANGETSU_SHIN_HELMET.get() ||
                                                        player.getInventory().armor.get(3).getItem() == ModItems.DUKE_HELMET.get();
                                if (isTransformeds) {
                                    // 先播放动画
                                    belt.startReleaseWithPlayerAnimation(player, beltStack);
                                    // 设置延时，在动画播放完后再发送解除变身请求
                                    delayTicks = 40;
                                    delayedBeltStack = beltStack.copy();
                                    handled.set(true);
                                }
                            }
                            case MELON -> {
                                // 先判断是变身还是解除
                                boolean isTransformeds = player.getInventory().armor.get(3).getItem() == ModItems.ZANGETSU_SHIN_HELMET.get();
                                if (isTransformeds) {
                                    // 先播放动画
                                    belt.startReleaseWithPlayerAnimation(player, beltStack);
                                    // 设置延时，在动画播放完后再发送解除变身请求
                                    delayTicks = 40;
                                    delayedBeltStack = beltStack.copy();
                                    handled.set(true);
                                } else {
                                    // 变身
                                    PacketHandler.sendToServer(new MelonTransformationRequestPacket(player.getUUID()));
                                    handled.set(true);
                                }
                            }
                            case CHERRY -> {
                                // 先判断是变身还是解除
                                boolean isTransformeds = player.getInventory().armor.get(3).getItem() == ModItems.SIGURD_HELMET.get();
                                if (isTransformeds) {
                                    // 先播放动画
                                    belt.startReleaseWithPlayerAnimation(player, beltStack);
                                    // 设置延时，在动画播放完后再发送解除变身请求
                                    delayTicks = 40;
                                    delayedBeltStack = beltStack.copy();
                                    handled.set(true);
                                } else {
                                    // 变身
                                    PacketHandler.sendToServer(new CherryTransformationRequestPacket(player.getUUID()));
                                    handled.set(true);
                                }
                            }
                            case PEACH -> {
                                // 先判断是变身还是解除
                                boolean isTransformeds = player.getInventory().armor.get(3).getItem() == ModItems.MARIKA_HELMET.get();
                                if (isTransformeds) {
                                    // 先播放动画
                                    belt.startReleaseWithPlayerAnimation(player, beltStack);
                                    // 设置延时，在动画播放完后再发送解除变身请求
                                    delayTicks = 40;
                                    delayedBeltStack = beltStack.copy();
                                    handled.set(true);
                                } else {
                                    // 变身
                                    PacketHandler.sendToServer(new MarikaTransformationRequestPacket(player.getUUID()));
                                    handled.set(true);
                                }
                            }
                            case DRAGONFRUIT -> {
                                // 先判断是变身还是解除
                                boolean isTransformeds = player.getInventory().armor.get(3).getItem() == ModItems.TYRANT_HELMET.get();
                                if (isTransformeds) {
                                    // 先播放动画
                                    belt.startReleaseWithPlayerAnimation(player, beltStack);
                                    // 设置延时，在动画播放完后再发送解除变身请求
                                    delayTicks = 40;
                                    delayedBeltStack = beltStack.copy();
                                    handled.set(true);
                                }
                                // 移除C键触发变身的功能，只保留解除变身功能
                                // 变身功能应通过X键在KeyInputListener中触发
                            }
                            default -> {}
                        }
                    });
        }

        // Dark Kiva 解除变身逻辑
        if (!handled.get()) {
            findFirstCurio(player,
                            stack -> stack.getItem() instanceof DrakKivaBelt)
                    .ifPresent(curio -> {
                        ItemStack beltStack = curio.stack();
                        boolean isDarkKiva = player.getInventory().armor.get(3).getItem() == ModItems.DARK_KIVA_HELMET.get();

                        if (isDarkKiva) {
                            // 发送解除变身请求
                            PacketHandler.sendToServer(new TransformationRequestPacket(player.getUUID(), "DARK_KIVA", true));
                            delayTicks = 60;
                            delayedBeltStack = beltStack.copy();
                            handled.set(true);
                        }
                    });
        }

        // 香蕉和DarkOrange逻辑
        if (!handled.get()) {
            findFirstCurio(player, stack -> stack.getItem() instanceof sengokudrivers_epmty)
                    .ifPresent(curio -> {
                        ItemStack beltStack = curio.stack();
                        sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();
                        sengokudrivers_epmty.BeltMode mode = belt.getMode(beltStack);

                        if (mode == sengokudrivers_epmty.BeltMode.BANANA) {
                            // 播放解除变身动画
                            belt.startReleaseAnimation(player,beltStack);
                            // 设置延时，在动画播放完后再发送解除变身请求
                            delayTicks = 40;
                            delayedBeltStack = beltStack.copy();
                            handled.set(true);
                        } else if (mode == sengokudrivers_epmty.BeltMode.ORANGELS) {
                            // 发送DarkOrange解除变身请求
                            PacketHandler.sendToServer(new com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkOrangeReleaseRequestPacket(player.getUUID()));
                            belt.startReleaseAnimation(player,beltStack);
                            handled.set(true);
                        }
                    });
        }
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
            case "RIDERNECROM"      -> handleNecromRelease(player);      // Rider Necrom
            case "DARK_GHOST"      -> handleDarkGhostRelease(player);    // 黑暗灵骑
            case "NAPOLEON_GHOST"  -> handleNapoleonGhostRelease(player); // 拿破仑魂
        }
    }
    
    // 黑暗灵骑解除变身
    private static void handleDarkGhostRelease(ServerPlayer player) {
        KRBVariables.PlayerVariables vars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());
        
        // 1. 清除玩家的变身盔甲
        clearTransformationArmor(player);
        
        // 2. 确保状态被正确重置
        if (vars.isDarkGhostTransformed) {
            vars.isDarkGhostTransformed = false;
            vars.syncPlayerVariables(player);
        }
        
        // 3. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    
    // 拿破仑魂解除变身
    private static void handleNapoleonGhostRelease(ServerPlayer player) {
        KRBVariables.PlayerVariables vars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());
        
        // 1. 清除玩家的变身盔甲
        clearTransformationArmor(player);
        
        // 2. 确保状态被正确重置
        if (vars.isNapoleonGhostTransformed) {
            vars.isNapoleonGhostTransformed = false;
            vars.originalNapoleonGhostArmor = null;
            vars.syncPlayerVariables(player);
        }
        
        // 3. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // 新增：Necrom 解除变身
    private static void handleNecromRelease(ServerPlayer player) {
        KRBVariables.PlayerVariables vars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());

        // 1. 清除装甲
        clearTransformationArmor(player);

        // 2. 重置状态
        vars.isMegaUiorderTransformed = false;
        vars.syncPlayerVariables(player);

        // 3. 返还眼魂
        ItemStack eye = new ItemStack(ModItems.NECROM_EYE.get());
        if (!player.getInventory().add(eye)) player.spawnAtLocation(eye);

        // 4. 停止待机音（可选）
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "login_by"
        );
        PacketHandler.sendToAllTrackingAndSelf(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );

        // 清除玩家相关的KnecromghostEntity实体
        for (KnecromghostEntity ghost : player.level().getEntitiesOfClass(
                KnecromghostEntity.class,
                player.getBoundingBox().inflate(20.0D))) {
            if (player.getUUID().equals(ghost.targetPlayerId)) {
                ghost.discard();
            }
        }

        // 重置手环为默认形态
        findFirstCurio(player, s -> s.getItem() instanceof Mega_uiorder)
                .ifPresent(curio -> {
                    ItemStack stack = curio.stack();
                    Mega_uiorder bracelet = (Mega_uiorder) stack.getItem();

                    // 设置为默认模式
                    bracelet.switchMode(stack, Mega_uiorder.Mode.DEFAULT);

                    // 更新 Curios 槽位
                    CurioUtils.updateCurioSlot(player,
                            curio.slotContext().identifier(),
                            curio.slotContext().index(),
                            stack);
                });

        // 5. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // 新增：Evil Bats解除变身逻辑
    // 处理Z键临时取下眼魂的逻辑
    private static void handleZKeyPress(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            // 检查玩家是否装备了Mega_uiorder手环
            Optional<SlotResult> megaSlot = findFirstCurio(player,
                    s -> s.getItem() instanceof Mega_uiorder);
            
            if (megaSlot.isPresent()) {
                ItemStack beltStack = megaSlot.get().stack();
                Mega_uiorder belt = (Mega_uiorder) beltStack.getItem();
                
                // 检查手环是否处于NECROM_EYE模式
                if (belt.getCurrentMode(beltStack) == Mega_uiorder.Mode.NECROM_EYE) {
                    // 获取玩家变量，标记为临时取下
                    KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                    variables.isNecromTemporaryRemoved = true;
                    variables.syncPlayerVariables(player);
                    
                    // 生成眼魂物品还给玩家
                    ItemStack eye = new ItemStack(ModItems.NECROM_EYE.get());
                    if (!player.getInventory().add(eye)) player.spawnAtLocation(eye);
                    
                    // 停止待机音
                    ResourceLocation soundLoc = new ResourceLocation(
                            "kamen_rider_boss_you_and_me",
                            "login_by"
                    );
                    PacketHandler.sendToAllTrackingAndSelf(
                            new SoundStopPacket(player.getId(), soundLoc),
                            player
                    );
                    
                    // 重置手环为默认形态
                    belt.switchMode(beltStack, Mega_uiorder.Mode.DEFAULT);
                    
                    // 更新 Curios 槽位
                    CurioUtils.updateCurioSlot(player,
                            megaSlot.get().slotContext().identifier(),
                            megaSlot.get().slotContext().index(),
                            beltStack);
                    
                    // 播放解除音效
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    player.displayClientMessage(
                            Component.literal("Ghost Eye temporarily removed. Re-equipping will not play standby sound or spawn effect entity."), true);
                }
            }
        }
    }
    
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
        if (mainHandItem.getItem() instanceof TwoWeaponItem ||
            mainHandItem.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem ||
            mainHandItem.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem) {
            
            // 检查武器是否为BAT形态
            if (TwoWeaponItem.getVariant(mainHandItem) == TwoWeaponItem.Variant.BAT) {
                // 将武器形态从BAT改回DEFAULT
                TwoWeaponItem.setVariant(mainHandItem, TwoWeaponItem.Variant.DEFAULT);
            }
        }

        // 处理副手武器
        if (offHandItem.getItem() instanceof TwoWeaponItem ||
            offHandItem.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponSwordItem ||
            offHandItem.getItem() instanceof com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.weapon.TwoWeaponGunItem) {
            
            // 检查武器是否为BAT形态
            if (TwoWeaponItem.getVariant(offHandItem) == TwoWeaponItem.Variant.BAT) {
                // 将武器形态从BAT改回DEFAULT
                TwoWeaponItem.setVariant(offHandItem, TwoWeaponItem.Variant.DEFAULT);
            }
        }

        //停止待机音
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "evil_by"
        );
        PacketHandler.sendToAllTrackingAndSelf(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );


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
                
                // 调用startReleaseWithPlayerAnimation方法确保先播放玩家sodax动画
                belt.startReleaseWithPlayerAnimation(player, newStack);
                
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
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player
                );

                // 清除变身装甲
                clearTransformationArmor(player);
                
                // 清理变身武器
                TransformationWeaponManager.clearTransformationWeapons(player);

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

        // 调用startReleaseWithPlayerAnimation方法确保先播放玩家sodax动画
        ItemStack newStack = beltStack.copy();
        belt.startReleaseWithPlayerAnimation(player, newStack);
        
        // 1. 重置腰带模式
        belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
        SlotResult slotResult = curio.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                newStack
        );

        // 2. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // 3. 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_lockonby");
        // 只发送给当前玩家
        PacketHandler.sendToAllTrackingAndSelf(new SoundStopPacket(player.getId(), soundLoc), player);


        // 4. 卸掉蜜瓜装甲（3 件）
        clearTransformationArmor(player);
        
        // 清理变身武器
        TransformationWeaponManager.clearTransformationWeapons(player);

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

        // 调用startReleaseWithPlayerAnimation方法确保先播放玩家sodax动画
        ItemStack newStack = beltStack.copy();
        belt.startReleaseWithPlayerAnimation(player, newStack);
        
        // 1. 重置腰带模式
        belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
        SlotResult slotResult = curio.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                newStack
        );

        // 2. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // 3. 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_lockonby");
        // 只发送给当前玩家
        PacketHandler.sendToAllTrackingAndSelf(new SoundStopPacket(player.getId(), soundLoc), player);

        // 4. 卸掉樱桃装甲
        clearTransformationArmor(player);
        
        // 清理变身武器
        TransformationWeaponManager.clearTransformationWeapons(player);

        // 5. 返还樱桃锁种
        ItemStack cherryLockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY.get());
        if (!player.getInventory().add(cherryLockSeed)) player.spawnAtLocation(cherryLockSeed);

        // 6. 重置腰带状态
        belt.setEquipped(beltStack, false);
        belt.setHenshin(beltStack, false);
        player.inventoryMenu.broadcastChanges();
    }
    
    // 新增：检查并触发Necrom盔甲机制
    private static void checkAndTriggerNecromArmorMechanism(Player player) {
        // 检查玩家是否穿着Necrom全套盔甲
        boolean isNecromArmorEquipped = isNecromArmorEquipped(player);

        if (isNecromArmorEquipped) {
            // 检查玩家是否装备了Mega_uiorder手环
            Optional<SlotResult> megaSlot = findFirstCurio(player,
                    s -> s.getItem() instanceof Mega_uiorder);

            if (megaSlot.isPresent()) {
                ItemStack beltStack = megaSlot.get().stack();
                Mega_uiorder belt = (Mega_uiorder) beltStack.getItem();
                
                // 如果手环不是NECROM_EYE模式，将其切换到该模式
//                if (belt.getCurrentMode(beltStack) != Mega_uiorder.Mode.NECROM_EYE) {
//                    belt.switchMode(beltStack, Mega_uiorder.Mode.NECROM_EYE);
//
//                    // 更新Curios槽位
//                    CurioUtils.updateCurioSlot(player,
//                            megaSlot.get().slotContext().identifier(),
//                            megaSlot.get().slotContext().index(),
//                            beltStack);
//
//                    // 发送消息提示玩家
//                    player.displayClientMessage(Component.literal("检测到Necrom盔甲，手环已切换至眼魂模式！"), true);
//                }
                
                // 检查玩家是否是眼魔
                boolean isGhostEye = isGhostEyePlayer(player);
                
                // 如果不是眼魔，触发眼魔机制
                if (!isGhostEye) {
                    // 发送数据包到服务器将玩家设置为眼魔
                    if (player instanceof ServerPlayer serverPlayer) {
                        // 这里可以添加将玩家转换为眼魔的逻辑
                        // 例如：设置眼魔种族、添加相关能力等
                        
                        // 记录眼魔状态
                        KRBVariables.PlayerVariables vars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                        vars.isGhostEye = true;
                        vars.syncPlayerVariables(serverPlayer);
                        
                        player.displayClientMessage(Component.literal("已获得眼魔能力！"), true);
                    }
                }
            }
        }
    }
    
    // 检查玩家是否是眼魔
    private static boolean isGhostEyePlayer(Player player) {
        try {
            return player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables()).isGhostEye;
        } catch (Exception e) {
            return false;
        }
    }
    
    // 检查玩家是否穿着Necrom全套盔甲
    private static boolean isNecromArmorEquipped(Player player) {
        // 检查玩家是否穿着Necrom头盔、胸甲和护腿
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof RidernecromItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof RidernecromItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() instanceof RidernecromItem;
    }
    

    private static void handleDragonfruitRelease(ServerPlayer player) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

        if (curio.isEmpty()) return;

        ItemStack beltStack = curio.get().stack();
        Genesis_driver belt = (Genesis_driver) beltStack.getItem();

        if (belt.getMode(beltStack) != Genesis_driver.BeltMode.DRAGONFRUIT) return;

        // 调用startReleaseWithPlayerAnimation方法确保先播放玩家sodax动画
        ItemStack newStack = beltStack.copy();
        belt.startReleaseWithPlayerAnimation(player, newStack);
        
        // 1. 重置腰带模式
        belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
        SlotResult slotResult = curio.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                newStack
        );

        // 2. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // 3. 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_lockonby");
        // 只发送给当前玩家
        PacketHandler.sendToAllTrackingAndSelf(new SoundStopPacket(player.getId(), soundLoc), player);

        // 4. 卸掉Tyrant装甲
        clearTransformationArmor(player);
        
        // 清理变身武器
        TransformationWeaponManager.clearTransformationWeapons(player);

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

        // 调用startReleaseWithPlayerAnimation方法确保先播放玩家sodax动画
        ItemStack newStack = beltStack.copy();
        belt.startReleaseWithPlayerAnimation(player, newStack);
        
        // 1. 重置腰带模式
        belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
        SlotResult slotResult = curio.get();
        CurioUtils.updateCurioSlot(
                player,
                slotResult.slotContext().identifier(),
                slotResult.slotContext().index(),
                newStack
        );

        // 2. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // 3. 停止待机音效
        ResourceLocation soundLoc = new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_lockonby");
        // 只发送给当前玩家
        PacketHandler.sendToAllTrackingAndSelf(new SoundStopPacket(player.getId(), soundLoc), player);

        // 4. 卸掉Marika装甲
        clearTransformationArmor(player);
        
        // 清理变身武器
        TransformationWeaponManager.clearTransformationWeapons(player);

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

                // 停止音效
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "lockonby"
                );
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player
                );

                clearTransformationArmor(player);
                
                // 清理变身武器
                TransformationWeaponManager.clearTransformationWeapons(player);

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

    public static void clearTransformationArmor(ServerPlayer player) {
        for (int i = 0; i < 4; i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
        
        // 解除变身后立即移除隐身效果
        player.removeEffect(MobEffects.INVISIBILITY);
    }
}