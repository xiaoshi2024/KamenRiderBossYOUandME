package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.GiifuSleepingStateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.minecraft.world.item.Items.GOLDEN_SWORD;

public class GiifuSleepingStateBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public GiifuSleepingStateBlock(BlockBehaviour.Properties properties) {
        super(properties
                .noOcclusion() // 不遮挡光线
                .strength(3.0f)); // 假设基夫的石化形态更坚固
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // 设置碰撞箱
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GiifuSleepingStateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack itemStack = player.getItemInHand(hand);

        // 如果玩家拿着黄金剑，尝试抽取石化心脏
        if (itemStack.getItem() == GOLDEN_SWORD) {
            // 检查是否已经抽取过心脏
            if (hasHeartExtracted(level, pos)) {
                player.sendSystemMessage(Component.translatable("message.giifu.heart_already_extracted"));
                return InteractionResult.FAIL;
            }

            // 抽取心脏
            if (extractHeart(level, pos)) {
                // 播放音效和粒子效果


                spawnParticles(level, pos);
                player.sendSystemMessage(Component.translatable("message.giifu.heart_extracted"));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    private boolean hasHeartExtracted(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof GiifuSleepingStateBlockEntity) {
            return ((GiifuSleepingStateBlockEntity) blockEntity).hasHeartExtracted();
        }
        return false;
    }

    private boolean extractHeart(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof GiifuSleepingStateBlockEntity) {
            return ((GiifuSleepingStateBlockEntity) blockEntity).extractHeart();
        }
        return false;
    }

    private void spawnParticles(Level level, BlockPos pos) {
        if (level.isClientSide) {
            for (int i = 0; i < 50; i++) {
                double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 3;
                double y = pos.getY() + 1 + level.random.nextDouble() * 2;
                double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 3;

                level.addParticle(ParticleTypes.FLASH, x, y, z, 0, 0, 0);
                level.addParticle(ParticleTypes.SOUL, x, y, z,
                        (level.random.nextDouble() - 0.5) * 0.1,
                        level.random.nextDouble() * 0.1,
                        (level.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 如果不需要每 tick 都处理逻辑，可以移除这个方法
        level.scheduleTick(pos, this, 1);
    }
}