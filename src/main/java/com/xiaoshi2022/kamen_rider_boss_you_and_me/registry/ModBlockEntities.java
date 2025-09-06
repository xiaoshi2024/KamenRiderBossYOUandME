package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.GiifuSleepingStateBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas.ThroneBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Matrix2dc;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "kamen_rider_boss_you_and_me");

    public static final RegistryObject<BlockEntityType<BananasEntity>> BANANAS_ENTITY =
            BLOCK_ENTITIES.register("bananas_entity",
                    () -> BlockEntityType.Builder.of(BananasEntity::new,
                            ModBlocks.BANANAS_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<LemonxEntity>> LEMONX_ENTITY =
            BLOCK_ENTITIES.register("lemonx_entity",
                    () -> BlockEntityType.Builder.of(LemonxEntity::new,
                            ModBlocks.LEMON_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<melonxEntity>> MELONSX_ENTITY =
            BLOCK_ENTITIES.register("melonx_entity",
                    () -> BlockEntityType.Builder.of(melonxEntity::new,
                            ModBlocks.MELON_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<cherryxEntity>> CHERRYX_ENTITY =
            BLOCK_ENTITIES.register("cherryx_entity",
                    () -> BlockEntityType.Builder.of(cherryxEntity::new,
                            ModBlocks.CHERRYX_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<PeachxEntity>> PEACHX_ENTITY =
            BLOCK_ENTITIES.register("peachx_entity",
                    () -> BlockEntityType.Builder.of(PeachxEntity::new,
                            ModBlocks.PEACHX_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<OrangelsxEntity>> ORANGELSX_ENTITY =
            BLOCK_ENTITIES.register("orangelsx_entity",
                    () -> BlockEntityType.Builder.of(OrangelsxEntity::new,
                            ModBlocks.ORANGELSX_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<DragonfruitBlockEntity>> DRAGONFRUITX_ENTITY =
            BLOCK_ENTITIES.register("dragonfruitx_entity",
                    () -> BlockEntityType.Builder.of(DragonfruitBlockEntity::new,
                            ModBlocks.DRAGONFRUITX_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ThroneBlockEntity>> THRONE_ENTITY =
            BLOCK_ENTITIES.register("throne_entity",
                    () -> BlockEntityType.Builder.of(ThroneBlockEntity::new,
                            ModBlocks.THRONE_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<GiifuSleepingStateBlockEntity>> GIIFU_SLEEPING_STATE_ENTITY =
            BLOCK_ENTITIES.register("giifu_sleeping_state_entity",
                    () -> BlockEntityType.Builder.of(GiifuSleepingStateBlockEntity::new,
                            ModBlocks.GIIFU_SLEEPING_STATE_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<CurseBlockEntity>> CURSE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("curse_block_entity",
                    () -> BlockEntityType.Builder.of(CurseBlockEntity::new,
                            ModBlocks.CURSE_BLOCK.get()).build(null));


}
