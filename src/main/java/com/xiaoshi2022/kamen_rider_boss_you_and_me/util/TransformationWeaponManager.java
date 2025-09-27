package com.xiaoshi2022.kamen_rider_boss_you_and_me.util;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraftforge.items.ItemStackHandler;

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
                // 由于目前没有找到音速弓的具体实现，这里假设使用 musousaberd作为临时替代
                // 后续需要替换为实际的音速弓物品
                weaponStack = new ItemStack(ModItems.SONICARROW.get());  
                
                // 为武器添加特殊标签，以便与玩家自己合成的武器区分开
                addTransformationWeaponTag(weaponStack, beltMode.name());
                break;
                
            case MELON:
                // 蜜瓜锁种 - 给予多把武器
                giveMultipleWeapons(player, beltMode);
                return; // 因为已经处理了武器放置，所以直接返回
                
            case CHERRY:
                // 樱桃锁种 - 可以给予其他武器
                weaponStack = new ItemStack(ModItems.SONICARROW.get());
                // 为武器添加特殊标签，以便与玩家自己合成的武器区分开
                addTransformationWeaponTag(weaponStack, beltMode.name());
                break;
                
            case PEACH:
                // 蜜桃锁种 - 可以给予其他武器
                weaponStack = new ItemStack(ModItems.SONICARROW.get());
                // 为武器添加特殊标签，以便与玩家自己合成的武器区分开
                addTransformationWeaponTag(weaponStack, beltMode.name());
                break;
                
            case DRAGONFRUIT:
                // 火龙果锁种 - 可以给予其他武器
                weaponStack = new ItemStack(ModItems.SONICARROW.get());
                // 为武器添加特殊标签，以便与玩家自己合成的武器区分开
                addTransformationWeaponTag(weaponStack, beltMode.name());
                break;
                
            default:
                // 默认情况不给予武器
                return;
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
        
        // 为所有武器添加特殊标签
        addTransformationWeaponTag(sonicArrow, beltMode.name());
        addTransformationWeaponTag(musousaberd, beltMode.name());
        
        // 放置第一把武器 (音速弓)
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
        
        // 放置第二把武器 (无双军刀)
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
        
        // 放置第一把武器 (无双军刀) - 放置在waist_left饰品槽位
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
        
        // 放置大橙丸
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

        // 放置音速弩弓
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
        
        // 发送消息通知玩家获得了武器
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("你获得了黑暗铠武的武器！")
        );
    }
    
    /**
     * 清理玩家物品栏中所有通过变身指令给予的武器
     * 在解除变身后调用此方法
     */
    public static void clearTransformationWeapons(ServerPlayer player) {
        // 检查主手
        if (isTransformationWeapon(player.getMainHandItem())) {
            player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
        }
        
        // 检查副手
        if (isTransformationWeapon(player.getOffhandItem())) {
            player.getInventory().offhand.set(0, ItemStack.EMPTY);
        }
        
        // 检查物品栏
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (isTransformationWeapon(stack)) {
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
}