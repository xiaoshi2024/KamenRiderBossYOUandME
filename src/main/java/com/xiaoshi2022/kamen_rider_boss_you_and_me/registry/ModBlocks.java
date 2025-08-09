package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.BananasBlock;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.Lemonx;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Matrix2dc;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "kamen_rider_boss_you_and_me");

    public static final RegistryObject<Block> BANANAS_BLOCK = BLOCKS.register("bananas_block",
            () -> new BananasBlock(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> LEMON_BLOCK = BLOCKS.register("lemonx",
            () -> new Lemonx(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));
}
