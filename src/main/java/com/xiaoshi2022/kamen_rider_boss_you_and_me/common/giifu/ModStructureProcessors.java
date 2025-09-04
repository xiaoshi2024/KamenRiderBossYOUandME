package com.xiaoshi2022.kamen_rider_boss_you_and_me.common.giifu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me.MODID;

public class ModStructureProcessors {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MODID);

    public static final RegistryObject<StructureProcessorType<GiifuStatueProcessor>> GIIFU_STATUE =
            PROCESSORS.register("giifu_statue", () -> () -> GiifuStatueProcessor.CODEC);
}