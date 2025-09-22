package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Necrom_eye.Necrom_eyeRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.KnecromghostEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

public class Necrom_eye extends Item implements GeoItem {

    public static final int SOUND_COOLDOWN = 5 * 20; // 5 秒
    private static final RawAnimation START = RawAnimation.begin().thenPlay("start");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Necrom_eye(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    /* ---------- 客户端渲染器 ---------- */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private Necrom_eyeRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new Necrom_eyeRenderer();
                return renderer;
            }
        });
    }

    /* ---------- 主使用逻辑 ---------- */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        /* 分支 1：佩戴手环且为 DEFAULT 模式 → 插入眼魂 */
        if (level instanceof ServerLevel serverLevel) {
            Optional<SlotResult> megaSlot = CurioUtils.findFirstCurio(player,
                    s -> s.getItem() instanceof Mega_uiorder);
            if (megaSlot.isPresent()) {
                ItemStack beltStack = megaSlot.get().stack();
                Mega_uiorder belt = (Mega_uiorder) beltStack.getItem();
                if (belt.getCurrentMode(beltStack) == Mega_uiorder.Mode.DEFAULT) {
                    // 切换模式
                    belt.switchMode(beltStack, Mega_uiorder.Mode.NECROM_EYE);

                    playPlayerAnimation((ServerPlayer) player, "necrom_henshin");

                    // 同步动画
                    PacketHandler.sendToAllTracking(
                            new BeltAnimationPacket(player.getId(), "heixn", "mega_uiorder",
                                    Mega_uiorder.Mode.NECROM_EYE.name()), player);
                    // 消耗物品
                    stack.shrink(1);

                    if (!level.isClientSide()) {
                        KnecromghostEntity necroEntity = new KnecromghostEntity(ModEntityTypes.KNECROMGHOST.get(), level);
                        necroEntity.setTargetPlayer(player); // 设置目标玩家并开始骑乘（这个方法内部会处理位置）
                        level.addFreshEntity(necroEntity);
                    }

                    // 播放固定音效
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.LOGIN_BY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    // 置位待机
                    player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                            .ifPresent(vars -> {
                                vars.isNecromStandby = true;
                                vars.syncPlayerVariables(player);
                            });
                    player.displayClientMessage(
                            Component.literal("眼魂已插入，待机中... 按 X 键完成变身"), true);
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
                // 戴了手环但模式不对
                player.displayClientMessage(
                        Component.literal("手环状态异常，无法插入眼魂！"), true);
                return InteractionResultHolder.fail(stack);
            }
        }

        /* 分支 2：未佩戴手环 → 纯播放特效（不消耗） */
        if (!isWearingBracelet(player)) {
            long last = player.getPersistentData().getLong("lastPlayedSound");
            long now = level.getGameTime();
            if (now - last < SOUND_COOLDOWN) {
                player.displayClientMessage(Component.literal(
                        "冷却时间未结束，还需等待 " + (SOUND_COOLDOWN - (now - last)) / 20 + " 秒"), true);
                return InteractionResultHolder.fail(stack);
            }
            if (level instanceof ServerLevel serverLevel) {
                // 随机 3 选 1
                int idx = new Random().nextInt(3);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        idx == 0 ? ModBossSounds.STAND_BY_NECROM.get()
                                : idx == 1 ? ModBossSounds.LOADING_EYE.get()
                                : ModBossSounds.DESTROY_EYE.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                player.getPersistentData().putLong("lastPlayedSound", now);
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel),
                        "start", "start");
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // 默认失败
        return InteractionResultHolder.fail(stack);
    }

    /* 发送动画到客户端 */
    public static void playPlayerAnimation(ServerPlayer player, String animationName) {
        if (player.level().isClientSide()) return;

        PacketHandler.sendAnimationToAll(
                Component.literal(animationName),
                player.getId(),
                false
        );
    }

    /* ---------- 工具方法 ---------- */
    private boolean isWearingBracelet(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof Mega_uiorder)
                        .isPresent())
                .orElse(false);
    }

    /* ---------- GeckLib 动画 ---------- */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "start", 20, state -> PlayState.STOP)
                .triggerableAnim("start", START)
                .setSoundKeyframeHandler(state -> {
                    Player player = ClientUtils.getClientPlayer();
                    if (player != null)
                        player.playSound(ModBossSounds.STAND_BY_NECROM.get(), 1, 1);
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}