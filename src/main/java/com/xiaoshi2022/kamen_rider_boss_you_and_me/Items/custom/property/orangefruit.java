package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.orangefruit.darkOrangeModel;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.orangefruit.orangefruitRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.DarkOrangeTransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.BeltUtils;
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
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class orangefruit extends Item implements GeoItem {
    // 静态ThreadLocal变量用于存储当前渲染的物品栈
    private static final ThreadLocal<ItemStack> CURRENT_RENDER_STACK = ThreadLocal.withInitial(() -> null);

    // 设置当前渲染物品栈的静态方法
    public static void setCurrentRenderStack(ItemStack stack) {
        CURRENT_RENDER_STACK.set(stack);
    }

    // 清除当前渲染物品栈的静态方法
    public static void clearCurrentRenderStack() {
        CURRENT_RENDER_STACK.remove();
    }
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation CUT_OPEN = RawAnimation.begin().thenPlay("cut_open");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean isDarkVariant = false; // 标记是否为darkOrange变种

    public orangefruit(net.minecraft.world.item.Item.Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
    @Override
    public ItemStack getDefaultInstance() {
        // 所有新实例都默认黑暗
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putBoolean("isDarkVariant", true);
        return stack;
    }

    // 检查物品栈是否为darkOrange变种
    public static boolean isDarkVariant(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getBoolean("isDarkVariant");
        }
        return false;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private orangefruitRenderer normalRenderer;
            private GeoItemRenderer<orangefruit> darkRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                // 从ThreadLocal获取当前渲染物品栈，如果没有则使用主手物品
                ItemStack stack = CURRENT_RENDER_STACK.get();
                if (stack == null) {
                    stack = ClientUtils.getClientPlayer().getMainHandItem();
                }

                if (isDarkVariant(stack)) {
                    if (this.darkRenderer == null) {
                        this.darkRenderer = orangefruitRenderer.createDarkOrangeRenderer();
                    }
                    return this.darkRenderer;
                } else {
                    if (this.normalRenderer == null) {
                        this.normalRenderer = new orangefruitRenderer();
                    }
                    return this.normalRenderer;
                }
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 仅主手能触发
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        // 检查腰带
        var beltOpt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(c -> c.findFirstCurio(i -> i.getItem() instanceof sengokudrivers_epmty));

        // 获取副手物品
        ItemStack offhand = player.getOffhandItem();

        if (beltOpt.isPresent()) {
            // 有腰带时，副手必须是柠檬锁种
            if (!offhand.is(ModItems.LEMON_ENERGY.get())) {
                if (!level.isClientSide) {
                    player.sendSystemMessage(Component.literal("副手必须持有LEMON锁种！"));
                }
                return InteractionResultHolder.fail(stack);
            }
        } else {
            // 没有腰带时，直接作为海姆冥界生成器使用
            return useAsHelheimGenerator(level, player, stack);
        }

        // 读取主手物品的自定义 NBT
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean("ready")) {
            // 第一次点击：仅提示，不消耗
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("再次点击以同时消耗橘子锁种与柠檬锁种"));
                tag.putBoolean("ready", true);
                // 写回栈，避免 Forge 误判
                player.setItemInHand(hand, stack);
                
                // 播放ORANGE和LEMON_ENERGY音效
                level.playSound(null, player, ModBossSounds.ORANGE.get(), SoundSource.PLAYERS, 1, 1);
                level.playSound(null, player, ModBossSounds.LEMON_ENERGY.get(), SoundSource.PLAYERS, 1, 1);

                // 在玩家头顶生成柠檬能量特效方块
                BlockPos aboveHead = player.blockPosition().above(2);
                if (level.isEmptyBlock(aboveHead)) {
                    level.setBlock(aboveHead,
                            com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks.ORANGELSX_BLOCK.get().defaultBlockState(),
                            Block.UPDATE_ALL);

                    level.playSound(null, aboveHead,
                            ModSounds.OPENDLOCK.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);

                    // 播放音效
                    level.playSound(null, aboveHead,
                            ModBossSounds.LEMON_ENERGY.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);

                    level.playSound(null, aboveHead,
                            ModSounds.OPENDLOCK.get(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F);

                }
            }
            return InteractionResultHolder.success(stack);
        } else {
            // 第二次点击：真正消耗
            if (level.isClientSide) {
                return InteractionResultHolder.success(stack);
            }

            // 已有锁种检查
            if (BeltUtils.hasActiveLockseed(player)) {
                player.sendSystemMessage(Component.literal("请先解除当前锁种！"));
                return InteractionResultHolder.fail(stack);
            }

            // 消耗两大锁种
            stack.shrink(1);
            offhand.shrink(1);

            // 发送Dark_orangels变身请求包
            PacketHandler.sendToServer(new DarkOrangeTransformationRequestPacket(player.getUUID()));

            // 玩家变量
            KRBVariables.PlayerVariables var = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables());
            var.orange_ready = true;
            var.syncPlayerVariables(player);

            // 音效
            level.playSound(null, player, ModBossSounds.LOCKONS.get(), SoundSource.PLAYERS, 1, 1);


            return InteractionResultHolder.success(ItemStack.EMPTY);
        }
    }

/**
 * 使用物品作为海姆冥界生成器的方法
 * @param level 当前游戏世界
 * @param player 使用物品的玩家
 * @param stack 被使用的物品堆栈
 * @return 交互结果持有者，表示物品使用的成功与否
 */
    private InteractionResultHolder<ItemStack> useAsHelheimGenerator(Level level, Player player, ItemStack stack) {
    // 如果物品没有NBT标签，则创建一个新的
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
    // 设置物品标签中的lockseed为true，锁定种子
        stack.getTag().putBoolean("lockseed", true);

    // 定义冷却间隔时间（12秒，以游戏刻为单位，1秒=20刻）
        final int INTERVAL = 12 * 20;
    // 获取玩家上次播放声音的时间
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");
    // 获取当前游戏时间
        long currentTime = level.getGameTime();

    // 检查冷却时间是否已结束
        if (currentTime - lastPlayed >= INTERVAL) {
        // 如果是服务器端世界
            if (level instanceof ServerLevel serverLevel) {
            // 触发物品的"open"动画
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
            serverLevel.playSound(null, pos, ModBossSounds.ORANGE.get(), SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "open", 20, state -> PlayState.STOP)
                .triggerableAnim("open", OPEN)
                .setSoundKeyframeHandler(state -> {
                    Player player = ClientUtils.getClientPlayer();
                    if (player != null)
                        player.playSound(ModBossSounds.ORANGE.get(), 1, 1);
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * 用于菜单 / 指令生成普通（非黑暗）橘子锁种
     */
    public static ItemStack createNormalVariant() {
        ItemStack stack = new ItemStack(ModItems.ORANGEFRUIT.get());
        // 确保黑暗标记不存在或为 false
        stack.getOrCreateTag().putBoolean("isDarkVariant", false);
        return stack;
    }

    /// // 在指令、按钮、物品栏事件等位置直接：
    /// ItemStack normalOrange = orangefruit.createNormalVariant();
    /// player.getInventory().add(normalOrange);   // 或给玩家/容器
}