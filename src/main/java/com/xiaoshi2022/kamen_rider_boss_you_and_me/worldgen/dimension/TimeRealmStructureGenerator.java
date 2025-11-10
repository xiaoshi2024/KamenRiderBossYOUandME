package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TimeRealmStructureGenerator {
    
    /**
     * 生成时间堡垒遗迹
     * 这是时间领域中的主要特殊结构，会生成时间王族实体
     */
    public static void generateTimeFortress(ServerLevel level, BlockPos centerPos) {
        // 查找地面高度，确保结构在地面上生成
        int groundY = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                centerPos
        ).getY();
        
        BlockPos structurePos = new BlockPos(centerPos.getX(), groundY, centerPos.getZ());
        
        // 生成堡垒主体结构
        generateFortressMainStructure(level, structurePos);
        
        // 生成周围的墙壁和塔楼
        generateFortressWalls(level, structurePos);
        
        // 生成内部房间和走廊
        generateFortressInterior(level, structurePos);
        
        // 在堡垒中放置宝箱和特殊方块
        placeTreasuresAndFeatures(level, structurePos);
        
        // 记录堡垒位置到维度数据中
        recordFortressPosition(level, structurePos);
    }
    
    /**
     * 生成堡垒主体结构
     */
    private static void generateFortressMainStructure(ServerLevel level, BlockPos pos) {
        // 使用黑石和抛光黑石作为堡垒的主要建筑材料
        BlockState mainBlock = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState accentBlock = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState windowBlock = Blocks.GLOWSTONE.defaultBlockState();
        
        // 生成主体建筑（15x15的底座，高度为8）
        for (int y = 0; y < 8; y++) {
            for (int x = -7; x <= 7; x++) {
                for (int z = -7; z <= 7; z++) {
                    BlockPos currentPos = pos.offset(x, y, z);
                    
                    // 外层墙壁使用不同的方块
                    if (y == 0 || Math.abs(x) == 7 || Math.abs(z) == 7) {
                        level.setBlock(currentPos, accentBlock, 3);
                    } else {
                        // 内部填充
                        level.setBlock(currentPos, mainBlock, 3);
                    }
                    
                    // 在第二层和第六层放置窗户
                    if ((y == 2 || y == 6) && 
                        (Math.abs(x) == 5 && Math.abs(z) <= 1) || 
                        (Math.abs(z) == 5 && Math.abs(x) <= 1)) {
                        level.setBlock(currentPos, windowBlock, 3);
                    }
                }
            }
        }
        
        // 生成屋顶
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                BlockPos currentPos = pos.offset(x, 8, z);
                level.setBlock(currentPos, accentBlock, 3);
            }
        }
    }
    
    /**
     * 生成堡垒周围的墙壁和塔楼
     */
    private static void generateFortressWalls(ServerLevel level, BlockPos pos) {
        BlockState wallBlock = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState towerBlock = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        BlockState wallTopBlock = Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState();
        
        // 生成四个角的塔楼（高度为12）
        int[] offsets = {-15, 15};
        for (int xOffset : offsets) {
            for (int zOffset : offsets) {
                BlockPos towerBase = pos.offset(xOffset, 0, zOffset);
                
                // 生成3x3的塔楼
                for (int y = 0; y < 12; y++) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            level.setBlock(towerBase.offset(x, y, z), towerBlock, 3);
                        }
                    }
                }
                
                // 塔顶装饰
                level.setBlock(towerBase.offset(0, 12, 0), Blocks.LIGHTNING_ROD.defaultBlockState(), 3);
            }
        }
        
        // 生成连接塔楼的围墙
        generateWall(level, pos.offset(-15, 0, -15), pos.offset(15, 0, -15));
        generateWall(level, pos.offset(15, 0, -15), pos.offset(15, 0, 15));
        generateWall(level, pos.offset(15, 0, 15), pos.offset(-15, 0, 15));
        generateWall(level, pos.offset(-15, 0, 15), pos.offset(-15, 0, -15));
    }
    
    /**
     * 生成一段围墙
     */
    private static void generateWall(ServerLevel level, BlockPos start, BlockPos end) {
        BlockState wallBlock = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState wallTopBlock = Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState();
        
        int distance = (int) start.distSqr(end);
        for (int i = 0; i <= distance; i++) {
            double progress = (double) i / distance;
            int x = (int) (start.getX() + (end.getX() - start.getX()) * progress);
            int z = (int) (start.getZ() + (end.getZ() - start.getZ()) * progress);
            
            // 找到地面高度
            int groundY = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                    new BlockPos(x, 0, z)
            ).getY();
            
            // 生成围墙柱
            for (int y = 0; y < 3; y++) {
                level.setBlock(new BlockPos(x, groundY + y, z), wallBlock, 3);
            }
            
            // 围墙顶部
            level.setBlock(new BlockPos(x, groundY + 3, z), wallTopBlock, 3);
        }
    }
    
    /**
     * 生成堡垒内部结构
     */
    private static void generateFortressInterior(ServerLevel level, BlockPos pos) {
        // 清空内部空间
        for (int y = 1; y < 7; y++) {
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    if (Math.abs(x) < 5 || Math.abs(z) < 5) {
                        level.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        
        // 生成地板（使用不同的方块标记区域）
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                if (Math.abs(x) <= 6 && Math.abs(z) <= 6) {
                    BlockState floorBlock;
                    if (x*x + z*z <= 4*4) {
                        // 中央区域
                        floorBlock = Blocks.NETHERITE_BLOCK.defaultBlockState();
                    } else if (x*x + z*z <= 6*6) {
                        // 周围区域
                        floorBlock = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(); // 使用现有的方块
                    } else {
                        floorBlock = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
                    }
                    level.setBlock(pos.offset(x, 0, z), floorBlock, 3);
                }
            }
        }

        // 生成柱子支撑
        for (int x : new int[]{-6, 6}) {
            for (int z : new int[]{-6, 6}) {
                for (int y = 1; y < 7; y++) {
                    level.setBlock(pos.offset(x, y, z), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 3); // 使用现有的方块
                }
            }
        }
    }
    
    /**
     * 在堡垒中放置宝箱和特殊方块
     */
    private static void placeTreasuresAndFeatures(ServerLevel level, BlockPos pos) {
        // 在中央放置时间祭坛
        level.setBlock(pos.offset(0, 1, 0), Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);
        
        // 祭坛周围放置符文
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2;
            int x = (int) Math.round(3 * Math.cos(angle));
            int z = (int) Math.round(3 * Math.sin(angle));
            level.setBlock(pos.offset(x, 1, z), Blocks.END_STONE.defaultBlockState(), 3);
        }
        
        // 在角落放置宝箱（这里使用钻石块作为占位符，实际游戏中应替换为宝箱）
        for (int x : new int[]{-4, 4}) {
            for (int z : new int[]{-4, 4}) {
                level.setBlock(pos.offset(x, 1, z), Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
            }
        }
    }
    
    /**
     * 记录堡垒位置到维度数据中
     */
    private static void recordFortressPosition(ServerLevel level, BlockPos pos) {
        // 获取维度数据
        TimeRealmDimension.DimensionData data = TimeRealmDimension.getDimensionData(level);
        
        // 生成唯一ID
        String id = "fortress_" + pos.getX() + "_" + pos.getZ();
        
        // 记录位置
        data.addFortressPosition(id, pos);
    }
    
    /**
     * 在世界中随机位置生成时间堡垒
     * 这个方法会被世界生成器或tick处理器调用
     */
    public static void generateRandomFortress(ServerLevel level, RandomSource random) {
        // 生成随机坐标，确保与已有堡垒有足够距离
        int x, z;
        boolean validPosition = false;
        BlockPos fortressPos = null;
        
        // 尝试最多10次找到合适位置
        for (int attempt = 0; attempt < 10; attempt++) {
            x = random.nextInt(40000) - 20000;
            z = random.nextInt(40000) - 20000;
            
            // 检查是否与已有堡垒距离过近
            fortressPos = new BlockPos(x, 0, z);
            if (isValidFortressPosition(level, fortressPos)) {
                validPosition = true;
                break;
            }
        }
        
        // 如果找到合适位置，生成堡垒
        if (validPosition && fortressPos != null) {
            generateTimeFortress(level, fortressPos);
        }
    }
    
    /**
     * 检查位置是否适合生成堡垒
     */
    private static boolean isValidFortressPosition(ServerLevel level, BlockPos pos) {
        // 获取已生成的堡垒位置
        TimeRealmDimension.DimensionData data = TimeRealmDimension.getDimensionData(level);
        CompoundTag fortressPositions = data.getFortressPositions();
        
        // 检查与已有堡垒的距离
        for (String key : fortressPositions.getAllKeys()) {
            CompoundTag posTag = fortressPositions.getCompound(key);
            int fx = posTag.getInt("x");
            int fz = posTag.getInt("z");
            
            // 如果距离小于500，则不生成
            double distance = Math.sqrt((pos.getX() - fx) * (pos.getX() - fx) + (pos.getZ() - fz) * (pos.getZ() - fz));
            if (distance < 500) {
                return false;
            }
        }
        
        // 检查地形是否适合
        int y = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                pos
        ).getY();
        
        // 确保地形不太陡峭（检查周围几个点的高度）
        int heightVariance = 0;
        int baseY = y;
        
        for (int dx = -5; dx <= 5; dx += 5) {
            for (int dz = -5; dz <= 5; dz += 5) {
                int currentY = level.getHeightmapPos(
                        net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                        pos.offset(dx, 0, dz)
                ).getY();
                heightVariance = Math.max(heightVariance, Math.abs(currentY - baseY));
            }
        }
        
        // 如果高度变化超过10，则认为地形太陡峭
        return heightVariance <= 10;
    }
    
    /**
     * 初始化世界结构生成
     * 在世界加载时调用此方法
     */
    public static void initializeStructureGeneration(ServerLevel level) {
        // 获取维度数据
        TimeRealmDimension.DimensionData data = TimeRealmDimension.getDimensionData(level);
        
        // 如果还没有生成过结构，生成初始的几个时间堡垒
        if (!data.areStructuresGenerated()) {
            RandomSource random = level.getRandom();
            
            // 生成3-5个初始堡垒
            int fortressCount = 3 + random.nextInt(3);
            for (int i = 0; i < fortressCount; i++) {
                generateRandomFortress(level, random);
            }
            
            // 标记结构已生成
            data.setStructuresGenerated(true);
            data.setDirty();
        }
    }
}