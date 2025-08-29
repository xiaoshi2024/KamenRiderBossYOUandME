package com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class PickUpHelheimFruitGoal extends Goal {

    private final ElementaryInvesHelheim inves;
    private ItemEntity targetItem;
    private int cooldown = 0;

    public PickUpHelheimFruitGoal(ElementaryInvesHelheim inves) {
        this.inves = inves;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (inves.master != null) return false;          // 有主人时不执行
        if (cooldown > 0) { cooldown--; return false; }

        // 搜索 8 格范围内的掉落物
        AABB box = inves.getBoundingBox().inflate(8.0);
        List<ItemEntity> items = inves.level().getEntitiesOfClass(
                ItemEntity.class, box,
                e -> e.getItem().is(ElementaryInvesHelheim.HELHEIM_FOOD_TAG)
        );
        if (items.isEmpty()) return false;

        // 找最近的
        targetItem = items.stream()
                .min((a, b) -> Float.compare(a.distanceTo(inves), b.distanceTo(inves)))
                .orElse(null);

        return targetItem != null && targetItem.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return targetItem != null && targetItem.isAlive() && inves.distanceTo(targetItem) < 10;
    }

    @Override
    public void start() {
        inves.getNavigation().moveTo(targetItem, 1.2);
    }

    @Override
    public void tick() {
        if (targetItem == null || !targetItem.isAlive()) return;

        inves.getLookControl().setLookAt(targetItem, 10, 10);
        double dist = inves.distanceTo(targetItem);
        if (dist < 1.5) {                 // 够近就“吃掉”
            ItemStack stack = targetItem.getItem();
            if (!stack.isEmpty()) {
                // 吃掉一个
                stack.shrink(1);
                // 可在这里加饱食、回血、BUFF 等
                inves.heal(2.0F);
                inves.playSound(inves.getEatingSound(stack), 1.0F, 1.0F);
            }
            if (stack.isEmpty()) targetItem.discard();
            cooldown = 60;                // 3 秒冷却
        } else {
            inves.getNavigation().moveTo(targetItem, 1.2);
        }
    }

    @Override
    public void stop() {
        targetItem = null;
    }
}