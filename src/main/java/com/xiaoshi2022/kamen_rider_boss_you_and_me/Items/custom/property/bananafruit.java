package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.bananafruit.bananafruitRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
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
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class bananafruit extends Item implements GeoItem {
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation CUT_OPEN = RawAnimation.begin().thenPlay("cut_open");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public bananafruit(net.minecraft.world.item.Item.Properties properties) {
        super(properties);

        // Register our item as server-side handled.
        // This enables both animation data syncing and server-side animation triggering
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
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

        // 获取玩家的 Curios 库存
        AtomicReference<ItemStack> beltStack = new AtomicReference<>(ItemStack.EMPTY); // 初始化为空物品堆
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            // 获取特定槽类型的库存（例如 "belt"）
            Optional<ICurioStacksHandler> beltInventory = curiosInventory.getStacksHandler("belt");
            beltInventory.ifPresent(slotInventory -> {
                // 遍历槽类型中的物品
                for (int i = 0; i < slotInventory.getSlots(); i++) {
                    ItemStack curioStack = slotInventory.getStacks().getStackInSlot(i);
                    if (curioStack.getItem() instanceof sengokudrivers_epmty) {
                        // 如果找到符合条件的物品，赋值给 beltStack
                        beltStack.set(curioStack);
                        break; // 找到后退出循环
                    }
                }
            });
        });

        if (!beltStack.get().isEmpty()) {
            // 腰带模式切换逻辑
            CompoundTag tag = stack.getOrCreateTag();
            int clickCount = tag.getInt("clickCount");

            if (clickCount == 0) {
                // 第一次点击
                tag.putInt("clickCount", 1);
                player.displayClientMessage(Component.literal("再次点击以装备香蕉锁种！"), true);
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            } else {
                // 第二次点击 - 切换腰带模式
                sengokudrivers_epmty belt = (sengokudrivers_epmty) beltStack.get().getItem();
                belt.setMode(beltStack.get(), sengokudrivers_epmty.BeltMode.BANANA);

                // 消耗一个香蕉锁种
                stack.shrink(1);
                return InteractionResultHolder.sidedSuccess(ItemStack.EMPTY, level.isClientSide());
            }
        } else {
            // 没有装备腰带时执行原有功能
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
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                    // Use helper method to avoid client-code in common class
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
