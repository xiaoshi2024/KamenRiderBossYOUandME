package com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Another_Zi_o;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import java.util.EnumSet;

public class TeleportBehindGoal extends Goal {
    private static final int TELEPORT_COOLDOWN = 60;   // 3 秒
    private final Another_Zi_o mob;

    public TeleportBehindGoal(Another_Zi_o mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.JUMP));   // 不抢 move/look
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (!(target instanceof Player)) return false;

        /* 冷却、概率、距离三重判定 */
        return mob.teleportCooldown <= 0
                && mob.getRNG().nextFloat() < 0.4F
                && mob.distanceToSqr(target) < 8 * 8;
    }

    @Override
    public void start() {
        Player player = (Player) mob.getTarget();
        if (player == null) return;

        /* 计算玩家背后 2 格的位置 */
        Vec3 dir = player.getLookAngle().scale(-1);      // 玩家视线反方向
        Vec3 pos = player.position().add(dir.x * 2, 0, dir.z * 2);

        /* 简单防卡墙：临时把 Y 调到玩家同高 */
        pos = new Vec3(pos.x, player.getY(), pos.z);

        mob.teleportTo(pos.x, pos.y, pos.z);
        mob.teleportCooldown = TELEPORT_COOLDOWN;

        /* 可以加点特效、音效 */
        mob.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }
}