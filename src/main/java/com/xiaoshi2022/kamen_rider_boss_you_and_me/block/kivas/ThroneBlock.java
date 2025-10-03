package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas.ThroneBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.entity.SeatEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ThroneBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ThroneBlock(BlockBehaviour.Properties properties) {
        super(properties
                .noOcclusion() // 不遮挡光线
                .strength(2.0f));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // 🚨 设置碰撞箱（确保玩家能坐进去）
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThroneBlockEntity(pos, state);
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

        // 如果是潜行右键，显示祭坛结构
        if (player.isShiftKeyDown()) {
            ClownBatSummoner.showAltarStructure(player, pos);
            return InteractionResult.SUCCESS;
        }

        // 检查是否拿着牙血鬼之牙
        if (itemStack.getItem() == ModItems.BLOODLINE_FANG.get()) {
            // 添加召唤限制检查
            if (ClownBatSummoner.hasPlayerClownBat(player, level)) {
                player.sendSystemMessage(Component.translatable("message.summon.player_has_bat"));
                return InteractionResult.FAIL;
            }

            if (ClownBatSummoner.trySummonClownBat(player, level, pos)) {
                // 消耗牙齿
                itemStack.shrink(1);
                // 播放音效和粒子效果
                level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN,
                        SoundSource.BLOCKS, 1.0F, 0.8F);
                spawnParticles(level, pos);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        // 🎯 检查玩家是否已经是乘客
        if (player.isPassenger()) {
            return InteractionResult.FAIL;
        }

        Direction direction = state.getValue(FACING);
        // 🎯 尝试不同的高度：0.8、1.0、1.2
        boolean success = SeatEntity.create(level, pos, 1.0, player, direction);

        return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
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

    // 🎯 重要：破坏时掉落自身
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.singletonList(new ItemStack(this));
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 方块被破坏时，让所有坐在这个方块上的 SeatEntity 消失
        if (!level.isClientSide) {
            // 查找所有位于这个位置的 SeatEntity
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class,
                    new AABB(pos).inflate(0.5));

            for (SeatEntity seat : seats) {
                // 让乘客停止乘坐
                if (seat.isVehicle()) {
                    seat.getFirstPassenger().stopRiding();
                }
                // 移除座椅实体
                seat.discard();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    // 移除 tick 方法中不存在的调用
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 如果你的王座不需要每 tick 都处理逻辑，可以移除这个方法
        // 或者保留它用于其他目的，但不要调用不存在的 ThroneBlockEntity.tick
        level.scheduleTick(pos, this, 1);
    }
}