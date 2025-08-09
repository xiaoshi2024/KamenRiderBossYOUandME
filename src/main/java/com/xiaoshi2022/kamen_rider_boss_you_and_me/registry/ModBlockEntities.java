package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.BananasEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.LemonxEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
}
