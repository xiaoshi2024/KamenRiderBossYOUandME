package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.giifu.GiifuSleepingStateBlock;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.ThroneBlock;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.BananasBlock;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.Lemonx;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.cherryx;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.melonx;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.Peachx;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.orangelsx;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.DragonfruitBlock;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.CurseBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.loading.FMLPaths;
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
    public static final RegistryObject<Block> MELON_BLOCK = BLOCKS.register("melonx",
            () -> new melonx(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));
    public static final RegistryObject<Block> CHERRYX_BLOCK = BLOCKS.register("cherryx",
            () -> new cherryx(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> PEACHX_BLOCK = BLOCKS.register("peachx",
            () -> new Peachx(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> ORANGELSX_BLOCK = BLOCKS.register("orangelsx",
            () -> new orangelsx(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> DRAGONFRUITX_BLOCK = BLOCKS.register("dragonfruitx_block",
            () -> new com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals.DragonfruitBlock(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> THRONE_BLOCK = BLOCKS.register("throne_block",
            () -> new ThroneBlock(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    public static final RegistryObject<Block> GIIFU_SLEEPING_STATE_BLOCK = BLOCKS.register("giifu_sleeping_state",
            () -> new GiifuSleepingStateBlock(Block.Properties.of()
                    .strength(2.0f)
                    .noOcclusion()
                    .lightLevel(state -> 15)));

    // Curse Block - 普通原版方块
    public static final RegistryObject<Block> CURSE_BLOCK = BLOCKS.register("curse_block",
            () -> new CurseBlock(Block.Properties.of()
                    .strength(3.0f, 6.0f) // 硬度3.0，抗爆性6.0
                    .requiresCorrectToolForDrops() // 需要正确工具挖掘
                    .lightLevel(state -> 7) // 发光等级7
                    .friction(0.6f) // 摩擦系数
                    .destroyTime(1.5f))); // 破坏时间
}
