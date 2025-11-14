package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.bananafruit.bananafruitRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
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

public class bananafruit extends Item implements GeoItem {
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation CUT_OPEN = RawAnimation.begin().thenPlay("cut_open");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public bananafruit(net.minecraft.world.item.Item.Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private bananafruitRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new bananafruitRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查玩家是否装备了腰带
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve().flatMap(
                curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty)
        );

        if (beltOptional.isPresent()) {
            // 如果玩家装备了腰带
            if (!stack.getOrCreateTag().contains("first_click")) {
                // 第一次右键点击
                stack.getOrCreateTag().putBoolean("first_click", true);

                // 播放open动画
                if (level instanceof ServerLevel serverLevel) {
                    triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "open");
                }


                // 在玩家头顶生成特效方块
                BlockPos aboveHead = player.blockPosition().above(2);
                if (level.isEmptyBlock(aboveHead)) {
                    level.setBlock(aboveHead,
                            com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks.BANANAS_BLOCK.get().defaultBlockState(),
                            Block.UPDATE_ALL);

                    // 播放音效
                    level.playSound(null, aboveHead,
                            ModSounds.OPENDLOCK.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);
                    level.playSound(null, aboveHead,
                            ModBossSounds.BANANAFRUITENERGY.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);
                }

                player.displayClientMessage(Component.literal("再次点击装备锁种"), true);
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
                            ModBossSounds.LOCKONS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    // 播放待机音效
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.BANANABY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    // 消耗锁种
                    stack.shrink(1);

                    //更新腰带为banana形态
                    ItemStack beltStack = beltOptional.get().stack();
                    sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.getItem();
                    belt.setMode(beltStack, sengokudrivers_epmty.BeltMode.BANANA);

                    // 获取PlayerVariables实例并设置状态
                    KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                    variables.banana_ready = true;
                    variables.syncPlayerVariables(player); // 同步变量到客户端
                }
                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        } else {
            // 如果玩家没有装备腰带，则调用 useAsHelheimGenerator 方法
            return useAsHelheimGenerator(level, player, stack);
        }
    }

    private InteractionResultHolder<ItemStack> useAsHelheimGenerator(Level level, Player player, ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putBoolean("lockseed", true);

        final int INTERVAL = 12 * 20;
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");
        long currentTime = level.getGameTime();

        if (currentTime - lastPlayed >= INTERVAL) {
            if (level instanceof ServerLevel serverLevel) {
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "open", "open");

                int generatedBlocks = player.getPersistentData().getInt("generatedBlocks");
                if (generateHelheimCrack(level, player)) {
                    generatedBlocks++;
                }

                player.getPersistentData().putInt("generatedBlocks", generatedBlocks);

                if (generatedBlocks >= 5) {
                    createPlayerOnlyExplosion(level, player);
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
                BlockState state = ModBlocks.HELHEIM_CRACK_BLOCK.get().defaultBlockState();
                level.setBlockAndUpdate(randomPos, state);

                // 播放声音
                playSound(level, player, randomPos);
                return true; // 成功生成一个方块
            }
        }
        return false; // 未生成方块
    }

    private void createPlayerOnlyExplosion(Level level, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            // 在玩家脚下生成一个爆炸，只对玩家造成伤害
            player.hurt(player.damageSources().generic(), 3.0F); // 对玩家造成3点伤害

            // 检查玩家是否处于残血状态（生命值小于等于1点）
            if (player.getHealth() <= 1.0F) {
                // 如果玩家死亡，在xx你被什么杀害了改为自定义
                String deathMessage = player.getName().getString() + "被自己贪玩的小手背叛了";
                player.sendSystemMessage(Component.literal(deathMessage));
            }
        }
    }

    private void playSound(Level level, Player player, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, pos, ModBossSounds.BANANAFRUITENERGY.get(), SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "open", 20, state -> PlayState.STOP)
                .triggerableAnim("open", OPEN)
                .setSoundKeyframeHandler(state -> {
                    Player player = ClientUtils.getClientPlayer();
                    if (player != null)
                        player.playSound(ModBossSounds.BANANAFRUITENERGY.get(), 1, 1);
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}