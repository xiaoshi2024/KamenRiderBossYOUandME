package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.giifusteamp.giifusteampRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.giifu.GiifuSleepingStateBlock;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import tocraft.walkers.api.PlayerShape;

import java.util.function.Consumer;

public class giifusteamp extends Item implements GeoItem {
    private static final RawAnimation idle = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public giifusteamp(Properties p_41383_) {
        super(p_41383_);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private giifusteampRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new giifusteampRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 检查是否为生存模式
        if (!player.isCreative() && !player.isSpectator()) {
            final int INTERVAL = 12 * 20; // 12秒间隔，1秒 = 20 tick
            long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");
            long currentTime = level.getGameTime();

            if (currentTime - lastPlayed >= INTERVAL) {
                // 检查玩家是否在看向基夫石像
                HitResult hitResult = Minecraft.getInstance().hitResult;

                if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    BlockState blockState = level.getBlockState(blockPos);

                    // 检查是否右键了基夫石像
                    if (blockState.getBlock() instanceof GiifuSleepingStateBlock) {
                        // 右键基夫石像 - 变异玩家为基夫德莫斯
                        if (level instanceof ServerLevel serverLevel) {
                            // 触发动画
                            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "extruding", "extruding");

                            // 播放音效
                            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.SEAL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                            // 更新玩家最后一次播放音效的时间
                            player.getPersistentData().putLong("lastPlayedSound", currentTime);
                        }

                        // 尝试将玩家变身为基夫德莫斯
                        if (player instanceof ServerPlayer serverPlayer) {
                            transformPlayerToGiifuDemosEntity(serverPlayer);
                            player.displayClientMessage(Component.literal("已变身为基夫德莫斯！"), true);
                            return InteractionResultHolder.success(player.getItemInHand(hand));
                        }
                    }
                } else {
                    // 右键空气 - 检查玩家是否为人类形态
                    if (isPlayerInHumanForm(player)) {
                        // 杀死玩家并生成门徒
                        if (level instanceof ServerLevel serverLevel) {
                            // 触发动画
                            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "extruding", "extruding");

                            // 播放音效
                            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.SEAL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                            // 杀死玩家
                            player.kill();

                            // 在玩家位置生成门徒
                            spawnGifftarianAtPlayerLocation(serverLevel, player);

                            // 更新玩家最后一次播放音效的时间
                            player.getPersistentData().putLong("lastPlayedSound", currentTime);

                            player.displayClientMessage(Component.literal("你已被杀死，门徒已生成！"), true);
                            return InteractionResultHolder.success(player.getItemInHand(hand));
                        }
                    } else {
                        // 玩家不是人类形态，显示提示信息
                        player.displayClientMessage(Component.literal("你当前不是人类形态，无法被杀死！"), true);
                        return InteractionResultHolder.fail(player.getItemInHand(hand));
                    }
                }
            } else {
                // 提示玩家冷却时间未结束
                player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
            }
        } else {
            // 提示创造模式或旁观模式下无法使用
            player.displayClientMessage(Component.literal("创造模式或旁观模式下无法使用此功能"), true);
        }

        return super.use(level, player, hand);
    }

    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, this::idleAnimController));
    }

    private PlayState idleAnimController(AnimationState<giifusteamp> giifusteampAnimationState) {
        giifusteampAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    private void transformPlayerToGiifuDemosEntity(ServerPlayer serverPlayer) {
        // 创建基夫德莫斯实体实例
        EntityType<GiifuDemosEntity> giifuDemosEntityType = ModEntityTypes.GIIFUDEMOS_ENTITY.get();
        GiifuDemosEntity giifuDemosEntity = giifuDemosEntityType.create(serverPlayer.level());
        if (giifuDemosEntity == null) {
            System.err.println("Failed to create GiifuDemosEntity");
            return;
        }

        // 初始化实体并设置位置
        giifuDemosEntity.moveTo(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());

        // 调用PlayerShape.updateShapes方法进行变形
        if (PlayerShape.updateShapes(serverPlayer, giifuDemosEntity)) {
            System.out.println("Player transformed to GiifuDemosEntity successfully");
        } else {
            System.err.println("Failed to transform player to GiifuDemosEntity");
        }
    }

    private void spawnGifftarianAtPlayerLocation(ServerLevel level, Player player) {
        // 创建基夫门徒实体
        EntityType<Gifftarian> gifftarianEntityType = ModEntityTypes.GIFFTARIAN.get(); // 假设有这个实体类型注册
        if (gifftarianEntityType == null) {
            System.err.println("Failed to find Gifftarian entity type");
            return;
        }

        Gifftarian gifftarian = gifftarianEntityType.create(level);
        if (gifftarian == null) {
            System.err.println("Failed to create Gifftarian");
            return;
        }

        // 设置门徒位置和旋转
        gifftarian.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

        // 添加到世界
        level.addFreshEntity(gifftarian);
        System.out.println("Gifftarian spawned at player location");
    }

    /**
     * 检查玩家是否处于人类形态
     * @param player 要检查的玩家
     * @return 如果玩家是人类形态返回true，否则返回false
     */
    private boolean isPlayerInHumanForm(Player player) {
        // 使用Walkers API检查当前形态
        try {
            LivingEntity currentShape = PlayerShape.getCurrentShape(player);

            // 如果getCurrentShape返回null，说明玩家没有变形，处于人类形态
            if (currentShape == null) {
                return true;
            }

            // 如果当前形态是玩家形态（EntityType.PLAYER），则返回true
            // 注意：这里需要检查实体类型是否为PLAYER，而不是类名
            return currentShape.getType() == EntityType.PLAYER;
        } catch (Exception e) {
            System.err.println("Error checking player shape: " + e.getMessage());
            // 发生错误时，默认返回true（假设是人类形态）
            return true;
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}