package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CurseBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos previousCurseBlockPos; // 记录上一次放置的CURSE_BLOCK位置

    public CurseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CURSE_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public BlockPos getPreviousCurseBlockPos() {
        return previousCurseBlockPos;
    }

    public void setPreviousCurseBlockPos(@Nullable BlockPos previousCurseBlockPos) {
        this.previousCurseBlockPos = previousCurseBlockPos;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (previousCurseBlockPos != null) {
            tag.putIntArray("PreviousCurseBlockPos",
                    new int[]{previousCurseBlockPos.getX(), previousCurseBlockPos.getY(), previousCurseBlockPos.getZ()});
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("PreviousCurseBlockPos")) {
            int[] posArray = tag.getIntArray("PreviousCurseBlockPos");
            if (posArray.length == 3) {
                previousCurseBlockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
            }
        }
    }
}