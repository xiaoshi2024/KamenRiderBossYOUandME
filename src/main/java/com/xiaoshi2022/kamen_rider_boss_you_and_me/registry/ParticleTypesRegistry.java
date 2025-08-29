package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleTypesRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "kamen_rider_boss_you_and_me");

    public static final RegistryObject<SimpleParticleType> LEMONSLICE =
            PARTICLE_TYPES.register("lemonslice",
                    () -> new SimpleParticleType(false));   // false = 不需要额外数据
    public static final RegistryObject<SimpleParticleType> MLONSLICE =
            PARTICLE_TYPES.register("melonslice",
                    () -> new SimpleParticleType(false));   // false = 不需要额外数据

    public static final RegistryObject<SimpleParticleType> CHERRYSLICE =
            PARTICLE_TYPES.register("cherryslice",
                    () -> new SimpleParticleType(false));   // false = 不需要额外数据

    public static final RegistryObject<SimpleParticleType> PEACHSLICE =
            PARTICLE_TYPES.register("peachslice",
                    () -> new SimpleParticleType(false));   // false = 不需要额外数据
    public static final RegistryObject<SimpleParticleType> DRAGONLICE =
            PARTICLE_TYPES.register("dragonlice",
                    () -> new SimpleParticleType(false));   // false = 不需要额外数据
    
    // 新增：黑暗蝙蝠粒子
    public static final RegistryObject<SimpleParticleType> DARK_BAT =
            PARTICLE_TYPES.register("dark_bat",
                    () -> new SimpleParticleType(false));   // false = 不需要额外数据
}
