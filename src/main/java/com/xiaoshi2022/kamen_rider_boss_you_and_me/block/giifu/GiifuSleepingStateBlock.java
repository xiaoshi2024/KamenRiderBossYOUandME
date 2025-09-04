package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.GiifuSleepingStateBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects.ModEffects;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    public GiifuSleepingStateBlock(Properties properties) {
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() != Items.GOLDEN_SWORD) return InteractionResult.PASS;

        // 触发 DNA 事件
        if (level instanceof ServerLevel server) {
            double roll = server.random.nextDouble();
            if (roll < 0.6) {
                // 死亡 + 生成门徒
                player.kill();
                Gifftarian disciple = ModEntityTypes.GIFFTARIAN.get().create(server);
                disciple.moveTo(player.getX(), player.getY(), player.getZ(), 0, 0);
                server.addFreshEntity(disciple);
                player.sendSystemMessage(Component.literal("基夫的DNA侵蚀了你……你死了！"));
            } else {
                // 获得因子
                player.getPersistentData().putBoolean("hasGiifuDna", true);
                player.addEffect(new MobEffectInstance(
                        ModEffects.GIIFU_DNA.get(),
                        -1,     // 永久
                        0,      // 等级 I
                        true,   // ← 这里才是 ambient（环境效果）
                        false   // 无粒子
                ));
                player.sendSystemMessage(Component.literal("你继承了基夫的遗传因子！"));
            }
            server.playSound(null, pos, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.BLOCKS, 1.0F, 0.5F);
        }
        return InteractionResult.SUCCESS;
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