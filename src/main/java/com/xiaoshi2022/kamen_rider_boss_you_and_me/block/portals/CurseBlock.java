package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.portals;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.CurseBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class CurseBlock extends BaseEntityBlock {
    public CurseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.getItem() == ModItems.GIIFUSTEAMP.get()) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("msg.kamen_rider.curse_block.using_stamp"),
                        true
                );
                return InteractionResult.SUCCESS;
            }

            if (player instanceof ServerPlayer serverPlayer) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof CurseBlockEntity curseBlockEntity) {

                    BlockPos previousPos = curseBlockEntity.getPreviousCurseBlockPos();

                    if (previousPos != null && level.isLoaded(previousPos)) {
                        // 检查上一次放置的位置是否还有CURSE_BLOCK
                        if (isCurseBlockAtPosition(level, previousPos)) {
                            // 传送到上一次放置的位置
                            teleportToPreviousPosition(serverPlayer, previousPos);
                            serverPlayer.displayClientMessage(
                                    Component.translatable("msg.kamen_rider.curse_block.teleport_to_previous"),
                                    false
                            );
                        } else {
                            // 上一次的位置方块被拆除了，传送到世界出生点
                            teleportToOverworld(serverPlayer);
                            serverPlayer.displayClientMessage(
                                    Component.translatable("msg.kamen_rider.curse_block.previous_destroyed"),
                                    false
                            );
                        }
                    } else {
                        // 没有记录上一次位置，传送到世界出生点
                        teleportToOverworld(serverPlayer);
                        serverPlayer.displayClientMessage(
                                Component.translatable("msg.kamen_rider.curse_block.teleport_to_spawn"),
                                false
                        );
                    }

                    // 消耗耐久
                    if (!player.isCreative()) {
                        itemStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    // 检查指定位置是否有CURSE_BLOCK
    private boolean isCurseBlockAtPosition(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof CurseBlock;
    }

    // 当方块被放置时更新链式记录
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CurseBlockEntity newCurseBlockEntity) {

            // 查找最近的玩家
            Player nearestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10, false);
            if (nearestPlayer != null) {
                // 获取玩家最近放置的CURSE_BLOCK位置
                BlockPos previousPos = getPlayersLastCurseBlockPos(nearestPlayer);

                if (previousPos != null) {
                    // 设置这个新方块的上一位置
                    newCurseBlockEntity.setPreviousCurseBlockPos(previousPos);

                    if (nearestPlayer instanceof ServerPlayer serverPlayer) {
                        serverPlayer.displayClientMessage(
                                Component.translatable("msg.kamen_rider.curse_block.chain_linked"),
                                false
                        );
                    }
                }

                // 更新玩家最后放置的位置
                setPlayersLastCurseBlockPos(nearestPlayer, pos);
            }
        }
    }

    // 获取玩家最近放置的CURSE_BLOCK位置（从玩家NBT数据）
    @Nullable
    private BlockPos getPlayersLastCurseBlockPos(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.contains("LastCurseBlockPos")) {
            int[] posArray = persistentData.getIntArray("LastCurseBlockPos");
            if (posArray.length == 3) {
                return new BlockPos(posArray[0], posArray[1], posArray[2]);
            }
        }
        return null;
    }

    // 设置玩家最近放置的CURSE_BLOCK位置（保存到玩家NBT数据）
    private void setPlayersLastCurseBlockPos(Player player, BlockPos pos) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putIntArray("LastCurseBlockPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    // 传送到上一次位置
    private void teleportToPreviousPosition(ServerPlayer player, BlockPos targetPos) {
        ServerLevel targetLevel = player.server.getLevel(Level.OVERWORLD);
        if (targetLevel == null) {
            teleportToOverworld(player);
            return;
        }

        BlockPos safePos = findSafeSpawnPosition(targetLevel, targetPos);
        player.teleportTo(targetLevel, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                player.getYRot(), player.getXRot());

        targetLevel.playSound(null, safePos, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private void teleportToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        BlockPos spawnPos = overworld.getSharedSpawnPos();
        BlockPos safePos = findSafeSpawnPosition(overworld, spawnPos);

        player.teleportTo(overworld, safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5,
                player.getYRot(), player.getXRot());

        overworld.playSound(null, safePos, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private BlockPos findSafeSpawnPosition(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

        for (int i = 0; i < 32 && mutablePos.getY() < level.getMaxBuildHeight() - 1; i++) {
            if (level.getBlockState(mutablePos).isAir() &&
                    level.getBlockState(mutablePos.above()).isAir() &&
                    !level.getBlockState(mutablePos.below()).isAir()) {
                return mutablePos.immutable();
            }
            mutablePos.move(0, 1, 0);
        }

        mutablePos.set(pos.getX(), Math.min(pos.getY(), level.getMaxBuildHeight() - 2), pos.getZ());
        for (int i = 0; i < 32 && mutablePos.getY() > level.getMinBuildHeight() + 1; i++) {
            if (level.getBlockState(mutablePos).isAir() &&
                    level.getBlockState(mutablePos.above()).isAir() &&
                    !level.getBlockState(mutablePos.below()).isAir()) {
                return mutablePos.immutable();
            }
            mutablePos.move(0, -1, 0);
        }

        return pos;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CurseBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}