package com.xiaoshi2022.kamen_rider_boss_you_and_me.block.giifu;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.GiifuSleepingStateBlockEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects.ModEffects;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.GiifuHumanEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class GiifuSleepingStateBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty AWAKENED = BooleanProperty.create("awakened");

    /* 骑士踢标记 & 封印 key */
    public static final String TAG_RIDER_KICK = "is_rider_kick";
    private static final String KICK_BROKEN = "giifu_kick_broken";

    public GiifuSleepingStateBlock(Properties properties) {
        super(properties.noOcclusion().strength(3.0f, 6.0F)); // ← 爆炸抗性降下来
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(AWAKENED, false));
    }

    /* -------- 基本方块 -------- */

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AWAKENED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GiifuSleepingStateBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Block.box(1, 0, 1, 15, 16, 15);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    /* -------- 挖掘保护 -------- */

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        // 只判断“是否已觉醒”，未觉醒可挖，觉醒后生存模式挖不动
        if (state.getValue(AWAKENED) && !player.isCreative()) {
            return 0.0F; // 生存模式挖不动
        }
        return super.getDestroyProgress(state, player, level, pos); // 创造模式照常
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // 只允许创造模式玩家破坏已觉醒石像
        if (state.getValue(AWAKENED) && !player.isCreative()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("只有创造模式才能破坏已觉醒的基夫石像！"), true);
            }
            return; // 阻止破坏
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    /* -------- 右键交互 -------- */

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        // GiifuSleepingStateBlock#use
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GiifuSleepingStateBlockEntity blockEntity
                && blockEntity.isKicked()) {
            player.displayClientMessage(Component.literal("这块石像已被永久封印，不再回应召唤……"), true);
            return InteractionResult.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);

        /* 1. 唤醒 */
        if (held.is(ModItems.GIIFUSTEAMP.get()) && !state.getValue(AWAKENED)) {
            level.setBlockAndUpdate(pos, state.setValue(AWAKENED, true));
            GiifuHumanEntity human = ModEntityTypes.GIIFU_HUMAN.get().create(level);
            human.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            human.setSleepingPos(pos);
            human.setStatuePos(pos);
            level.addFreshEntity(human);
            player.displayClientMessage(Component.literal("基夫响应了召唤……"), true);
            level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.SUCCESS;
        }

        /* 2. DNA 事件 */
        if (held.is(Items.GOLDEN_SWORD)) {
            return tryDnaEvent(level, pos, player);
        }

        return InteractionResult.PASS;
    }

    /* -------- 骑士踢爆 -------- */

    public static void markNextAttackAsRiderKick(Player attacker) {
        attacker.getPersistentData().putBoolean(TAG_RIDER_KICK, true);
    }


    public static boolean consumeRiderKickMark(Player attacker) {
        CompoundTag persistentData = attacker.getPersistentData();
        boolean hasMark = persistentData.getBoolean(TAG_RIDER_KICK);
        if (hasMark) {
            persistentData.remove(TAG_RIDER_KICK);
        }
        return hasMark;
    }


    // GiifuSleepingStateBlock.java
    public void tryKickExplode(Level level, BlockPos pos, Player kicker) {
        if (!(level instanceof ServerLevel srv)) return;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof GiifuSleepingStateBlock)
                || !state.getValue(AWAKENED)) return;
        if (!consumeRiderKickMark(kicker)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GiifuSleepingStateBlockEntity blockEntity
                && !blockEntity.isKicked()) {

            // 1. 视觉效果
            srv.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1, 0, 0, 0, 1);
            srv.playSound(null, pos, SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS, 1, 1);

            // 2. 本石像标记为已踢爆
            blockEntity.setKicked(true);
            level.destroyBlock(pos, false);        // 立即消失

            // 3. 世界广播（仅本石像）
            srv.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("基夫石像被骑士踢粉碎——这片区域的恶魔始祖已被封印！"),
                    false
            );
        }
    }

    /* -------- 数据存储 -------- */

    private static class GiifuSavedData extends SavedData {
        private boolean sealed = false;

        public GiifuSavedData() {}

        public static GiifuSavedData create() {
            return new GiifuSavedData();
        }

        public static GiifuSavedData load(CompoundTag tag) {
            GiifuSavedData data = new GiifuSavedData();
            data.sealed = tag.getBoolean(KICK_BROKEN);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putBoolean(KICK_BROKEN, sealed);
            return tag;
        }

        public boolean isSealed() {
            return sealed;
        }

        public void setSealed(boolean sealed) {
            this.sealed = sealed;
            this.setDirty();
        }
    }

    private boolean isWorldSealed(Level level) {
        if (level.getServer() == null) return false;
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        GiifuSavedData data = storage.computeIfAbsent(
                GiifuSavedData::load,        // Function<CompoundTag, T>
                GiifuSavedData::create,          // Supplier<T>
                KICK_BROKEN
        );
        return data.isSealed();
    }

    private void setWorldSealed(Level level, boolean flag) {
        if (level.getServer() == null) return;
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        GiifuSavedData data = storage.computeIfAbsent(
                GiifuSavedData::load, // ② 1 参反序列化
                GiifuSavedData::create,              // ① 无参工厂
                KICK_BROKEN
        );
        data.setSealed(flag);
    }

    /* -------- 其他 -------- */

    private InteractionResult tryDnaEvent(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel srv)) return InteractionResult.SUCCESS;
        double roll = srv.getRandom().nextDouble();
        if (roll < 0.6) {
            player.kill();
            var d = ModEntityTypes.GIFFTARIAN.get().create(srv);
            d.moveTo(player.getX(), player.getY(), player.getZ(), 0, 0);
            srv.addFreshEntity(d);
            player.sendSystemMessage(Component.literal("基夫的DNA侵蚀了你……你死了！"));
        } else {
            // 直接设置玩家为基夫种族，不再使用旧的hasGiifuDna标记
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                if (!variables.isGiifu) {
                    variables.isGiifu = true;
                    variables.syncPlayerVariables(player);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§d基夫血脉觉醒！你现在是基夫种族！§r"), true);
                    
                    // 触发成就：献上感谢，亡命徒
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        kamen_rider_boss_you_and_me.GIIFU_TRIGGER.trigger(serverPlayer);
                    }
                }
            });
            player.addEffect(new MobEffectInstance(ModEffects.GIIFU_DNA.get(), -1, 0, true, false));
            player.sendSystemMessage(Component.literal("你继承了基夫的遗传因子！"));
        }
        srv.playSound(null, pos, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.BLOCKS, 1, 0.5f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}
}