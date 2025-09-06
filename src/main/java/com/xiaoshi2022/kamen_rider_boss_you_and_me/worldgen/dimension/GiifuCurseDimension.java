package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class GiifuCurseDimension {
    public static final ResourceKey<Level> GIIFU_CURSE_DIM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "giifu_curse")
    );

    // 平台中心坐标
    public static final BlockPos PLATFORM_CENTER = new BlockPos(0, 64, 0);

    // 平台生成状态缓存
    private static final Set<ResourceKey<Level>> GENERATED_PLATFORMS = new HashSet<>();

    /**
     * 生成或确保平台存在（优化版）
     */
    public static void generatePlatform(ServerLevel level) {
        // 检查是否已经生成过
        if (GENERATED_PLATFORMS.contains(level.dimension())) {
            return;
        }

        BlockPos center = PLATFORM_CENTER;
        BlockState shell = ModBlocks.CURSE_BLOCK.get().defaultBlockState();

        // 快速检查平台是否存在
        boolean platformExists = true;
        BlockPos[] checkPoints = {
                center.offset(-8, 0, -8), center.offset(8, 0, -8),
                center.offset(-8, 0, 8), center.offset(8, 0, 8)
        };

        for (BlockPos pos : checkPoints) {
            if (!level.getBlockState(pos).is(shell.getBlock())) {
                platformExists = false;
                break;
            }
        }

        if (platformExists) {
            GENERATED_PLATFORMS.add(level.dimension());
            return;
        }

        // 只生成必要的结构（优化性能）
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                // 地板
                level.setBlock(center.offset(x, 0, z), shell, 3);
                // 天花板
                level.setBlock(center.offset(x, 15, z), shell, 3);

                // 墙壁（只在边缘生成）
                if (x == -8 || x == 8 || z == -8 || z == 8) {
                    for (int y = 1; y < 15; y++) {
                        level.setBlock(center.offset(x, y, z), shell, 3);
                    }
                } else {
                    // 内部区域确保是空气
                    for (int y = 1; y < 15; y++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        GENERATED_PLATFORMS.add(level.dimension());
    }

    /**
     * 清理平台缓存（可选）
     */
    public static void clearPlatformCache(ResourceKey<Level> dimension) {
        GENERATED_PLATFORMS.remove(dimension);
    }
}