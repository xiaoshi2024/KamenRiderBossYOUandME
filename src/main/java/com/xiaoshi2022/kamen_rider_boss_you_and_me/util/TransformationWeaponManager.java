package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Set;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class TransformationWeaponManager {
    
    /**
     * 当玩家使用战极驱动器变身后，给予对应的武器
     * 只有当配置启用时才会执行
     */
    public static void giveWeaponOnSengokuDriverTransformation(ServerPlayer player, sengokudrivers_epmty.BeltMode beltMode) {
        // 检查是否启用了武器给予功能
        if (!TransformationConfig.isGlobalGiveWeaponEnabled()) {
            return;
        }
        
        // 根据不同的锁种类型给予对应的武器
        ItemStack weaponStack = null;
        
        switch (beltMode) {
            case BANANA:
                // 香蕉锁种 - 给予香蕉矛枪
                weaponStack = new ItemStack(ModItems.BANA_SPEAR.get());
                
                // 为武器添加特殊标签，以便与玩家自己合成的武器区分开
                addTransformationWeaponTag(weaponStack, beltMode.name());
                break;
                
            case ORANGELS:
                // 橙子锁种 - 黑暗铠武形态，给予3把武器
                giveDarkGaimWeapons(player, beltMode);
                return; // 因为已经处理了武器放置，所以直接返回
                
            default:
                // 默认情况不给予武器
                return;
        }
        
        // 检查玩家是否已经拥有相同类型和标签的武器
        if (hasSameWeapon(player, weaponStack)) {
            return; // 如果已经拥有，就不再给予
        }
        
        // 如果玩家主手为空，则放在主手；否则放在副手；如果都不为空，则放在物品栏
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(player.getUsedItemHand(), weaponStack);
        } else if (player.getOffhandItem().isEmpty()) {
            player.getInventory().offhand.set(0, weaponStack);
        } else {
            // 尝试找到物品栏中的空位
            int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
            if (emptySlot != -1) {
                player.getInventory().setItem(emptySlot, weaponStack);
            }
            // 如果没有空位，物品会掉落在地上
        }
        
        // 注释掉多余的聊天栏提示
        /*player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("你获得了变身武器！")
        );*/
    }
    
    /**
     * 当玩家使用创世纪驱动器变身后，给予对应的武器
     * 只有当配置启用时才会执行
     */
    public static void giveWeaponOnGenesisDriverTransformation(ServerPlayer player, Genesis_driver.BeltMode beltMode) {
        // 检查是否启用了武器给予功能
        if (!TransformationConfig.isGlobalGiveWeaponEnabled()) {
            return;
        }
        
        // 根据不同的锁种类型给予对应的武器
        ItemStack weaponStack = null;
        
        switch (beltMode) {
            case LEMON:
                // 柠檬锁种 - 给予音速弓
                weaponStack = new ItemStack(ModItems.SONICARROW.get());  
                
                // 为武器添加特殊标签，并存储锁种信息
                addTransformationWeaponTag(weaponStack, beltMode.name());
                // 存储锁种信息到武器的NBT标签中，以便解除变身后返回
                storeLockSeedInWeapon(weaponStack, "kamen_rider_boss_you_and_me:lemon_energy");
                break;
                
            case MELON:
                // 蜜瓜锁种 - 给予多把武器
                giveMultipleWeapons(player, beltMode);
                return; // 因为已经处理了武器放置，所以直接返回
                
            case CHERRY:
                // 樱桃锁种 - 可以给予其他武器
                weaponStack = new ItemStack(ModItems.SONICARROW.get());
                // 为武器添加特殊标签，并存储锁种信息
                addTransformationWeaponTag(weaponStack, beltMode.name());
                // 存储锁种信息到武器的NBT标签中，以便解除变身后返回
                storeLockSeedInWeapon(weaponStack, "kamen_rider_weapon_craft:cherry");
                break;
                
            case PEACH:
                // 蜜桃锁种 - 可以给予其他武器
                weaponStack = new ItemStack(ModItems.SONICARROW.get());
                // 为武器添加特殊标签，并存储锁种信息
                addTransformationWeaponTag(weaponStack, beltMode.name());
                // 存储锁种信息到武器的NBT标签中，以便解除变身后返回
                storeLockSeedInWeapon(weaponStack, "kamen_rider_boss_you_and_me:peach_energy");
                break;
                
            case DRAGONFRUIT:
                // 火龙果锁种 - 可以给予其他武器
                weaponStack = new ItemStack(ModItems.SONICARROW.get());
                // 为武器添加特殊标签，并存储锁种信息
                addTransformationWeaponTag(weaponStack, beltMode.name());
                // 存储锁种信息到武器的NBT标签中，以便解除变身后返回
                storeLockSeedInWeapon(weaponStack, "kamen_rider_boss_you_and_me:dragonfruit");
                break;
                
            default:
                // 默认情况不给予武器
                return;
        }
        
        // 检查玩家是否已经拥有相同类型和标签的武器
        if (hasSameWeapon(player, weaponStack)) {
            return; // 如果已经拥有，就不再给予
        }
        
        // 如果玩家主手为空，则放在主手；否则放在副手；如果都不为空，则放在物品栏
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(player.getUsedItemHand(), weaponStack);
        } else if (player.getOffhandItem().isEmpty()) {
            player.getInventory().offhand.set(0, weaponStack);
        } else {
            // 尝试找到物品栏中的空位
            int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
            if (emptySlot != -1) {
                player.getInventory().setItem(emptySlot, weaponStack);
            }
            // 如果没有空位，物品会掉落在地上
        }
        
        // 发送消息通知玩家获得了武器
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("你获得了变身武器！")
        );
    }
    
    /**
     * 为武器添加特殊的NBT标签，以便与玩家自己合成的武器区分开
     */
    private static void addTransformationWeaponTag(ItemStack stack, String mode) {
        CompoundTag tag = stack.getOrCreateTag();
        // 添加特殊标签，标识这是通过变身指令给予的武器
        tag.putBoolean("TransformationWeapon", true);
        tag.putString("TransformationMode", mode);
    }
    
    /**
     * 检查物品是否是通过变身指令给予的武器
     */
    public static boolean isTransformationWeapon(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        return stack.getTag().contains("TransformationWeapon") && stack.getTag().getBoolean("TransformationWeapon");
    }
    
    /**
     * 给予玩家多把武器（专门为MELON形态设计）
     */
    private static void giveMultipleWeapons(ServerPlayer player, Genesis_driver.BeltMode beltMode) {
        // 创建多种武器
        ItemStack sonicArrow = new ItemStack(ModItems.SONICARROW.get());
        ItemStack musousaberd = new ItemStack(ModItems.MUSOUSABERD.get());
        
        // 为所有武器添加特殊标签，并为音速弓存储锁种信息
        addTransformationWeaponTag(sonicArrow, beltMode.name());
        addTransformationWeaponTag(musousaberd, beltMode.name());
        // 存储锁种信息到武器的NBT标签中，以便解除变身后返回
        storeLockSeedInWeapon(sonicArrow, "kamen_rider_weapon_craft:melon");
        
        // 放置第一把武器 (音速弓)
        // 只有在玩家没有相同武器时才给予
        if (!hasSameWeapon(player, sonicArrow)) {
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), sonicArrow);
            } else if (player.getOffhandItem().isEmpty()) {
                player.getInventory().offhand.set(0, sonicArrow);
            } else {
                // 尝试找到物品栏中的空位
                int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
                if (emptySlot != -1) {
                    player.getInventory().setItem(emptySlot, sonicArrow);
                } else {
                    // 如果没有空位，物品会掉落在地上
                    player.drop(sonicArrow, false);
                }
            }
        }
        
        // 放置第二把武器 (无双军刀)
        // 只有在玩家没有相同武器时才给予
        if (!hasSameWeapon(player, musousaberd)) {
            // 优先检查主手和副手是否有空间
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), musousaberd);
            } else if (player.getOffhandItem().isEmpty()) {
                player.getInventory().offhand.set(0, musousaberd);
            } else {
                // 再次尝试找到物品栏中的空位
                int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
                if (emptySlot != -1) {
                    player.getInventory().setItem(emptySlot, musousaberd);
                } else {
                    // 如果没有空位，物品会掉落在地上
                    player.drop(musousaberd, false);
                }
            }
        }
    }
    
    /**
     * 给予玩家黑暗铠武的武器
     */
    private static void giveDarkGaimWeapons(ServerPlayer player, sengokudrivers_epmty.BeltMode beltMode) {
        // 创建武器
        ItemStack musousaberd = new ItemStack(ModItems.MUSOUSABERD.get()); // 无双军刀
        ItemStack daid = new ItemStack(ModItems.DAIDAIMARU.get()); // 大橙丸
        ItemStack sonic = new ItemStack(ModItems.SONICARROW.get()); // 音速弩弓
        
        // 为所有武器添加特殊标签
        addTransformationWeaponTag(musousaberd, beltMode.name());
        addTransformationWeaponTag(daid, beltMode.name());
        addTransformationWeaponTag(sonic, beltMode.name());
        
        // 为音速弓存储锁种信息，以便解除变身后返回
        storeLockSeedInWeapon(sonic, "kamen_rider_boss_you_and_me:dark_orangels");
        
        // 放置第一把武器 (无双军刀) - 放置在waist_left饰品槽位
        // 只有在玩家没有相同武器时才给予
        if (!hasSameWeapon(player, musousaberd)) {
            // 尝试将无双军刀放置在waist_left饰品槽位
            // 使用数组来存储布尔值，因为lambda表达式中不能修改外部局部变量
            boolean[] placedInCurios = {false};
            
            // 获取Curios Inventory
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                // 检查waist_left槽位
                curiosInventory.getStacksHandler("waist_left").ifPresent(handler -> {
                    ItemStackHandler stacks = (ItemStackHandler) handler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        if (stacks.getStackInSlot(i).isEmpty()) {
                            stacks.setStackInSlot(i, musousaberd);
                            placedInCurios[0] = true;
                            break;
                        }
                    }
                });
            });
            
            // 如果没有成功放置在饰品槽位，则尝试放置在主手、副手或物品栏
            if (!placedInCurios[0]) {
                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(player.getUsedItemHand(), musousaberd);
                } else if (player.getOffhandItem().isEmpty()) {
                    player.getInventory().offhand.set(0, musousaberd);
                } else {
                    // 尝试找到物品栏中的空位
                    int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
                    if (emptySlot != -1) {
                        player.getInventory().setItem(emptySlot, musousaberd);
                    } else {
                        // 如果没有空位，物品会掉落在地上
                        player.drop(musousaberd, false);
                    }
                }
            }
        }
        
        // 放置大橙丸
        // 只有在玩家没有相同武器时才给予
        if (!hasSameWeapon(player, daid)) {
            // 优先检查主手和副手是否有空间
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), daid);
            } else if (player.getOffhandItem().isEmpty()) {
                player.getInventory().offhand.set(0, daid);
            } else {
                // 尝试找到物品栏中的空位
                int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
                if (emptySlot != -1) {
                    player.getInventory().setItem(emptySlot, daid);
                } else {
                    // 如果没有空位，物品会掉落在地上
                    player.drop(daid, false);
                }
            }
        }

        // 放置音速弩弓
        // 只有在玩家没有相同武器时才给予
        if (!hasSameWeapon(player, sonic)) {
            // 优先检查主手和副手是否有空间
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), sonic);
            } else if (player.getOffhandItem().isEmpty()) {
                player.getInventory().offhand.set(0, sonic);
            } else {
                // 尝试找到物品栏中的空位
                int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
                if (emptySlot != -1) {
                    player.getInventory().setItem(emptySlot, sonic);
                } else {
                    // 如果没有空位，物品会掉落在地上
                    player.drop(sonic, false);
                }
            }
        }
        
        // 注释掉多余的聊天栏提示
        /*player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("你获得了黑暗铠武的武器！")
        );*/
    }
    
    /**
     * 将锁种信息存储到武器的NBT标签中
     */
    private static void storeLockSeedInWeapon(ItemStack weaponStack, String lockSeedId) {
        CompoundTag tag = weaponStack.getOrCreateTag();
        // 存储锁种的物品ID
        tag.putString("LoadedLockSeed", lockSeedId);
        // 存储锁种数量
        tag.putInt("LockSeedCount", 1);
    }
    
    /**
     * 检查玩家是否已经拥有相同类型的武器
     * 对于音速弓，只检查物品类型，不检查形态标签
     */
    private static boolean hasSameWeapon(ServerPlayer player, ItemStack weaponStack) {
        // 对于音速弓，只检查物品类型，不检查标签
        if (weaponStack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow) {
            // 检查主手
            if (!player.getMainHandItem().isEmpty() && 
                player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow) {
                return true;
            }
            
            // 检查副手
            if (!player.getOffhandItem().isEmpty() && 
                player.getOffhandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow) {
                return true;
            }
            
            // 检查物品栏
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && 
                    stack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow) {
                    return true;
                }
            }
            
            // 检查饰品槽位
            // 使用数组来存储布尔值，因为lambda表达式中不能修改外部局部变量
            boolean[] hasWeapon = {false};
            
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                // 检查waist_left槽位
                curiosInventory.getStacksHandler("waist_left").ifPresent(handler -> {
                    ItemStackHandler stacks = (ItemStackHandler) handler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!stack.isEmpty() && 
                            stack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow) {
                            hasWeapon[0] = true;
                            break;
                        }
                    }
                });
            });
            
            return hasWeapon[0];
        }
        
        // 对于其他武器，仍然检查物品类型和标签
        // 检查主手
        if (!player.getMainHandItem().isEmpty() && 
            player.getMainHandItem().getItem() == weaponStack.getItem() && 
            isSameWeaponTag(player.getMainHandItem(), weaponStack)) {
            return true;
        }
        
        // 检查副手
        if (!player.getOffhandItem().isEmpty() && 
            player.getOffhandItem().getItem() == weaponStack.getItem() && 
            isSameWeaponTag(player.getOffhandItem(), weaponStack)) {
            return true;
        }
        
        // 检查物品栏
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && 
                stack.getItem() == weaponStack.getItem() && 
                isSameWeaponTag(stack, weaponStack)) {
                return true;
            }
        }
        
        // 检查饰品槽位
        // 使用数组来存储布尔值，因为lambda表达式中不能修改外部局部变量
        boolean[] hasWeapon = {false};
        
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            // 检查waist_left槽位
            curiosInventory.getStacksHandler("waist_left").ifPresent(handler -> {
                ItemStackHandler stacks = (ItemStackHandler) handler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty() && 
                        stack.getItem() == weaponStack.getItem() && 
                        isSameWeaponTag(stack, weaponStack)) {
                        hasWeapon[0] = true;
                        break;
                    }
                }
            });
        });
        
        return hasWeapon[0];
    }
    
    /**
     * 检查两个武器是否有相同的标签
     */
    private static boolean isSameWeaponTag(ItemStack stack1, ItemStack stack2) {
        // 检查两个武器是否都是变身武器
        if (!isTransformationWeapon(stack1) || !isTransformationWeapon(stack2)) {
            return false;
        }
        
        // 获取两个武器的标签
        CompoundTag tag1 = stack1.getTag();
        CompoundTag tag2 = stack2.getTag();
        
        // 检查标签是否存在且包含TransformationMode
        if (tag1 != null && tag2 != null && 
            tag1.contains("TransformationMode") && tag2.contains("TransformationMode")) {
            // 比较TransformationMode是否相同
            return tag1.getString("TransformationMode").equals(tag2.getString("TransformationMode"));
        }
        
        return false;
    }
    
    /**
     * 清理玩家物品栏中所有通过变身指令给予的武器
     * 在解除变身后调用此方法
     */
    public static void clearTransformationWeapons(ServerPlayer player) {
        // 检查主手 - 先检查是否有装载的锁种
        if (isTransformationWeapon(player.getMainHandItem())) {
            ItemStack mainHand = player.getMainHandItem();
            // 检查并返回装载的锁种
            returnLoadedLockSeed(player, mainHand);
            // 清理武器
            player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
        }
        
        // 检查副手 - 先检查是否有装载的锁种
        if (isTransformationWeapon(player.getOffhandItem())) {
            ItemStack offhand = player.getOffhandItem();
            // 检查并返回装载的锁种
            returnLoadedLockSeed(player, offhand);
            // 清理武器
            player.getInventory().offhand.set(0, ItemStack.EMPTY);
        }
        
        // 检查物品栏 - 先检查是否有装载的锁种
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (isTransformationWeapon(stack)) {
                // 检查并返回装载的锁种
                returnLoadedLockSeed(player, stack);
                // 清理武器
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }
        
        // 检查并清理饰品槽位（waist_left）中的变身武器
        clearCuriosSlots(player);
        
        // 发送消息通知玩家武器已被收回
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("变身武器已被收回！")
        );
    }
    
    /**
     * 检查武器中是否装载了锁种，如果有则返回给玩家
     * 对于音速弓，只检测其本身的变种形态来返回对应的锁种
     */
    private static void returnLoadedLockSeed(ServerPlayer player, ItemStack weaponStack) {
        // 检查是否是音速弓
        if (weaponStack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow) {
            // 获取武器的NBT标签
            CompoundTag tag = weaponStack.getTag();
            
            boolean foundLockSeed = false;
            String lockSeedId = null;
            int count = 1;
            
            // 只检测音速弓本身的变种形态(Mode标签)
            if (tag != null && tag.contains("Mode")) {
                String mode = tag.getString("Mode");
                
                // 根据音速弓本身的Mode标签返回对应的锁种
                switch (mode) {
                    case "MELON":
                        lockSeedId = "kamen_rider_weapon_craft:melon";
                        foundLockSeed = true;
                        break;
                    case "LEMON":
                        lockSeedId = "kamen_rider_boss_you_and_me:lemon_energy";
                        foundLockSeed = true;
                        break;
                    case "CHERRY":
                        lockSeedId = "kamen_rider_weapon_craft:cheryy";
                        foundLockSeed = true;
                        break;
                    case "PEACH":
                        lockSeedId = "kamen_rider_boss_you_and_me:peach_energy";
                        foundLockSeed = true;
                        break;
                    // 添加其他可能的形态
                    case "DRAGONFRUIT":
                        lockSeedId = "kamen_rider_boss_you_and_me:dragonfruit";
                        foundLockSeed = true;
                        break;
                    case "ORANGELS":
                        lockSeedId = "kamen_rider_boss_you_and_me:orangefruit";
                        foundLockSeed = true;
                        break;
                    default:
                        break;
                }
            }
            
            if (foundLockSeed && lockSeedId != null && !lockSeedId.isEmpty()) {
                // 尝试创建锁种物品
                try {
                    // 使用getResourceLocation创建ResourceLocation
                    ResourceLocation lockSeedRL = new ResourceLocation(lockSeedId);
                    Item lockSeedItem = ForgeRegistries.ITEMS.getValue(lockSeedRL);
                    
                    if (lockSeedItem != null) {
                        // 创建锁种物品堆叠
                        ItemStack lockSeedStack = new ItemStack(lockSeedItem, Math.max(1, count));
                        
                        // 将锁种返回给玩家
                        player.addItem(lockSeedStack);
                        
                        // 发送消息通知玩家
                        player.sendSystemMessage(
                                net.minecraft.network.chat.Component.literal("你获得了返回的锁种！" + lockSeedItem.getDescription().getString())
                        );
                    }
                } catch (Exception e) {
                    // 处理可能的异常，但不显示错误消息
                }
                
                // 从武器的NBT标签中移除锁种信息，防止重复返回
                if (tag != null) {
                    tag.remove("LoadedLockSeed");
                    tag.remove("LockSeedCount");
                    tag.remove("lock_seed");
                    tag.remove("lock_seed_count");
                    tag.remove("LockSeed");
                    tag.remove("Count");
                }
            } else {
                // 如果没有找到锁种信息，记录详细的调试信息
                if (tag != null) {
                    // 打印武器的所有标签键，帮助调试
                    Set<String> keys = tag.getAllKeys();
                    System.out.println("音速弓NBT标签键: " + String.join(", ", keys));
                    
                    // 打印Mode标签值，如果存在
                    if (tag.contains("Mode")) {
                        System.out.println("Mode: " + tag.getString("Mode"));
                    }
                } else {
                    System.out.println("音速弓没有NBT标签");
                }
            }
        }
    }
    
    /**
     * 清理饰品槽位中的变身武器
     * 主要清理waist_left槽位，这是用户自定义的饰品槽位
     */
    private static void clearCuriosSlots(ServerPlayer player) {
        // 获取Curios Inventory
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            // 检查waist_left槽位
            curiosInventory.getStacksHandler("waist_left").ifPresent(handler -> {
                ItemStackHandler stacks = (ItemStackHandler) handler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (isTransformationWeapon(stack)) {
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
            
            // 这里可以根据需要添加其他需要检查的饰品槽位
        });
    }
    
    /**
     * 当玩家使用魂灵驱动器变身为黑暗Ghost后，给予对应的武器
     * 只有当配置启用时才会执行
     */
    public static void giveWeaponOnDarkGhostTransformation(ServerPlayer player) {
        // 检查是否启用了武器给予功能
        if (!TransformationConfig.isGlobalGiveWeaponEnabled()) {
            return;
        }
        
        // 给予伽甘枪剑
        ItemStack gangunSaber = new ItemStack(ModItems.GANGUNSABER.get());
        
        // 为武器添加特殊标签
        addTransformationWeaponTag(gangunSaber, "DARK_GHOST");
        
        // 检查玩家是否已经拥有相同类型和标签的武器
        if (hasSameWeapon(player, gangunSaber)) {
            return; // 如果已经拥有，就不再给予
        }
        
        // 如果玩家主手为空，则放在主手；否则放在副手；如果都不为空，则放在物品栏
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(player.getUsedItemHand(), gangunSaber);
        } else if (player.getOffhandItem().isEmpty()) {
            player.getInventory().offhand.set(0, gangunSaber);
        } else {
            // 尝试找到物品栏中的空位
            int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
            if (emptySlot != -1) {
                player.getInventory().setItem(emptySlot, gangunSaber);
            } else {
                // 如果没有空位，物品会掉落在地上
                player.drop(gangunSaber, false);
            }
        }
        
        // 发送消息通知玩家获得了武器
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("你获得了黑暗Ghost的武器！")
        );
    }
    
    /**
     * 当玩家使用魂灵驱动器变身为拿破仑Ghost后，给予对应的武器
     * 只有当配置启用时才会执行
     */
    public static void giveWeaponOnNapoleonGhostTransformation(ServerPlayer player) {
        // 检查是否启用了武器给予功能
        if (!TransformationConfig.isGlobalGiveWeaponEnabled()) {
            return;
        }
        
        // 给予眼剑枪（伽甘枪剑）
        ItemStack gangunSaber = new ItemStack(ModItems.GANGUNSABER.get());
        
        // 为武器添加特殊标签
        addTransformationWeaponTag(gangunSaber, "NAPOLEON_GHOST");
        
        // 检查玩家是否已经拥有相同类型和标签的武器
        if (hasSameWeapon(player, gangunSaber)) {
            return; // 如果已经拥有，就不再给予
        }
        
        // 如果玩家主手为空，则放在主手；否则放在副手；如果都不为空，则放在物品栏
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(player.getUsedItemHand(), gangunSaber);
        } else if (player.getOffhandItem().isEmpty()) {
            player.getInventory().offhand.set(0, gangunSaber);
        } else {
            // 尝试找到物品栏中的空位
            int emptySlot = player.getInventory().findSlotMatchingItem(ItemStack.EMPTY);
            if (emptySlot != -1) {
                player.getInventory().setItem(emptySlot, gangunSaber);
            } else {
                // 如果没有空位，物品会掉落在地上
                player.drop(gangunSaber, false);
            }
        }
        
        // 发送消息通知玩家获得了武器
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("你获得了拿破仑Ghost的武器！")
        );
    }
    
    /**
     * 当玩家解除GhostDriver变身后，清理对应的武器
     * 用于清理眼剑枪（伽甘枪剑）
     */
    public static void clearWeaponsOnGhostDriverDemorph(ServerPlayer player) {
        // 直接调用通用的清理方法，它会清理所有带有TransformationWeapon标签的武器
        clearTransformationWeapons(player);
    }
    
    /**
     * 监听玩家死亡事件，在玩家死亡时清理所有变身武器
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            clearTransformationWeapons(player);
        }
    }
    
    /**
     * 监听玩家丢弃物品事件，当玩家丢弃眼剑枪等变身武器时立即清除
     */
    @SubscribeEvent
    public static void onPlayerDropItem(ItemTossEvent event) {
        ItemEntity droppedItem = event.getEntity();
        ItemStack stack = droppedItem.getItem();
        
        // 检查是否是变身武器（特别是眼剑枪）
        if (isTransformationWeapon(stack)) {
            // 取消原掉落事件
            event.setCanceled(true);
            // 移除物品实体
            droppedItem.discard();
            
            // 可选：通知玩家
            if (event.getPlayer() instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) event.getPlayer();
                serverPlayer.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("变身武器无法被丢弃！")
                );
            }
        }
    }
    
    /**
     * 监听容器操作事件，当玩家尝试将眼剑枪等变身武器放入容器时阻止并清除
     */
    @SubscribeEvent
    public static void onContainerClick(net.minecraftforge.event.entity.player.PlayerContainerEvent event) {
        // 这个事件比较复杂，我们需要确保只在玩家真正尝试放入物品时处理
        // 这里使用一个简单的方法：在容器打开时检查并清除其中的变身武器
        // 更精确的实现可能需要监听更具体的物品移动事件
    }
    
    /**
     * 监听物品拾取事件，防止玩家拾取被丢弃的变身武器
     */
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        ItemEntity itemEntity = event.getItem();
        ItemStack stack = itemEntity.getItem();
        
        // 检查是否是变身武器
        if (isTransformationWeapon(stack)) {
            // 取消拾取事件
            event.setCanceled(true);
            
            // 清除物品实体
            itemEntity.discard();
        }
    }
    
    /**
     * 监听物品移动到容器的事件（更精确的实现）
     */
    @SubscribeEvent
    public static void onPlayerAttemptPickup(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        // 使用getEntity方法获取玩家
        Player player = event.getEntity();
        // 检查玩家手中是否有变身武器
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (isTransformationWeapon(stack)) {
                // 如果玩家尝试对着容器右击，可能是想放入物品
                // 这里可以更精确地检测目标是否是容器
                // 为了简单起见，我们直接在玩家尝试与方块交互时检查
                if (event.getLevel().getBlockEntity(event.getPos()) instanceof Container) {
                    // 清除玩家手中的变身武器
                    player.setItemInHand(hand, ItemStack.EMPTY);
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("变身武器无法放入容器！")
                    );
                    event.setCanceled(true);
                }
            }
        }
    }
}