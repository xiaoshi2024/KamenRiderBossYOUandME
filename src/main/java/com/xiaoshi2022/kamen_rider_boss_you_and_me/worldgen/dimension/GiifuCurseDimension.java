package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class GiifuCurseDimension {
    public static final ResourceKey<Level> GIIFU_CURSE_DIM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "giifu_curse")
    );

    // 平台中心坐标
    public static final BlockPos PLATFORM_CENTER = new BlockPos(0, 64, 0);

    /**
     * 生成或确保平台存在
     */
    public static void generatePlatform(ServerLevel level) {
        // 使用持久化数据存储来跟踪生成状态
        DimensionData data = getDimensionData(level);

        // 检查是否已经生成过
        if (data.isPlatformGenerated()) {
            // 验证平台是否实际存在（防止数据损坏或手动删除）
            if (isPlatformActuallyExists(level)) {
                return;
            } else {
                // 平台数据标记为已生成但实际不存在，需要重新生成
                data.setPlatformGenerated(false);
            }
        }

        BlockPos center = PLATFORM_CENTER;
        BlockState shell = ModBlocks.CURSE_BLOCK.get().defaultBlockState();

        // 生成平台结构
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

        // 标记为已生成并保存
        data.setPlatformGenerated(true);
        data.setDirty();
    }

    /**
     * 检查平台是否实际存在
     */
    public static boolean isPlatformActuallyExists(ServerLevel level) {
        BlockPos center = PLATFORM_CENTER;
        BlockState shell = ModBlocks.CURSE_BLOCK.get().defaultBlockState();

        // 检查关键结构点
        BlockPos[] keyCheckPoints = {
                center, // 中心
                center.offset(-8, 0, -8), // 西北角地板
                center.offset(8, 0, 8),   // 东南角地板
                center.offset(-8, 15, -8), // 西北角天花板
                center.offset(8, 15, 8),   // 东南角天花板
                center.offset(-8, 8, -8),  // 西北角墙壁
                center.offset(8, 8, 8)     // 东南角墙壁
        };

        for (BlockPos pos : keyCheckPoints) {
            if (!level.getBlockState(pos).is(shell.getBlock())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查平台是否就绪（可用于其他生成器）
     */
    public static boolean isPlatformReady(ServerLevel level) {
        return isPlatformActuallyExists(level);
    }

    /**
     * 获取维度持久化数据
     */
    static DimensionData getDimensionData(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(DimensionData::load, DimensionData::new, "giifu_dimension_data");
    }

    /**
     * 维度持久化数据类
     */
    public static class DimensionData extends SavedData {
        private boolean platformGenerated = false;
        private boolean chestGenerated = false;

        public boolean isPlatformGenerated() {
            return platformGenerated;
        }

        public void setPlatformGenerated(boolean generated) {
            this.platformGenerated = generated;
            setDirty();
        }

        public boolean isChestGenerated() {
            return chestGenerated;
        }

        public void setChestGenerated(boolean generated) {
            this.chestGenerated = generated;
            setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag compound) {
            compound.putBoolean("platformGenerated", platformGenerated);
            compound.putBoolean("chestGenerated", chestGenerated);
            return compound;
        }

        public static DimensionData load(CompoundTag compound) {
            DimensionData data = new DimensionData();
            data.platformGenerated = compound.getBoolean("platformGenerated");
            data.chestGenerated = compound.getBoolean("chestGenerated");
            return data;
        }

        public DimensionData() {
            // 默认构造函数
        }
    }

    /**
     * 清理平台数据（用于调试或重置）
     */
    public static void clearPlatformData(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        storage.set("giifu_dimension_data", new DimensionData());
    }

    /**
     * 强制重新生成平台（用于调试）
     */
    public static void forceRegeneratePlatform(ServerLevel level) {
        clearPlatformData(level);
        generatePlatform(level);
    }
}