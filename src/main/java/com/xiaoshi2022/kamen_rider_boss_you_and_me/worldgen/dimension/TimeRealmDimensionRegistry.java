package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.OptionalLong;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class TimeRealmDimensionRegistry {

    // 维度资源键
    public static final ResourceKey<Level> TIME_REALM_DIMENSION = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(MODID, "time_realm"));
    public static final ResourceKey<LevelStem> TIME_REALM_STEM = ResourceKey.create(Registries.LEVEL_STEM, new ResourceLocation(MODID, "time_realm"));

    // 维度类型资源键
    public static final ResourceKey<DimensionType> TIME_REALM_DIM_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE, new ResourceLocation(MODID, "time_realm_type")
    );

    /**
     * 在引导上下文中注册时间领域维度类型
     */
    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(
                TIME_REALM_DIM_TYPE,
                new DimensionType(
                        OptionalLong.empty(),  // 固定时间，null表示正常时间流动
                        true,                  // 是否自然生成
                        false,                 // 是否为下界（ultrawarm）
                        false,                 // 是否有天花板
                        true,                  // 是否有天空光
                        1.0,                   // 坐标缩放
                        false,                 // 床是否可用
                        false,                 // 重生锚是否可用
                        -64,                   // 最小Y值
                        384,                   // 高度
                        384,                   // 逻辑高度
                        BlockTags.INFINIBURN_OVERWORLD, // 无限燃烧方块
                        new ResourceLocation(MODID, "time_realm"), // 维度效果
                        0.0f,                  // 环境光照
                        new DimensionType.MonsterSettings(
                                false,                // 是否有袭击
                                true,                 // 是否为和平模式下的特殊怪物生成规则
                                UniformInt.of(0, 7),  // 怪物生成方块光照限制
                                0                     // 其他设置
                        )
                )
        );
    }

    /**
     * 在引导上下文中注册时间领域维度
     */
    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        // 获取噪声生成设置和生物群系的引用
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimensionTypeRegistry = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseSettingsRegistry = context.lookup(Registries.NOISE_SETTINGS);

        // 创建自定义生物群系提供者
        TimeRealmBiomeProvider biomeProvider = new TimeRealmBiomeProvider(biomeRegistry, NoiseGeneratorSettings.OVERWORLD);

        // 创建基于噪声的区块生成器
        net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator chunkGenerator = new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(
                biomeProvider,
                noiseSettingsRegistry.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
        );

        // 注册时间领域维度
        context.register(
                TIME_REALM_STEM,
                new LevelStem(
                        dimensionTypeRegistry.getOrThrow(TIME_REALM_DIM_TYPE),
                        chunkGenerator
                )
        );
    }

    /**
     * 注册维度相关的所有内容
     * 这个方法会在Mod主类或相关的注册事件中被调用
     */
    public static void registerDimensions(MinecraftServer server) {
        // 维度的实际注册通常通过数据包或数据生成完成
        // 这里保留此方法作为API入口点
    }
}