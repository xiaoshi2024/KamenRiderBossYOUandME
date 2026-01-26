package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ai.goal;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

// 说明：
// 在需要打开菜单的地方调用 EntityCuriosListMenu.openMenu(player, targetEntity); 
// 然后如果解除变身，恢复本体模型
// 例如在玩家右击僵尸或村民时：
// if (entity instanceof ServerPlayer && target instanceof Zombie || isMCAVillager(target)) {
//     EntityCuriosListMenu.openMenu((ServerPlayer)entity, target);
// }

public class PickUpDriverGoal extends Goal {
    private final net.minecraft.world.entity.Mob villager;
    private final Level level;
    private ItemEntity targetItem;
    private int cooldown = 0;
    private static final int COOLDOWN_TICKS = 20 * 5; // 5秒冷却时间

    public PickUpDriverGoal(net.minecraft.world.entity.Mob villager) {
        this.villager = villager;
        this.level = villager.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 冷却时间检查
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        // 检查村民是否已经手持物品
        if (!villager.getMainHandItem().isEmpty()) {
            return false;
        }

        // 搜索8格范围内的掉落物
        AABB searchArea = villager.getBoundingBox().inflate(8.0);
        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(
                ItemEntity.class,
                searchArea,
                entity -> entity.isAlive() && !entity.hasPickUpDelay() && EntitySelector.NO_SPECTATORS.test(entity)
        );

        // 筛选出包含Genesis_driver的物品实体
        Optional<ItemEntity> optionalItem = nearbyItems.stream()
                .filter(itemEntity -> {
                    ItemStack stack = itemEntity.getItem();
                    return !stack.isEmpty() && stack.getItem() instanceof Genesis_driver;
                })
                .min((a, b) -> Float.compare(
                        (float) a.distanceToSqr(villager),
                        (float) b.distanceToSqr(villager)
                ));

        if (optionalItem.isPresent()) {
            this.targetItem = optionalItem.get();
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // 如果目标物品不存在或已被拾取，则停止
        if (targetItem == null || targetItem.isRemoved() || targetItem.getItem().isEmpty()) {
            return false;
        }

        // 如果村民已经手持物品，则停止
        if (!villager.getMainHandItem().isEmpty()) {
            return false;
        }

        // 如果目标物品太远，则停止
        return villager.distanceToSqr(targetItem) <= 64.0 && targetItem.isAlive();
    }

    @Override
    public void start() {
        // 开始追踪物品
        // 解除变身时恢复本体模型的逻辑将在Curio槽位内容变化时处理
    }

    @Override
    public void tick() {
        if (targetItem == null || targetItem.isRemoved()) {
            return;
        }

        // 村民向目标物品移动
        villager.getNavigation().moveTo(targetItem, 0.4);

        // 当村民足够接近物品时，尝试拾取
        if (villager.distanceToSqr(targetItem) < 1.0) {
            attemptPickupItem();
        }
    }

    @Override
    public void stop() {
        targetItem = null;
        villager.getNavigation().stop();
    }

    private void attemptPickupItem() {
        if (targetItem != null && !targetItem.isRemoved()) {
            ItemStack itemStack = targetItem.getItem();
            if (itemStack.getItem() instanceof Genesis_driver) {
                // 将物品设置到村民主手
                villager.setItemInHand(villager.getUsedItemHand(), itemStack);
                // 移除地面上的物品
                targetItem.discard();
                // 设置冷却时间
                cooldown = COOLDOWN_TICKS;
            }
        }
    }
}