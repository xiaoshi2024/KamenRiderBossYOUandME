package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class FindAndEquipDriverGoal extends Goal {
    private final Zombie zombie;
    private final Level level;
    private int cooldownTime = 0;
    private int searchRange = 16;
    private BlockPos targetChest = null;
    private ItemEntity targetItem = null;

    public FindAndEquipDriverGoal(Zombie zombie) {
        this.zombie = zombie;
        this.level = zombie.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 如果冷却中，或者已经手持腰带，则不执行此目标
        if (cooldownTime > 0 || (zombie.getMainHandItem().getItem() instanceof Genesis_driver)) {
            return false;
        }

        // 先搜索地面上的物品
        boolean foundItem = findNearbyDriverOnGround();
        if (!foundItem) {
            // 如果没有找到地面物品，再搜索箱子
            return findNearbyChestWithDriver();
        }
        return foundItem;
    }

    @Override
    public void start() {
        cooldownTime = 200; // 200tick = 10秒的冷却时间
    }

    @Override
    public void tick() {
        if (cooldownTime > 0) {
            cooldownTime--;
        }

        // 优先处理地面物品
        if (targetItem != null && !level.isClientSide) {
            // 如果物品仍然存在
            if (targetItem.isAlive()) {
                // 移动到物品位置
                double distanceSqr = zombie.distanceToSqr(targetItem.getX(), targetItem.getY(), targetItem.getZ());
                if (distanceSqr > 1.0) {
                    zombie.getNavigation().moveTo(targetItem.getX(), targetItem.getY(), targetItem.getZ(), 1.0);
                } else {
                    // 当接近物品时，主动拾取
                    if (zombie.getMainHandItem().isEmpty()) {
                        ItemStack stack = targetItem.getItem().copy();
                        // 装备到主手
                        zombie.setItemInHand(zombie.getUsedItemHand(), stack);
                        // 从世界中移除物品
                        targetItem.discard();
                        targetItem = null;
                    }
                }
            } else {
                targetItem = null;
            }
        }
        // 处理箱子
        else if (targetChest != null && !level.isClientSide) {
            // 移动到箱子位置
            if (zombie.distanceToSqr(targetChest.getX() + 0.5, targetChest.getY() + 0.5, targetChest.getZ() + 0.5) > 1.0) {
                zombie.getNavigation().moveTo(targetChest.getX() + 0.5, targetChest.getY() + 0.5, targetChest.getZ() + 0.5, 1.0);
            } else {
                // 到达箱子后，尝试获取腰带
                BlockEntity blockEntity = level.getBlockEntity(targetChest);
                if (blockEntity instanceof ChestBlockEntity chest) {
                    for (int i = 0; i < chest.getContainerSize(); i++) {
                        ItemStack stack = chest.getItem(i);
                        if (stack.getItem() instanceof Genesis_driver && ((Genesis_driver) stack.getItem()).getMode(stack) != Genesis_driver.BeltMode.DEFAULT) {
                            // 如果僵尸主手为空，则装备腰带
                            if (zombie.getMainHandItem().isEmpty()) {
                                zombie.setItemInHand(zombie.getUsedItemHand(), stack.copy());
                                chest.removeItem(i, 1);
                                break;
                            }
                        }
                    }
                }
                targetChest = null;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        // 只要有目标（箱子或物品）且主手为空，就继续执行
        return (targetChest != null || (targetItem != null && targetItem.isAlive())) && zombie.getMainHandItem().isEmpty();
    }

    // 查找附近含有非默认模式腰带的箱子
    private boolean findNearbyChestWithDriver() {
        AABB searchBox = new AABB(
                zombie.blockPosition().offset(-searchRange, -searchRange, -searchRange),
                zombie.blockPosition().offset(searchRange, searchRange, searchRange)
        );

        // 检查箱子
        int minX = (int) searchBox.minX;
        int minY = (int) searchBox.minY;
        int minZ = (int) searchBox.minZ;
        int maxX = (int) searchBox.maxX;
        int maxY = (int) searchBox.maxY;
        int maxZ = (int) searchBox.maxZ;

        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() == Blocks.CHEST || state.getBlock() == Blocks.TRAPPED_CHEST) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ChestBlockEntity chest) {
                    for (int i = 0; i < chest.getContainerSize(); i++) {
                        ItemStack stack = chest.getItem(i);
                        if (stack.getItem() instanceof Genesis_driver) {
                            Genesis_driver driver = (Genesis_driver) stack.getItem();
                            // 只寻找非默认模式的腰带
                            if (driver.getMode(stack) != Genesis_driver.BeltMode.DEFAULT) {
                                targetChest = pos;
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    // 查找地面上的非默认模式腰带物品
    private boolean findNearbyDriverOnGround() {
        AABB searchBox = new AABB(
                zombie.blockPosition().offset(-searchRange, -searchRange, -searchRange),
                zombie.blockPosition().offset(searchRange, searchRange, searchRange)
        );

        // 查找地面上的物品实体
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, searchBox);
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof Genesis_driver driver) {
                // 只寻找非默认模式的腰带
                if (driver.getMode(stack) != Genesis_driver.BeltMode.DEFAULT) {
                    targetItem = itemEntity;
                    targetChest = null; // 优先拾取地面物品
                    return true;
                }
            }
        }

        targetItem = null;
        return false;
    }

}