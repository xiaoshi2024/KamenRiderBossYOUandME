package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class GiifuStructureGenerator {

    private static final ResourceLocation CHEST_STRUCTURE =
            new ResourceLocation("kamen_rider_boss_you_and_me", "giifu_loot/giifu_chest");

    // 使用更持久的存储来跟踪已生成的结构
    private static final Set<ResourceKey<Level>> GENERATED_CHESTS = new HashSet<>();
    private static final Set<ResourceKey<Level>> PENDING_GENERATION = new HashSet<>();

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {

                // 检查是否已经生成过或者正在生成
                if (GENERATED_CHESTS.contains(serverLevel.dimension()) ||
                        PENDING_GENERATION.contains(serverLevel.dimension())) {
                    return;
                }

                // 标记为正在生成，防止重复触发
                PENDING_GENERATION.add(serverLevel.dimension());

                // 先确保平台生成
                GiifuCurseDimension.generatePlatform(serverLevel);

                // 延迟一tick生成宝箱，确保平台已生成完成
                serverLevel.getServer().execute(() -> {
                    try {
                        generateLootChest(serverLevel);
                        GENERATED_CHESTS.add(serverLevel.dimension());
                    } finally {
                        PENDING_GENERATION.remove(serverLevel.dimension());
                    }
                });
            }
        }
    }

    public static void generateLootChest(ServerLevel level) {
        // 再次检查是否已经生成（防止竞态条件）
        if (GENERATED_CHESTS.contains(level.dimension())) {
            return;
        }

        // 检查宝箱是否已经存在（防止服务器重启后重复生成）
        if (isChestAlreadyExists(level)) {
            System.out.println("Chest already exists in dimension: " + level.dimension());
            GENERATED_CHESTS.add(level.dimension());
            return;
        }

        // 在平台内随机位置生成宝箱
        RandomSource random = level.getRandom();
        int x = GiifuCurseDimension.PLATFORM_CENTER.getX() + random.nextInt(10) - 5;
        int z = GiifuCurseDimension.PLATFORM_CENTER.getZ() + random.nextInt(10) - 5;
        int y = GiifuCurseDimension.PLATFORM_CENTER.getY() + 2; // 平台上方

        BlockPos chestPos = new BlockPos(x, y, z);

        // 检查该位置是否适合放置（必须是空气或可替换方块）
        if (!level.getBlockState(chestPos).isAir() &&
                !level.getBlockState(chestPos).canBeReplaced()) {
            // 如果位置被占用，尝试在周围寻找合适位置
            for (int i = 0; i < 10; i++) {
                BlockPos newPos = chestPos.offset(
                        random.nextInt(5) - 2,
                        0,
                        random.nextInt(5) - 2
                );
                if (level.getBlockState(newPos).isAir() ||
                        level.getBlockState(newPos).canBeReplaced()) {
                    chestPos = newPos;
                    break;
                }
            }
        }

        // 直接加载并放置NBT结构
        StructureTemplate template = level.getStructureManager().get(CHEST_STRUCTURE).orElse(null);
        if (template != null) {
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setIgnoreEntities(false);

            template.placeInWorld(level, chestPos, chestPos, settings, random, 2);

            // 调试信息
            System.out.println("Generated giifu chest at: " + chestPos + " in dimension: " + level.dimension());
        } else {
            System.out.println("Failed to load structure: " + CHEST_STRUCTURE);
        }
    }

    // 新增：检查宝箱是否已经存在
    private static boolean isChestAlreadyExists(ServerLevel level) {
        // 在平台范围内搜索箱子
        BlockPos center = GiifuCurseDimension.PLATFORM_CENTER;
        int searchRadius = 15;

        for (int x = center.getX() - searchRadius; x <= center.getX() + searchRadius; x++) {
            for (int z = center.getZ() - searchRadius; z <= center.getZ() + searchRadius; z++) {
                for (int y = center.getY() + 1; y <= center.getY() + 10; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).getBlock() == net.minecraft.world.level.block.Blocks.CHEST) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 新增：手动触发生成（用于调试或其他需要时）
    public static void forceGenerateChest(ServerLevel level) {
        if (level.dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {
            GENERATED_CHESTS.remove(level.dimension());
            generateLootChest(level);
        }
    }

    public static void clearChestCache(ResourceKey<Level> dimension) {
        GENERATED_CHESTS.remove(dimension);
        PENDING_GENERATION.remove(dimension);
    }

    // 新增：检查是否已经生成
    public static boolean hasChestGenerated(ResourceKey<Level> dimension) {
        return GENERATED_CHESTS.contains(dimension);
    }
}