package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class TimeRealmDimension {
    public static final ResourceKey<Level> TIME_REALM_DIM = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "time_realm")
    );

    // 玩家进入维度时的初始坐标
    public static final BlockPos SPAWN_POS = new BlockPos(0, 70, 0);

    /**
     * 初始化时间领域维度，确保生成条件正确设置
     * 这个维度是类似主世界的另类地球，保留主世界的地形生成特性
     */
    public static void initializeDimension(ServerLevel level) {
        // 使用持久化数据存储来跟踪生成状态
        DimensionData data = getDimensionData(level);

        // 确保初始出生点安全
        ensureSafeSpawnPoint(level);

        // 确保已标记为初始化完成
        if (!data.isDimensionInitialized()) {
            data.setDimensionInitialized(true);
            data.setDirty();
        }
    }

    /**
     * 确保出生点安全，避免玩家生成在危险位置
     */
    private static void ensureSafeSpawnPoint(ServerLevel level) {
        BlockPos spawnPos = SPAWN_POS;
        
        // 确保出生点上方有足够的空气空间
        for (int y = 0; y < 5; y++) {
            level.setBlock(spawnPos.offset(0, y, 0), Blocks.AIR.defaultBlockState(), 3);
        }
        
        // 确保出生点下方有固体方块
        BlockPos groundPos = spawnPos.below();
        if (level.getBlockState(groundPos).isAir()) {
            level.setBlock(groundPos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        }
    }

    /**
     * 生成时间领域专属遗迹
     * 调用结构生成器生成时间堡垒遗迹
     */
    public static void generateTimeFortress(ServerLevel level, BlockPos centerPos) {
        // 调用结构生成器生成时间堡垒
        TimeRealmStructureGenerator.generateTimeFortress(level, centerPos);
    }

    /**
     * 检查维度是否已初始化
     */
    public static boolean isDimensionInitialized(ServerLevel level) {
        DimensionData data = getDimensionData(level);
        return data.isDimensionInitialized();
    }
    
    /**
     * 检查玩家是否可以安全地生成在这个维度
     */
    public static boolean isSpawnSafe(ServerLevel level) {
        BlockPos spawnPos = SPAWN_POS;
        // 检查出生点地面是否为固体方块
        return !level.getBlockState(spawnPos.below()).isAir() && 
               level.getBlockState(spawnPos).isAir() &&
               level.getBlockState(spawnPos.above()).isAir();
    }

    /**
     * 获取维度持久化数据
     */
    static DimensionData getDimensionData(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(DimensionData::load, DimensionData::new, "time_realm_dimension_data");
    }

    /**
     * 维度持久化数据类
     * 存储维度初始化状态和遗迹生成信息
     */
    public static class DimensionData extends SavedData {
        private boolean dimensionInitialized = false;
        private boolean structuresGenerated = false;
        
        // 记录已生成的时间堡垒位置，避免重复生成
        private CompoundTag fortressPositions = new CompoundTag();

        public boolean isDimensionInitialized() {
            return dimensionInitialized;
        }

        public void setDimensionInitialized(boolean initialized) {
            this.dimensionInitialized = initialized;
            setDirty();
        }

        public boolean areStructuresGenerated() {
            return structuresGenerated;
        }

        public void setStructuresGenerated(boolean generated) {
            this.structuresGenerated = generated;
            setDirty();
        }
        
        public CompoundTag getFortressPositions() {
            return fortressPositions;
        }
        
        public void addFortressPosition(String id, BlockPos pos) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            fortressPositions.put(id, posTag);
            setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag compound) {
            compound.putBoolean("dimensionInitialized", dimensionInitialized);
            compound.putBoolean("structuresGenerated", structuresGenerated);
            compound.put("fortressPositions", fortressPositions);
            return compound;
        }

        public static DimensionData load(CompoundTag compound) {
            DimensionData data = new DimensionData();
            data.dimensionInitialized = compound.getBoolean("dimensionInitialized");
            data.structuresGenerated = compound.getBoolean("structuresGenerated");
            if (compound.contains("fortressPositions")) {
                data.fortressPositions = compound.getCompound("fortressPositions");
            }
            return data;
        }

        public DimensionData() {
            // 默认构造函数
        }
    }

    /**
     * 重置维度数据（用于调试或重置）
     */
    public static void resetDimensionData(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        storage.set("time_realm_dimension_data", new DimensionData());
    }

    /**
     * 在世界中随机位置生成时间堡垒
     * 调用结构生成器在随机位置生成时间堡垒
     */
    public static void generateRandomTimeFortress(ServerLevel level, RandomSource random) {
        // 调用结构生成器生成随机时间堡垒
        TimeRealmStructureGenerator.generateRandomFortress(level, random);
    }
    
    /**
     * 初始化世界结构生成
     * 在世界加载时调用此方法，生成初始的时间堡垒
     */
    public static void initializeStructureGeneration(ServerLevel level) {
        TimeRealmStructureGenerator.initializeStructureGeneration(level);
    }
    
    /**
     * 获取适合玩家生成的安全位置
     * @return 安全的生成位置
     */
    public static BlockPos getSafeSpawnPosition(ServerLevel level) {
        // 首先尝试使用默认出生点
        if (isSpawnSafe(level)) {
            return SPAWN_POS.immutable();
        }
        
        // 如果默认出生点不安全，查找附近的安全位置
        for (int radius = 1; radius <= 100; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x*x + z*z <= radius*radius) {
                        BlockPos pos = SPAWN_POS.offset(x, 0, z);
                        int surfaceY = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos).getY();
                        BlockPos checkPos = new BlockPos(pos.getX(), surfaceY, pos.getZ());
                        
                        if (!level.getBlockState(checkPos.below()).isAir() && 
                            level.getBlockState(checkPos).isAir() &&
                            level.getBlockState(checkPos.above()).isAir()) {
                            return checkPos.immutable();
                        }
                    }
                }
            }
        }
        
        // 如果找不到安全位置，返回默认位置
        return SPAWN_POS.immutable();
    }
}