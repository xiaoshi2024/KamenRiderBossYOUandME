package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.BatStamp.BatStampRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Two_sidriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension.GiifuCurseDimension;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.ITeleporter;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BatStampItem extends Item implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation SEAL = RawAnimation.begin().thenLoop("seal");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BatStampItem(Item.Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BatStampRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new BatStampRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player != null && !level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // 传送至基夫体内世界
            teleportToGiifuDimension(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void teleportToGiifuDimension(ServerPlayer player) {
        if (player.level().dimension() == GiifuCurseDimension.GIIFU_CURSE_DIM) {
            // 如果已经在基夫维度，传送回主世界
            player.changeDimension(player.server.overworld(), new SimpleTeleporter());
        } else {
            // 传送到基夫维度
            player.changeDimension(
                    player.server.getLevel(GiifuCurseDimension.GIIFU_CURSE_DIM),
                    new SimpleTeleporter()
            );
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.OFF_HAND) {
            // 只有在副手使用时才触发逻辑
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);

        // 检查玩家是否佩戴了 Two_sidriver 腰带，并且腰带的变种是否为 X 形态
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver && Two_sidriver.getDriverType(s) == Two_sidriver.DriverType.X));

        if (!opt.isPresent()) {
            // 如果没有佩戴 Two_sidriver 腰带，或者腰带的变种不是 X 形态，则不触发任何逻辑
            return InteractionResultHolder.pass(stack);
        }

        // 播放蝙蝠印章的音效
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModBossSounds.BAT.get(),
                SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        // 检查是否在服务器端
        if (!level.isClientSide()) {
            // 调用 insertIntoBelt 方法
            insertIntoBelt((ServerPlayer) player, hand, level, player);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private void insertIntoBelt(ServerPlayer serverPlayer, InteractionHand hand, Level level, Player player) {
        // 播放玩家动画
        playPlayerAnimation(serverPlayer, "bat_stamp");

        // 播放待机的音效
        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModBossSounds.EVIL_BY.get(),
                SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        // 设置2.5秒的延迟
        level.getServer().execute(() -> {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            level.getServer().execute(() -> {
                                ItemStack currentStack = serverPlayer.getItemInHand(hand);
                                if (currentStack.getItem() == BatStampItem.this) {
                                    // 查找玩家是否佩戴 Two_sidriver
                                    Optional<SlotResult> opt = CuriosApi.getCuriosInventory(serverPlayer)
                                            .resolve()
                                            .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));

                                    if (opt.isPresent()) {
                                        ItemStack belt = opt.get().stack();

                                        // 只有 X 形态可以被盖戳成 BAT
                                        if (Two_sidriver.getDriverType(belt) == Two_sidriver.DriverType.X) {
                                            Two_sidriver.setDriverType(belt, Two_sidriver.DriverType.BAT);
                                            Two_sidriver.syncToTracking(serverPlayer, belt);

                                            // 消耗印章 - 从正确的手上移除
                                            ItemStack handStack = serverPlayer.getItemInHand(hand);
                                            if (!handStack.isEmpty() && handStack.getItem() == BatStampItem.this) {
                                                handStack.shrink(1);
                                                if (handStack.isEmpty()) {
                                                    serverPlayer.setItemInHand(hand, ItemStack.EMPTY);
                                                } else {
                                                    serverPlayer.setItemInHand(hand, handStack);
                                                }
                                            }

                                            // 注释掉原有的实体生成逻辑，由XKeyLogic中的toEvil方法统一处理
                                            // 这样可以避免重复生成多个实体
                                            System.out.println("BatStampItem已标记腰带为BAT形态，实体生成将由XKeyLogic处理");
                                        }
                                    }
                                }
                            });
                        }
                    },
                    2500 // 2.5秒延迟
            );
        });
    }

    /* ========== 超音波攻击功能 ========== */
    /**
     * 执行超音波攻击的静态方法
     * 由BatUltrasonicAttackPacket调用
     */
    public static void performUltrasonicAttack(ServerPlayer serverPlayer) {
        Level level = serverPlayer.level();

        // 检查玩家是否佩戴了Two_sidriver腰带
        Optional<SlotResult> opt = CuriosApi.getCuriosInventory(serverPlayer)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s.getItem() instanceof Two_sidriver));

        boolean isEvilForm;
        if (opt.isPresent()) {
            ItemStack belt = opt.get().stack();
            Two_sidriver.DriverType type = Two_sidriver.getDriverType(belt);

            // 检查是否是evil形态
            isEvilForm = type == Two_sidriver.DriverType.BAT;
        } else {
            isEvilForm = false;
        }

        // 播放玩家动画 - stamps
        playPlayerAnimation(serverPlayer, "stamps");

        // 播放超音波攻击音效
        level.playSound(
                null,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                ModBossSounds.BAT.get(),
                SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        // 在服务器端执行攻击逻辑
        level.getServer().execute(() -> {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            level.getServer().execute(() -> {
                                // 获取玩家视线方向
                                Vec3 lookVec = serverPlayer.getLookAngle();

                                // 根据是否为evil形态调整攻击范围和伤害
                                double range = isEvilForm ? 15.0 : 10.0;
                                float damage = isEvilForm ? 38.0F : 8.0F;
                                double knockbackStrength = isEvilForm ? 2.0 : 1.5;

                                // 创建AABB来检测前方的实体
                                Vec3 startPos = serverPlayer.getEyePosition(1.0F);
                                Vec3 endPos = startPos.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);

                                // 创建一个长方体区域来检测实体
                                double width = isEvilForm ? 3.0 : 2.0;
                                AABB aabb = new AABB(
                                        Math.min(startPos.x, endPos.x) - width,
                                        Math.min(startPos.y, endPos.y) - 1.0,
                                        Math.min(startPos.z, endPos.z) - width,
                                        Math.max(startPos.x, endPos.x) + width,
                                        Math.max(startPos.y, endPos.y) + 1.0,
                                        Math.max(startPos.z, endPos.z) + width
                                );

                                // 获取区域内的所有实体
                                List<Entity> entities = level.getEntities(serverPlayer, aabb,
                                        entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.isAlive());

                                // 对每个实体造成伤害和击退
                                for (Entity entity : entities) {
                                    if (entity instanceof LivingEntity livingEntity && livingEntity != serverPlayer) {
                                        // 计算击退方向（从玩家到实体）
                                        Vec3 knockbackDir = entity.position().subtract(serverPlayer.position()).normalize();

                                        // 应用伤害
                                        boolean hurt = livingEntity.hurt(serverPlayer.damageSources().playerAttack(serverPlayer), damage);

                                        if (hurt) {
                                            // 应用击退效果
                                            livingEntity.push(knockbackDir.x * knockbackStrength, 0.3, knockbackDir.z * knockbackStrength);

                                            // 在evil形态下添加额外效果，例如减速
                                            if (isEvilForm) {
                                                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                                            }
                                        }
                                    }
                                }

                                // 生成视觉效果（粒子）
                                spawnUltrasonicParticles((ServerLevel) level, startPos, endPos, isEvilForm);
                            });
                        }
                    },
                    500 // 0.5秒延迟，与动画同步
            );
        });
    }

    /**
     * 生成超音波粒子效果
     */
    private static void spawnUltrasonicParticles(ServerLevel level, Vec3 startPos, Vec3 endPos, boolean isEvilForm) {
        // 计算方向向量和距离
        Vec3 direction = endPos.subtract(startPos).normalize();
        double distance = startPos.distanceTo(endPos);

        // 生成粒子
        int particleCount = isEvilForm ? 60 : 40;
        for (int i = 0; i < particleCount; i++) {
            // 计算沿光线的位置
            double progress = (double) i / particleCount;
            double offsetX = startPos.x + direction.x * distance * progress + (level.random.nextDouble() - 0.5) * 0.5;
            double offsetY = startPos.y + direction.y * distance * progress + (level.random.nextDouble() - 0.5) * 0.5;
            double offsetZ = startPos.z + direction.z * distance * progress + (level.random.nextDouble() - 0.5) * 0.5;

            // 生成粒子
            level.sendParticles(
                    ParticleTypes.SONIC_BOOM,
                    offsetX, offsetY, offsetZ,
                    1, 0, 0, 0, 0
            );

            // 在evil形态下额外添加红色粒子
            if (isEvilForm && level.random.nextBoolean()) {
                level.sendParticles(
                        ParticleTypes.DRIPPING_LAVA,
                        offsetX, offsetY, offsetZ,
                        1, 0, 0, 0, 0
                );
            }
        }
    }

    /* ========== 玩家动画支持 ========== */
    /* 发送动画到客户端 */
    public static void playPlayerAnimation(ServerPlayer player, String animationName) {
        if (player.level().isClientSide()) return;

        PacketHandler.sendAnimationToAll(
                Component.literal(animationName),
                player.getId(),
                false
        );
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, this::idleAnimController));
        controllers.add(new AnimationController<>(this, "seal", 20, state -> PlayState.CONTINUE)
                .triggerableAnim("seal", SEAL));
    }

    private PlayState idleAnimController(AnimationState<BatStampItem> animationState) {
        animationState.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 简单的传送器实现
    private static class SimpleTeleporter implements ITeleporter {
        public Vec3 getPortalArrivalPosition(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Entity entity, float yRot) {
            // 传送到平台中心上方
            return new Vec3(
                    GiifuCurseDimension.PLATFORM_CENTER.getX() + 0.5,
                    GiifuCurseDimension.PLATFORM_CENTER.getY() + 1,
                    GiifuCurseDimension.PLATFORM_CENTER.getZ() + 0.5
            );
        }
    }
}