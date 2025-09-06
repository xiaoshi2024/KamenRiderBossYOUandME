package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension.GiifuCurseDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = kamen_rider_boss_you_and_me.MODID)
public class GiifuCurseSpawnHandler {

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() != GiifuCurseDimension.GIIFU_CURSE_DIM) return;

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();
        BlockPos center = GiifuCurseDimension.PLATFORM_CENTER;

        // 1. 预加载区块
        level.getChunkSource().addRegionTicket(
                TicketType.POST_TELEPORT,
                new ChunkPos(center), 3, 3);

        // 2. 确保平台存在（同步生成，立即完成）
        GiifuCurseDimension.generatePlatform(level);

        // 3. 直接传送到平台中心，无需等待
        player.teleportTo(center.getX() + 0.5, center.getY() + 2, center.getZ() + 0.5);

    }
}