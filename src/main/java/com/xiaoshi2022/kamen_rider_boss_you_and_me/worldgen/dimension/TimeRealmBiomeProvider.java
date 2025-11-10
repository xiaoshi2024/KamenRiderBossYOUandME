package com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * 时间领域维度的生物群系提供者
 * 提供类似主世界的生物群系分布，但具有时间领域的特色
 */
public class TimeRealmBiomeProvider extends BiomeSource {
    private final HolderGetter<Biome> biomeRegistry;
    private final Holder<Biome> plainsBiome;

    public TimeRealmBiomeProvider(HolderGetter<Biome> biomeRegistry, ResourceKey<NoiseGeneratorSettings> settings) {
        super();
        this.biomeRegistry = biomeRegistry;
        this.plainsBiome = biomeRegistry.getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
    }

    private static List<Holder<Biome>> getBiomes(HolderGetter<Biome> biomeRegistry) {
        return Collections.singletonList(biomeRegistry.getOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS));
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return null;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.empty();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        // 这里可以自定义时间领域的生物群系分布
        // 目前使用主世界的基本生物群系，但可以根据需要修改
        return plainsBiome;
    }
}