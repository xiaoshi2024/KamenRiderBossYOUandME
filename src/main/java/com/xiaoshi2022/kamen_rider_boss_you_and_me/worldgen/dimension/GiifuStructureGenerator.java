package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GiifuStructureGenerator {

    private static final ResourceLocation CHEST_STRUCTURE =
            new ResourceLocation("kamen_rider_boss_you_and_me", "giifu_loot/giifu_chest");

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {

                // 使用持久化数据检查是否已经生成
                GiifuCurseDimension.DimensionData data = GiifuCurseDimension.getDimensionData(serverLevel);
                if (data.isChestGenerated()) {
                    return;
                }

                // 先确保平台生成
                GiifuCurseDimension.generatePlatform(serverLevel);

                // 延迟生成宝箱，确保平台已生成完成
                serverLevel.getServer().execute(() -> {
                    // 再次检查，防止竞态条件
                    GiifuCurseDimension.DimensionData delayedData = GiifuCurseDimension.getDimensionData(serverLevel);
                    if (!delayedData.isChestGenerated()) {
                        generateLootChest(serverLevel);
                    }
                });
            }
        }
    }

    public static void generateLootChest(ServerLevel level) {
        // 使用持久化数据检查是否已经生成
        GiifuCurseDimension.DimensionData data = GiifuCurseDimension.getDimensionData(level);
        if (data.isChestGenerated()) {
            return;
        }

        // 检查平台是否就绪
        if (!GiifuCurseDimension.isPlatformReady(level)) {
            // 平台未就绪，稍后重试
            level.getServer().execute(() -> generateLootChest(level));
            return;
        }

        // 检查宝箱是否已经存在（防止服务器重启后重复生成）
        if (isChestAlreadyExists(level)) {
            System.out.println("Chest already exists in dimension: " + level.dimension());
            data.setChestGenerated(true);
            data.setDirty();
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

            // 标记为已生成并保存
            data.setChestGenerated(true);
            data.setDirty();

            // 调试信息
            System.out.println("Generated giifu chest at: " + chestPos + " in dimension: " + level.dimension());
        } else {
            System.out.println("Failed to load structure: " + CHEST_STRUCTURE);
            // 结构加载失败，可以尝试重新生成或记录错误
        }
    }

    // 检查宝箱是否已经存在
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

    // 手动触发生成（用于调试或其他需要时）
    public static void forceGenerateChest(ServerLevel level) {
        if (level.dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {
            // 清除生成状态并重新生成
            GiifuCurseDimension.DimensionData data = GiifuCurseDimension.getDimensionData(level);
            data.setChestGenerated(false);
            data.setDirty();
            generateLootChest(level);
        }
    }

    // 清理宝箱数据（用于调试）
    public static void clearChestData(ServerLevel level) {
        if (level.dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {
            GiifuCurseDimension.DimensionData data = GiifuCurseDimension.getDimensionData(level);
            data.setChestGenerated(false);
            data.setDirty();
        }
    }

    // 检查是否已经生成
    public static boolean hasChestGenerated(ServerLevel level) {
        if (level.dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {
            GiifuCurseDimension.DimensionData data = GiifuCurseDimension.getDimensionData(level);
            return data.isChestGenerated();
        }
        return false;
    }
}