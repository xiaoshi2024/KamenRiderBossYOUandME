package com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

public class AttractGoal extends Goal {
    private final Mob mob;
    private final TagKey<Item> attractTag;
    private final double speedModifier;
    private Player targetPlayer;
    private int calmDown;

    public AttractGoal(Mob mob, ResourceLocation tagLocation, double speedModifier) {
        this.mob = mob;
        this.attractTag = ItemTags.create(tagLocation);
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 冷却时间处理
        if (this.calmDown > 0) {
            this.calmDown--;
            return false;
        }

        // 在9格范围内寻找持有吸引物品的玩家
        this.targetPlayer = this.mob.level().getNearestPlayer(this.mob, 9.0);

        if (this.targetPlayer == null) {
            return false;
        }

        return hasAttractiveItem(this.targetPlayer);
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPlayer != null
                && this.targetPlayer.isAlive()
                && this.mob.distanceToSqr(this.targetPlayer) < 100.0 // 10格平方
                && hasAttractiveItem(this.targetPlayer);
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.targetPlayer, this.speedModifier);
    }

    @Override
    public void tick() {
        if (this.targetPlayer != null) {
            // 面向玩家
            this.mob.getLookControl().setLookAt(this.targetPlayer, 10.0F, (float)this.mob.getMaxHeadXRot());

            // 如果距离大于3格，继续移动
            if (this.mob.distanceToSqr(this.targetPlayer) > 9.0) {
                this.mob.getNavigation().moveTo(this.targetPlayer, this.speedModifier);
            } else {
                // 到达后停止移动
                this.mob.getNavigation().stop();
            }
        }
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        this.mob.getNavigation().stop();
        this.calmDown = reducedTickDelay(100); // 5秒冷却
    }

    private boolean hasAttractiveItem(Player player) {
        // 检查主手和副手物品
        if (isAttractiveItem(player.getMainHandItem()) || isAttractiveItem(player.getOffhandItem())) {
            return true;
        }

        // 检查背包中的物品（可选）
        for (ItemStack itemStack : player.getInventory().items) {
            if (isAttractiveItem(itemStack)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAttractiveItem(ItemStack stack) {
        // 1. 优先检测标准物品标签（JSON定义）
        if (stack.is(this.attractTag)) {
            return true;
        }

        // 2. 检测NBT标记（兼容手动标记）
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }

        // 3. 检查所有可能的NBT键名（兼容新旧版本）
        return tag.contains("HelheimAttract", Tag.TAG_BYTE) && tag.getBoolean("HelheimAttract")
                || tag.contains("Tags", Tag.TAG_STRING)
                && tag.getString("Tags").equals("kamen_rider_helheim_food");
    }
}