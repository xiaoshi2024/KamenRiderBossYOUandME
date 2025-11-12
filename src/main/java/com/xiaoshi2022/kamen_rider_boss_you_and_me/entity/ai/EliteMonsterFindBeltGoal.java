package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.BeltCurioIntegration;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EliteMonster.EliteMonsterNpc;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class EliteMonsterFindBeltGoal extends Goal {
    private final EliteMonsterNpc eliteMonster;
    private final Level level;
    private int cooldownTime = 0;
    private int searchRange = 20; // 搜索范围设为20格
    private ItemEntity targetItem = null;

    public EliteMonsterFindBeltGoal(EliteMonsterNpc eliteMonster) {
        this.eliteMonster = eliteMonster;
        this.level = eliteMonster.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 如果冷却中，或者已经在Curio槽位装备了腰带，或者已经手持腰带，则不执行此目标
        if (cooldownTime > 0 || BeltCurioIntegration.hasBeltInCurioSlot(eliteMonster)) {
            // 检查是否手持腰带
            ItemStack mainHand = eliteMonster.getMainHandItem();
            ItemStack offHand = eliteMonster.getOffhandItem();
            if (EliteMonsterEquipHandler.isValidDriver(mainHand) || EliteMonsterEquipHandler.isValidDriver(offHand)) {
                return false;
            }
        }

        // 搜索地面上的腰带物品
        return findNearbyBeltOnGround();
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

        // 处理地面物品
        if (targetItem != null && !level.isClientSide) {
            // 如果物品仍然存在
            if (targetItem.isAlive()) {
                // 移动到物品位置
                double distanceSqr = eliteMonster.distanceToSqr(targetItem.getX(), targetItem.getY(), targetItem.getZ());
                if (distanceSqr > 1.0) {
                    eliteMonster.getNavigation().moveTo(targetItem.getX(), targetItem.getY(), targetItem.getZ(), 1.0);
                } else {
                    // 当接近物品时，主动拾取
                    ItemStack stack = targetItem.getItem().copy();
                    // 优先拾取到Curio槽位
                    if (BeltCurioIntegration.tryEquipBeltToCurioSlot(eliteMonster, stack)) {
                        // 从世界中移除物品
                        targetItem.discard();
                        targetItem = null;
                    } else if (eliteMonster.getMainHandItem().isEmpty()) {
                        // 如果Curio槽位无法装备，则装备到主手
                        eliteMonster.setItemInHand(eliteMonster.getUsedItemHand(), stack);
                        targetItem.discard();
                        targetItem = null;
                    } else if (eliteMonster.getOffhandItem().isEmpty()) {
                        // 如果主手已有物品，则尝试装备到副手
                        eliteMonster.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, stack);
                        targetItem.discard();
                        targetItem = null;
                    }
                }
            } else {
                targetItem = null;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        // 只要有目标物品且没有腰带，就继续执行
        return (targetItem != null && targetItem.isAlive()) && 
               !BeltCurioIntegration.hasBeltInCurioSlot(eliteMonster) &&
               !EliteMonsterEquipHandler.isValidDriver(eliteMonster.getMainHandItem()) &&
               !EliteMonsterEquipHandler.isValidDriver(eliteMonster.getOffhandItem());
    }

    // 查找地面上的任何类型腰带物品
    private boolean findNearbyBeltOnGround() {
        AABB searchBox = new AABB(
                eliteMonster.blockPosition().offset(-searchRange, -searchRange, -searchRange),
                eliteMonster.blockPosition().offset(searchRange, searchRange, searchRange)
        );

        // 查找地面上的物品实体
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, searchBox);
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getItem();
            if (EliteMonsterEquipHandler.isValidDriver(stack)) {
                targetItem = itemEntity;
                return true;
            }
        }

        targetItem = null;
        return false;
    }
}