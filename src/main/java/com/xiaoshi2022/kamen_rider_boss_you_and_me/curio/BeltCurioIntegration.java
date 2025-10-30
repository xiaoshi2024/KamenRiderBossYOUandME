package com.xiaoshi2022.kamen_rider_boss_you_and_me.curio;

import java.util.Map;
import java.util.Optional;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class BeltCurioIntegration {

    static final String BELT_SLOT_TYPE = "belt";

    /**
     * 检查Curios模组是否已加载
     */
    public static boolean isCuriosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    /**
     * 检查实体是否是僵尸类型
     */
    public static boolean isZombieType(LivingEntity entity) {
        // 直接检查是否是Zombie实例
        if (entity instanceof Zombie) {
            return true;
        }
        
        // 获取实体类型的标签并检查是否包含zombie相关标签
        return entity.getType().getTags().anyMatch(tag -> tag.location().getPath().contains("zombie"));
    }

    /**
     * 检查实体是否可以装备腰带到Curio槽位
     */
    public static boolean canEquipToCurioSlot(LivingEntity entity) {
        if (!isCuriosLoaded()) return false;

        // 记录实体类型信息用于调试
        String entityTypeName = entity.getType().toString();
        // LOGGER.debug("Checking if entity can equip belt: " + entityTypeName + ", isVillagerType: " + isVillagerType(entity));

        return isZombieType(entity) || isVillagerType(entity);
    }

    /**
     * 检查实体是否是村民类型（包括所有村民变体和MCA村民）
     */
    public static boolean isVillagerType(LivingEntity entity) {
        // 直接检查实体类型是否是原版村民
        if (entity.getType() == EntityType.VILLAGER || entity.getType() == EntityType.ZOMBIE_VILLAGER) {
            return true;
        }
        
        // 获取实体类型名称和类名，进行全面检查
        String entityTypeName = entity.getType().toString().toLowerCase();
        String className = entity.getClass().getName().toLowerCase();
        
        // 检查是否包含村民相关关键词
        boolean isVillagerName = entityTypeName.contains("villager") || className.contains("villager");
        
        // 特定检查MCA村民
        boolean isMCAVillager = entityTypeName.contains("mca") && entityTypeName.contains("villager") ||
                               className.contains("mca") && className.contains("villager");
        
        // 检查是否是村民实体但没有被正确标记
        boolean isVillagerInstance = entity instanceof Villager;
        
        return isVillagerName || isMCAVillager || isVillagerInstance;
    }

    /**
     * 尝试将腰带装备到Curio槽位
     */
    public static boolean tryEquipBeltToCurioSlot(LivingEntity entity, ItemStack beltStack) {
        if (!isCuriosLoaded()) {
            // LOGGER.debug("Curios not loaded, skipping belt equip");
            return false;
        }

        // 扩展检查，支持所有类型的腰带，通过检查腰带标签或继承关系
        boolean isBelt = beltStack.getItem() instanceof Genesis_driver || 
                        beltStack.getItem() instanceof sengokudrivers_epmty ||
                        beltStack.getItem() instanceof DrakKivaBelt ||
                        beltStack.getItem() instanceof Two_sidriver;

        if (!isBelt) {
            // LOGGER.debug("Item is not a belt, cannot equip: " + beltStack.getItem().getClass().getName());
            return false;
        }

        if (!canEquipToCurioSlot(entity)) {
            // LOGGER.debug("Entity cannot equip belt: " + entity.getType().toString());
            return false;
        }

        boolean success = equipCurio(entity, beltStack, BELT_SLOT_TYPE);
        
        // 为僵尸和村民添加隐藏模型的NBT标签
        if (success && (isZombieType(entity) || isVillagerType(entity))) {
            entity.getPersistentData().putBoolean("hide_original_model", true);
        }
        
        // LOGGER.debug("Belt equip result: " + success);
        return success;
    }

    /**
     * 为实体装备Curio物品 - 修复版本
     */
    public static boolean equipCurio(LivingEntity entity, ItemStack stack, String slotType) {
        if (!isCuriosLoaded() || !canEquipToCurioSlot(entity)) {
            return false;
        }

        Optional<Boolean> result = CuriosApi.getCuriosInventory(entity).resolve()
                .map(inventory -> {
                    // 检查是否有指定类型的槽位
                    Optional<ICurioStacksHandler> handlerOptional = inventory.getStacksHandler(slotType);
                    if (handlerOptional.isPresent()) {
                        ICurioStacksHandler handler = handlerOptional.get();
                        IDynamicStackHandler stacks = handler.getStacks();

                        // 尝试找到空槽位
                        for (int i = 0; i < handler.getSlots(); i++) {
                            if (stacks.getStackInSlot(i).isEmpty()) {
                                try {
                                    // 直接设置物品到槽位
                                    stacks.setStackInSlot(i, stack.copy());

                                    // 清除主手物品（如果之前装备在主手）
                                    ItemStack mainHand = entity.getMainHandItem();
                                    if (mainHand.getItem() instanceof Genesis_driver) {
                                        entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                    }
                                    return true;
                                } catch (Exception e) {
                                    System.err.println("Failed to equip curio: " + e.getMessage());
                                    return false;
                                }
                            }
                        }
                    }
                    return false;
                });

        return result.orElse(false);
    }

    /**
     * 为僵尸装备腰带
     */
    public static boolean equipBeltToZombie(LivingEntity zombie, ItemStack belt) {
        if (!isZombieType(zombie)) {
            return false;
        }
        
        boolean success = equipCurio(zombie, belt, BELT_SLOT_TYPE);
        
        // 添加隐藏模型的NBT标签
        if (success) {
            zombie.getPersistentData().putBoolean("hide_original_model", true);
        }
        
        return success;
    }

    /**
     * 从实体的Curio槽位获取腰带
     */
    public static Optional<ItemStack> getBeltFromCurioSlot(LivingEntity entity) {
        return getCurio(entity, BELT_SLOT_TYPE);
    }

    /**
     * 获取实体的Curio物品
     */
    public static Optional<ItemStack> getCurio(LivingEntity entity, String slotType) {
        if (!isCuriosLoaded() || !canEquipToCurioSlot(entity)) {
            return Optional.empty();
        }

        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(inventory -> inventory.findCurio(slotType, 0))
                .map(SlotResult::stack)
                .filter(stack -> !stack.isEmpty());
    }

    /**
     * 从实体的Curio槽位移除腰带 - 修复版本
     */
    public static Optional<ItemStack> removeBeltFromCurioSlot(LivingEntity entity) {
        return removeCurio(entity, BELT_SLOT_TYPE);
    }

    /**
     * 移除实体的Curio物品 - 修复版本
     */
    public static Optional<ItemStack> removeCurio(LivingEntity entity, String slotType) {
        if (!isCuriosLoaded() || !canEquipToCurioSlot(entity)) {
            return Optional.empty();
        }

        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(inventory -> {
                    // 查找对应的槽位处理器
                    Optional<ICurioStacksHandler> handlerOptional = inventory.getStacksHandler(slotType);
                    if (handlerOptional.isPresent()) {
                        ICurioStacksHandler handler = handlerOptional.get();
                        IDynamicStackHandler stacks = handler.getStacks();

                        // 查找第一个非空槽位
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack stackInSlot = stacks.getStackInSlot(i);
                            if (!stackInSlot.isEmpty()) {
                                // 保存物品堆栈
                                ItemStack removedStack = stackInSlot.copy();
                                // 清空槽位
                                stacks.setStackInSlot(i, ItemStack.EMPTY);
                                
                                // 如果是腰带槽位且实体是僵尸或村民，移除隐藏模型的NBT标签
                                if (slotType.equals(BELT_SLOT_TYPE) && (isZombieType(entity) || isVillagerType(entity))) {
                                    entity.getPersistentData().remove("hide_original_model");
                                }
                                
                                return Optional.of(removedStack);
                            }
                        }
                    }
                    return Optional.empty();
                });
    }
    
    /**
     * 检查实体是否已装备Curio物品
     */
    public static boolean isCurioEquipped(LivingEntity entity, String slotType) {
        if (!isCuriosLoaded() || !canEquipToCurioSlot(entity)) {
            return false;
        }
        
        return getCurio(entity, slotType).isPresent();
    }

    /**
     * 检查实体是否在Curio槽位中装备了腰带
     */
    public static boolean hasBeltInCurioSlot(LivingEntity entity) {
        if (!isCuriosLoaded() || !canEquipToCurioSlot(entity)) {
            return false;
        }
        
        // 正确处理Curio槽位检查
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(inventory -> {
                    try {
                        // 遍历所有Curio槽位
                        for (Map.Entry<String, ICurioStacksHandler> entry : inventory.getCurios().entrySet()) {
                            // 检查是否是腰带槽位
                            if (entry.getKey().equals(BELT_SLOT_TYPE)) {
                                IDynamicStackHandler stackHandler = entry.getValue().getStacks();
                                // 检查槽位中的物品
                                for (int i = 0; i < stackHandler.getSlots(); i++) {
                                    ItemStack stack = stackHandler.getStackInSlot(i);
                                    if (!stack.isEmpty() && stack.getItem() instanceof Genesis_driver) {
                                        return Optional.of(true);
                                    }
                                }
                            }
                        }
                        return Optional.empty();
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                })
                .orElse(false);
    }

    /**
     * 获取实体的腰带（从Curio槽位或主手）
     */
    public static Optional<ItemStack> getBeltFromEntity(LivingEntity entity) {
        // 优先从Curio槽位获取
        Optional<ItemStack> curioBelt = getBeltFromCurioSlot(entity);
        if (curioBelt.isPresent() && curioBelt.get().getItem() instanceof Genesis_driver) {
            return curioBelt;
        }

        // 如果Curio槽位没有，检查主手
        ItemStack mainHandStack = entity.getMainHandItem();
        if (mainHandStack.getItem() instanceof Genesis_driver) {
            return Optional.of(mainHandStack);
        }

        return Optional.empty();
    }

    /**
     * 使用 setEquippedCurio 方法装备饰品
     */
    public static boolean equipCurioUsingSetMethod(LivingEntity entity, ItemStack stack, String slotType) {
        if (!isCuriosLoaded() || !canEquipToCurioSlot(entity)) {
            return false;
        }

        Optional<Boolean> result = CuriosApi.getCuriosInventory(entity).resolve()
                .map(inventory -> {
                    try {
                        // 使用 setEquippedCurio 方法
                        inventory.setEquippedCurio(slotType, 0, stack.copy());

                        // 验证装备是否成功
                        Optional<SlotResult> equipped = inventory.findCurio(slotType, 0);
                        if (equipped.isPresent() && !equipped.get().stack().isEmpty()) {
                            // 清除主手物品
                            ItemStack mainHand = entity.getMainHandItem();
                            if (mainHand.getItem() instanceof Genesis_driver) {
                                entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        System.err.println("Failed to equip using setEquippedCurio: " + e.getMessage());
                        return false;
                    }
                });

        return result.orElse(false);
    }
}