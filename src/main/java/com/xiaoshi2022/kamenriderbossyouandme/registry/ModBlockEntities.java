package com.xiaoshi2022.kamenriderbossyouandme.registry;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.block.client.DragonfruitBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    // 修复：使用 create() 方法
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, KamenRiderBossYOUandME.MODID);

    public static final Supplier<BlockEntityType<DragonfruitBlockEntity>> DRAGONFRUITX_ENTITY =
            BLOCK_ENTITIES.register("dragonfruitx_entity",
                    () -> BlockEntityType.Builder.of(
                            DragonfruitBlockEntity::new,
                            ModBlocks.DRAGONFRUITX_BLOCK.get()
                    ).build(null)
            );
}