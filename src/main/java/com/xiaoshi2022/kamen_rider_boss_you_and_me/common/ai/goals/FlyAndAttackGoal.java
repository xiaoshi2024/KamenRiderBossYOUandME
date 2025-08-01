package com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class FlyAndAttackGoal extends Goal {
    private final ElementaryInvesHelheim mob;

    public FlyAndAttackGoal(ElementaryInvesHelheim mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 有目标且不在冷却时才能飞行
        return mob.getTarget() != null && mob.flyingCooldown <= 0;
    }

    @Override
    public void start() {
        mob.setFlying(true); // 开始飞行
    }

    @Override
    public void stop() {
        mob.setFlying(false); // 停止飞行
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        // 飞行攻击逻辑
        double dx = target.getX() - mob.getX();
        double dy = (target.getY() + 1.0) - mob.getY();
        double dz = target.getZ() - mob.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // 朝向目标
        mob.getLookControl().setLookAt(target, 30f, 30f);

        // 飞行移动
        if (dist > 1.5) {
            mob.setDeltaMovement(
                    dx * 0.15,
                    dy * 0.1,
                    dz * 0.15
            );
        }

        // 攻击判定
        if (dist < 1.5 && mob.getRandom().nextInt(5) == 0) {
            mob.doHurtTarget(target);
        }
    }
}