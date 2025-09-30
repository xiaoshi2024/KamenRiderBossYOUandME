package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.PeachEnergy.PeachEnergyRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class peach_energy extends Item implements GeoItem {
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("start");
    private static final RawAnimation SCATTER = RawAnimation.begin().thenPlay("scatter");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public peach_energy(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private PeachEnergyRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new PeachEnergyRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查玩家是否装备了创世纪驱动器
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve().flatMap(
                curios -> curios.findFirstCurio(item -> item.getItem() instanceof Genesis_driver)
        );

        if (beltOptional.isPresent()) {
            // 如果玩家装备了创世纪驱动器
            if (!stack.getOrCreateTag().contains("first_click")) {
                // 第一次右键点击
                stack.getOrCreateTag().putBoolean("first_click", true);

                // 播放open动画
                if (level instanceof ServerLevel serverLevel) {
                    triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "start");
                }


                // 在玩家头顶生成桃子能量特效方块
                BlockPos aboveHead = player.blockPosition().above(2);
                if (level.isEmptyBlock(aboveHead)) {
                    level.setBlock(aboveHead,
                            ModBlocks.PEACHX_BLOCK.get().defaultBlockState(),
                            Block.UPDATE_ALL);

                    // 播放音效
                    level.playSound(null, aboveHead,
                            ModBossSounds.PEACH_ENERGY.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);
                }

                player.displayClientMessage(Component.literal("再次点击装备桃子能量锁种"), true);
                return InteractionResultHolder.success(stack);
            } else {
                // 第二次右键点击 - 将锁种插入腰带
                if (!level.isClientSide) {
                    // 检查玩家是否已经装备了其他锁种
                    if (com.xiaoshi2022.kamen_rider_boss_you_and_me.util.BeltUtils.hasActiveLockseed(player)) {
                        player.sendSystemMessage(Component.literal("您已经装备了其他锁种，请先解除变身！"));
                        return InteractionResultHolder.success(stack);
                    }
                    
                    // 播放LOCK ON音效
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.LEMON_LOCKONBY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 消耗锁种
                    stack.shrink(1);

                    // 更新腰带为桃子形态
                    ItemStack beltStack = beltOptional.get().stack();
                    Genesis_driver belt = (Genesis_driver) beltStack.getItem();
                    belt.setMode(beltStack, Genesis_driver.BeltMode.PEACH);

                    // 获取PlayerVariables实例并设置状态
                    KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                    variables.peach_ready = true;
                    variables.peach_ready_time = level.getGameTime();
                    variables.syncPlayerVariables(player); // 同步变量到客户端

                    // 发送客户端提示消息
                    player.sendSystemMessage(Component.literal("桃子锁种已装载！按变身键变身"));
                }
                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        } else {
            // 如果玩家没有装备创世纪驱动器，则调用 useAsEnergyGenerator 方法
            return useAsEnergyGenerator(level, player, stack);
        }
    }

    private InteractionResultHolder<ItemStack> useAsEnergyGenerator(Level level, Player player, ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putBoolean("lockseed", true);

        final int INTERVAL = 12 * 20;
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");
        long currentTime = level.getGameTime();

        if (currentTime - lastPlayed >= INTERVAL) {
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "start", "start");

                int generatedBlocks = player.getPersistentData().getInt("generatedBlocks");
                if (generateHelheimCrack(level, player)) {
                    generatedBlocks++;
                }

                player.getPersistentData().putInt("generatedBlocks", generatedBlocks);

                if (generatedBlocks >= 5) {
                    createEnergyExplosion(level, player);
                    player.getPersistentData().putInt("generatedBlocks", 0);
                }

                player.getPersistentData().putLong("lastPlayedSound", currentTime);
            }
        } else {
            player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private boolean generateHelheimCrack(Level level, Player player) {
        Random random = new Random();
        BlockPos playerPos = player.blockPosition();
        int range = 4; // 生成范围为 4 格

        // 35% 的几率生成方块
        if (random.nextDouble() < 0.35) {
            // 随机生成一个位置
            BlockPos randomPos = new BlockPos(
                    playerPos.getX() + random.nextInt(range * 2) - range,
                    playerPos.getY() + random.nextInt(range * 2) - range,
                    playerPos.getZ() + random.nextInt(range * 2) - range
            );

            // 检查生成位置是否为空，如果是，则放置方块
            if (level.isEmptyBlock(randomPos)) {
                BlockState state = com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks.HELHEIM_CRACK_BLOCK.get().defaultBlockState();
                level.setBlockAndUpdate(randomPos, state);

                // 播放声音
                playSound(level, player, randomPos);
                return true; // 成功生成一个方块
            }
        }
        return false; // 未生成方块
    }

    private void createEnergyExplosion(Level level, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            // 桃子能量爆炸不会伤害玩家，而是给予短暂buff
            player.getFoodData().eat(2, 0.3f); // 恢复少量饥饿值

        }
    }

    private void playSound(Level level, Player player, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, pos, ModBossSounds.PEACH_ENERGY.get(), SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, state -> PlayState.STOP)
                .triggerableAnim("start", OPEN)
                .triggerableAnim("scatter", SCATTER)
                .setSoundKeyframeHandler(state -> {
                    Player player = ClientUtils.getClientPlayer();
                    if (player != null) {
                        if (state.getController().getCurrentAnimation() != null &&
                                "scatter".equals(state.getController().getCurrentAnimation().animation().name())) {
                        } else {
                            player.playSound(ModBossSounds.PEACH_ENERGY.get(), 1, 1);
                        }
                    }
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
