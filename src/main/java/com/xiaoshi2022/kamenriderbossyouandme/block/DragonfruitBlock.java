package com.xiaoshi2022.kamenriderbossyouandme.block;

import com.mojang.serialization.MapCodec;
import com.xiaoshi2022.kamenriderbossyouandme.block.client.DragonfruitBlockEntity;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DragonfruitBlock extends BaseEntityBlock {

    // 修复1：正确实现 codec
    public static final MapCodec<DragonfruitBlock> CODEC = simpleCodec(DragonfruitBlock::new);

    public DragonfruitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DragonfruitBlockEntity(pos, state);
    }

    // 修复2：删除 onPlace 和 tick 方法，只使用 getTicker

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.DRAGONFRUITX_ENTITY.get(), DragonfruitBlockEntity::tick);
    }
}