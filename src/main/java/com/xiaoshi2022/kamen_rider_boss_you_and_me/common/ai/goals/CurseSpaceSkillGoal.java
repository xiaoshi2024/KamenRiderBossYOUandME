package com.xiaoshi2022.kamen_rider_boss_you_and_me.common.ai.goals;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.GiifuHumanEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension.GiifuCurseDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.EnumSet;

public class CurseSpaceSkillGoal extends Goal {
    private final GiifuHumanEntity entity;
    private int castTick = 0;
    private LivingEntity target;

    public CurseSpaceSkillGoal(GiifuHumanEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = entity.getTarget();
        return target != null &&
                target instanceof Player &&
                entity.tickCount % 200 == 0 &&
                entity.getRandom().nextFloat() < 0.3f;
    }

    @Override
    public void start() {
        this.castTick = 40; // 减少到2秒施法时间
        entity.getNavigation().stop(); // 停止移动
        entity.level().broadcastEntityEvent(entity, (byte) 5);
    }

    @Override
    public void tick() {
        if (castTick > 0) {
            castTick--;

            // 在施法过程中看向目标
            if (target != null) {
                entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }

            // 施法完成时传送
            if (castTick == 0 && target instanceof ServerPlayer serverPlayer) {
                teleportPlayerToCurseDimension(serverPlayer);
            }
        }
    }

    private void teleportPlayerToCurseDimension(ServerPlayer player) {
        ServerLevel curseLevel = player.server.getLevel(GiifuCurseDimension.GIIFU_CURSE_DIM);
        if (curseLevel == null) return;

        BlockPos center = GiifuCurseDimension.PLATFORM_CENTER;

        // 最简化的传送 - 直接传送，平台会在需要时异步生成
        player.teleportTo(
                curseLevel,
                center.getX() + 0.5,
                center.getY() + 2,
                center.getZ() + 0.5,
                player.getYRot(),
                player.getXRot()
        );

        // 异步生成平台（如果不存在）
        player.server.execute(() -> {
            ChunkPos chunkPos = new ChunkPos(center);
            curseLevel.getChunkSource().addRegionTicket(
                    TicketType.POST_TELEPORT,
                    chunkPos, 1, 1
            );
            GiifuCurseDimension.generatePlatform(curseLevel);
        });

        curseLevel.playSound(null, center,
                net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.HOSTILE,
                1.0f, 1.0f);
    }

    @Override
    public boolean canContinueToUse() {
        return castTick > 0 && target != null && target.isAlive();
    }

    @Override
    public void stop() {
        this.castTick = 0;
        this.target = null;
    }
}