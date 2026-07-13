package com.xiaoshi2022.kamenriderbossyouandme.registry;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.block.DragonfruitBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(KamenRiderBossYOUandME.MODID);

    public static final DeferredBlock<Block> DRAGONFRUITX_BLOCK = BLOCKS.register("dragonfruitx_block",
            () -> new DragonfruitBlock(Block.Properties.of().noCollission()));
}