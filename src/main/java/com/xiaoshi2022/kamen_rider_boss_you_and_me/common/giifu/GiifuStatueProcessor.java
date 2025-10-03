package com.xiaoshi2022.kamen_rider_boss_you_and_me.common.giifu;

import com.mojang.serialization.Codec;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class GiifuStatueProcessor extends StructureProcessor {

    public static final GiifuStatueProcessor INSTANCE = new GiifuStatueProcessor();
    public static final Codec<GiifuStatueProcessor> CODEC = Codec.unit(INSTANCE);

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo process(
            LevelReader level,
            BlockPos offset,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo rawBlockInfo,
            StructureTemplate.StructureBlockInfo blockInfo,
            StructurePlaceSettings settings,
            @Nullable StructureTemplate template) {

        if (blockInfo.state().is(Blocks.OAK_PLANKS)) {
            RandomSource rand = settings.getRandom(blockInfo.pos());
            if (rand.nextFloat() < 0.08f) {
                return new StructureTemplate.StructureBlockInfo(
                        blockInfo.pos(),
                        ModBlocks.GIIFU_SLEEPING_STATE_BLOCK.get().defaultBlockState(),
                        null);
            }
        }
        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessors.GIIFU_STATUE.get();
    }
}