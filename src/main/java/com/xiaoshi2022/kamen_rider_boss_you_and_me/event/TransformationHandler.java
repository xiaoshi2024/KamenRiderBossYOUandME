package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.TwoWeaponItem;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.KnecromghostEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.SoundStopPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.TransformationWeaponManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils.findFirstCurio;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class TransformationHandler {
    
    // 清除玩家的变身盔甲
    public static void clearTransformationArmor(ServerPlayer player) {
        // 逐个清除盔甲
        player.getInventory().armor.set(0, ItemStack.EMPTY); // 靴子
        player.getInventory().armor.set(1, ItemStack.EMPTY); // 护腿
        player.getInventory().armor.set(2, ItemStack.EMPTY); // 胸甲
        player.getInventory().armor.set(3, ItemStack.EMPTY); // 头盔
    }
    
    // 处理解除变身的核心方法
    public static void completeBeltRelease(ServerPlayer player, String beltType) {
        KRBVariables.PlayerVariables vars = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                .orElse(new KRBVariables.PlayerVariables());
        
        // 根据不同的骑士类型执行不同的解除逻辑
        switch (beltType) {
            case "GENESIS", "GENESIS_LEMON" -> handleGenesisBeltRelease(player, vars);
            case "GENESIS_MELON" -> handleMelonRelease(player, vars);
            case "GENESIS_CHERRY" -> handleCherryRelease(player, vars);
            case "GENESIS_PEACH" -> handlePeachRelease(player, vars);
            case "GENESIS_DRAGONFRUIT" -> handleDragonfruitRelease(player, vars);
            case "BARONS" -> handleBaronsBeltRelease(player, vars);
            case "DUKE" -> handleDukeBeltRelease(player, vars);
            case "EVIL_BATS" -> handleEvilBatsRelease(player, vars);
            case "RIDERNECROM" -> handleNecromRelease(player, vars);
            case "DARK_GHOST" -> handleDarkGhostRelease(player, vars);
            case "NAPOLEON_GHOST" -> handleNapoleonGhostRelease(player, vars);
        }
        
        // 同步玩家背包变化
        player.inventoryMenu.broadcastChanges();
    }
    
    // 黑暗灵骑解除变身
    private static void handleDarkGhostRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
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
    private static void handleNapoleonGhostRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
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
    
    // RIDERNECROM 解除变身
    private static void handleNecromRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        // 1. 清除玩家的变身盔甲
        clearTransformationArmor(player);
        
        // 2. 重置状态
        vars.isMegaUiorderTransformed = false;
        vars.syncPlayerVariables(player);
        
        // 3. 返还眼魂
        ItemStack eye = new ItemStack(ModItems.NECROM_EYE.get());
        if (!player.getInventory().add(eye)) player.spawnAtLocation(eye);
        
        // 4. 停止待机音
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "login_by"
        );
        PacketHandler.sendToAllTrackingAndSelf(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );
        
        // 5. 清除玩家相关的KnecromghostEntity实体
        for (KnecromghostEntity ghost : player.level().getEntitiesOfClass(
                KnecromghostEntity.class,
                player.getBoundingBox().inflate(20.0D))) {
            if (player.getUUID().equals(ghost.targetPlayerId)) {
                ghost.discard();
            }
        }
        
        // 6. 重置手环为默认形态
        Optional<SlotResult> megaSlot = findFirstCurio(player, s -> s.getItem() instanceof Mega_uiorder);
        megaSlot.ifPresent(curio -> {
            ItemStack stack = curio.stack();
            Mega_uiorder bracelet = (Mega_uiorder) stack.getItem();
            
            // 设置为默认模式
            if (bracelet.getCurrentMode(stack) == Mega_uiorder.Mode.NECROM_EYE) {
                bracelet.switchMode(stack, Mega_uiorder.Mode.DEFAULT);
                
                // 更新 Curios 槽位
                CurioUtils.updateCurioSlot(player,
                        curio.slotContext().identifier(),
                        curio.slotContext().index(),
                        stack);
            }
        });
        
        // 7. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    
    // EVIL BATS 解除变身
    private static void handleEvilBatsRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        // 1. 清除玩家的变身盔甲
        clearTransformationArmor(player);
        
        // 2. 更新玩家变量
        vars.isEvilBatsTransformed = false;
        vars.syncPlayerVariables(player);
        
        // 3. 检查并更新玩家手持的双面武器
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        
        // 处理主手武器
        if (mainHandItem.getItem() instanceof TwoWeaponItem) {
            if (TwoWeaponItem.getVariant(mainHandItem) == TwoWeaponItem.Variant.BAT) {
                TwoWeaponItem.setVariant(mainHandItem, TwoWeaponItem.Variant.DEFAULT);
            }
        }
        
        // 处理副手武器
        if (offHandItem.getItem() instanceof TwoWeaponItem) {
            if (TwoWeaponItem.getVariant(offHandItem) == TwoWeaponItem.Variant.BAT) {
                TwoWeaponItem.setVariant(offHandItem, TwoWeaponItem.Variant.DEFAULT);
            }
        }
        
        // 4. 停止待机音
        ResourceLocation soundLoc = new ResourceLocation(
                "kamen_rider_boss_you_and_me",
                "evil_by"
        );
        PacketHandler.sendToAllTrackingAndSelf(
                new SoundStopPacket(player.getId(), soundLoc),
                player
        );
        
        // 5. 检查并更新腰带状态
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
                CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(), belt);
            }
        }
        
        // 6. 返还蝙蝠印章
        ItemStack batStamp = new ItemStack(ModItems.BAT_STAMP.get());
        if (!player.getInventory().add(batStamp)) {
            player.spawnAtLocation(batStamp);
        }
        
        // 7. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    
    // DUKE 解除变身
    private static void handleDukeBeltRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        // 1. 清除玩家的变身盔甲
        clearTransformationArmor(player);
        
        // 2. 重置腰带状态
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));
        
        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            
            // 重置腰带模式
            belt.setMode(beltStack, Genesis_driver.BeltMode.MELON);
            
            // 更新Curios槽位
            CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                    slotResult.slotContext().index(), beltStack);
        }
        
        // 3. 清理变身武器
        TransformationWeaponManager.clearTransformationWeapons(player);
        
        // 4. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    
    // BARONS 解除变身
    private static void handleBaronsBeltRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        // 1. 清除玩家的变身盔甲
        clearTransformationArmor(player);
        
        // 2. 重置腰带状态
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof sengokudrivers_epmty));
        
        // 3. 检查并返还香蕉锁种，只有当腰带处于香蕉模式时才返还
        boolean hasBananaMode = false;
        
        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();
            
            // 检查腰带当前模式
            if (belt.getMode(beltStack) == sengokudrivers_epmty.BeltMode.BANANA) {
                hasBananaMode = true;
            }
            
            // 重置腰带模式为默认
            belt.setMode(beltStack, sengokudrivers_epmty.BeltMode.DEFAULT);
            
            // 更新Curios槽位
            CurioUtils.updateCurioSlot(player, slotResult.slotContext().identifier(),
                    slotResult.slotContext().index(), beltStack);
        }
        
        // 4. 清理变身武器
        TransformationWeaponManager.clearTransformationWeapons(player);
        
        // 5. 只有当腰带确实处于香蕉模式时，才返还香蕉锁种
        if (hasBananaMode) {
            ItemStack bananaLockSeed = new ItemStack(ModItems.BANANAFRUIT.get());
            if (!player.getInventory().add(bananaLockSeed)) {
                player.spawnAtLocation(bananaLockSeed);
            }
        }
        
        // 6. 播放解除音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    
    // 柠檬解除变身
    private static void handleGenesisBeltRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));
        
        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            
            if (belt.getMode(beltStack) == Genesis_driver.BeltMode.LEMON) {
                // 创建新的腰带堆栈副本
                ItemStack newStack = beltStack.copy();
                
                // 1. 播放解除变身动画（包括sodax玩家动画）
                belt.startReleaseWithPlayerAnimation(player, newStack);
                
                // 2. 重置腰带模式为默认
                belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
                belt.setEquipped(newStack, false);
                belt.setHenshin(newStack, false);
                
                // 3. 更新Curio槽位
                CurioUtils.updateCurioSlot(
                        player,
                        slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(),
                        newStack
                );
                
                // 4. 停止待机音效
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "lemon_lockonby"
                );
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player
                );
                
                // 5. 清除变身盔甲
                clearTransformationArmor(player);
                
                // 6. 清理变身武器
                TransformationWeaponManager.clearTransformationWeapons(player);
                
                // 7. 返还柠檬锁种
                ItemStack lemonLockSeed = new ItemStack(ModItems.LEMON_ENERGY.get());
                if (!player.getInventory().add(lemonLockSeed)) {
                    player.spawnAtLocation(lemonLockSeed);
                }
                
                // 8. 播放解除音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
    
    // 蜜瓜解除变身
    private static void handleMelonRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));
        
        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            
            if (belt.getMode(beltStack) == Genesis_driver.BeltMode.MELON) {
                // 创建新的腰带堆栈副本
                ItemStack newStack = beltStack.copy();
                
                // 1. 播放解除变身动画（包括sodax玩家动画）
                belt.startReleaseWithPlayerAnimation(player, newStack);
                
                // 2. 重置腰带模式为默认
                belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
                belt.setEquipped(newStack, false);
                belt.setHenshin(newStack, false);
                
                // 3. 更新Curio槽位
                CurioUtils.updateCurioSlot(
                        player,
                        slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(),
                        newStack
                );
                
                // 4. 停止待机音效
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "lemon_lockonby"
                );
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player
                );
                
                // 5. 清除变身盔甲
                clearTransformationArmor(player);
                
                // 6. 清理变身武器
                TransformationWeaponManager.clearTransformationWeapons(player);
                
                // 7. 返还蜜瓜锁种
                ItemStack melonLockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.MELON.get());
                if (!player.getInventory().add(melonLockSeed)) {
                    player.spawnAtLocation(melonLockSeed);
                }
                
                // 8. 播放解除音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
    
    // 樱桃解除变身
    private static void handleCherryRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));
        
        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            
            if (belt.getMode(beltStack) == Genesis_driver.BeltMode.CHERRY) {
                // 创建新的腰带堆栈副本
                ItemStack newStack = beltStack.copy();
                
                // 1. 播放解除变身动画（包括sodax玩家动画）
                belt.startReleaseWithPlayerAnimation(player, newStack);
                
                // 2. 重置腰带模式为默认
                belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
                belt.setEquipped(newStack, false);
                belt.setHenshin(newStack, false);
                
                // 3. 更新Curio槽位
                CurioUtils.updateCurioSlot(
                        player,
                        slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(),
                        newStack
                );
                
                // 4. 停止待机音效
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "lemon_lockonby"
                );
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player
                );
                
                // 5. 清除变身盔甲
                clearTransformationArmor(player);
                
                // 6. 清理变身武器
                TransformationWeaponManager.clearTransformationWeapons(player);
                
                // 7. 返还樱桃锁种
                ItemStack cherryLockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY.get());
                if (!player.getInventory().add(cherryLockSeed)) {
                    player.spawnAtLocation(cherryLockSeed);
                }
                
                // 8. 播放解除音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
    
    // 桃子解除变身
    private static void handlePeachRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));
        
        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            
            if (belt.getMode(beltStack) == Genesis_driver.BeltMode.PEACH) {
                // 创建新的腰带堆栈副本
                ItemStack newStack = beltStack.copy();
                
                // 1. 播放解除变身动画（包括sodax玩家动画）
                belt.startReleaseWithPlayerAnimation(player, newStack);
                
                // 2. 重置腰带模式为默认
                belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
                belt.setEquipped(newStack, false);
                belt.setHenshin(newStack, false);
                
                // 3. 更新Curio槽位
                CurioUtils.updateCurioSlot(
                        player,
                        slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(),
                        newStack
                );
                
                // 4. 停止待机音效
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "lemon_lockonby"
                );
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player
                );
                
                // 5. 清除变身盔甲
                clearTransformationArmor(player);
                
                // 6. 清理变身武器
                TransformationWeaponManager.clearTransformationWeapons(player);
                
                // 7. 返还桃子锁种
                ItemStack peachLockSeed = new ItemStack(ModItems.PEACH_ENERGY.get());
                if (!player.getInventory().add(peachLockSeed)) {
                    player.spawnAtLocation(peachLockSeed);
                }
                
                // 8. 播放解除音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
    
    // 火龙果解除变身
    private static void handleDragonfruitRelease(ServerPlayer player, KRBVariables.PlayerVariables vars) {
        Optional<SlotResult> curio = CuriosApi.getCuriosInventory(player)
                .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));
        
        if (curio.isPresent()) {
            SlotResult slotResult = curio.get();
            ItemStack beltStack = slotResult.stack();
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            
            if (belt.getMode(beltStack) == Genesis_driver.BeltMode.DRAGONFRUIT) {
                // 创建新的腰带堆栈副本
                ItemStack newStack = beltStack.copy();
                
                // 1. 播放解除变身动画（包括sodax玩家动画）
                belt.startReleaseWithPlayerAnimation(player, newStack);
                
                // 2. 重置腰带模式为默认
                belt.setMode(newStack, Genesis_driver.BeltMode.DEFAULT);
                belt.setEquipped(newStack, false);
                belt.setHenshin(newStack, false);
                
                // 3. 更新Curio槽位
                CurioUtils.updateCurioSlot(
                        player,
                        slotResult.slotContext().identifier(),
                        slotResult.slotContext().index(),
                        newStack
                );
                
                // 4. 停止待机音效
                ResourceLocation soundLoc = new ResourceLocation(
                        "kamen_rider_boss_you_and_me",
                        "lemon_lockonby"
                );
                PacketHandler.sendToAllTrackingAndSelf(
                        new SoundStopPacket(player.getId(), soundLoc),
                        player
                );
                
                // 5. 清除变身盔甲
                clearTransformationArmor(player);
                
                // 6. 清理变身武器
                TransformationWeaponManager.clearTransformationWeapons(player);
                
                // 7. 返还火龙果锁种
                ItemStack dragonfruitLockSeed = new ItemStack(ModItems.DRAGONFRUIT.get());
                if (!player.getInventory().add(dragonfruitLockSeed)) {
                    player.spawnAtLocation(dragonfruitLockSeed);
                }
                
                // 8. 播放解除音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}