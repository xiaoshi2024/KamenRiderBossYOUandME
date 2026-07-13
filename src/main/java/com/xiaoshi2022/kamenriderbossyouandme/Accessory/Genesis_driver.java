package com.xiaoshi2022.kamenriderbossyouandme.Accessory;

import com.jpigeon.ridebattlelib.common.api.RideBattleAPI;
import com.jpigeon.ridebattlelib.common.event.HenshinEvent;
import com.jpigeon.ridebattlelib.common.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.genesisdriver.GenesisDriverRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.network.packet.BYAnimationPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.xiaoshi2022.kamenriderbossyouandme.Accessory.Genesis_driver.BeltMode.DEFAULT;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class Genesis_driver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    /* ------------------------- 静态变量 ------------------------- */
    private static final Map<UUID, Integer> PLAYER_FRUIT_COOLDOWNS = new HashMap<>();

    /* ------------------------- 事件处理 ------------------------- */
    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Pre event) {
        if (event.isCanceled()) return;
        Player player = event.getPlayer();
        handleGenesisDriverHenshin(player);
    }

    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Post event) {
        Player player = event.getPlayer();
        handleGenesisDriverUnhenshin(player);
    }

    private static void handleGenesisDriverHenshin(Player player) {
        if (player instanceof ServerPlayer sp) {
            ItemStack beltStack = getBeltStack(sp);
            if (beltStack.isEmpty()) return;
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            belt.startHenshinAnimation(sp, beltStack);
            RideBattleAPI.completeIn(60, sp);
        }
    }

    private static void handleGenesisDriverUnhenshin(Player player) {
        if (player instanceof ServerPlayer sp) {
            ItemStack beltStack = getBeltStack(sp);
            if (beltStack.isEmpty()) return;
            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            belt.startReleaseAnimation(sp, beltStack);
        }
    }

    /* ------------------------- 动画常量 ------------------------- */
    private static final RawAnimation IDLES   = RawAnimation.begin().thenPlayAndHold("idles");
    private static final RawAnimation SHOW    = RawAnimation.begin().thenPlayAndHold("show");
    private static final RawAnimation LEMON_TICK    = RawAnimation.begin().thenPlayAndHold("lemon_tick");
    private static final RawAnimation START         = RawAnimation.begin().thenPlayAndHold("start");
    private static final RawAnimation SCATTER       = RawAnimation.begin().thenPlayAndHold("scatter");
    private static final RawAnimation MOVE          = RawAnimation.begin().thenPlayAndHold("lemon_move");
    private static final RawAnimation MELON_TICK    = RawAnimation.begin().thenPlayAndHold("melon_tick");
    private static final RawAnimation MELON_START   = RawAnimation.begin().thenPlayAndHold("melon_start");
    private static final RawAnimation MELON_SCATTER = RawAnimation.begin().thenPlayAndHold("melon_scatter");
    private static final RawAnimation MELON_MOVE    = RawAnimation.begin().thenPlayAndHold("melon_move");
    private static final RawAnimation CHERRY_TICK    = RawAnimation.begin().thenPlayAndHold("cherry_tick");
    private static final RawAnimation CHERRY_START   = RawAnimation.begin().thenPlayAndHold("cherry_start");
    private static final RawAnimation CHERRY_SCATTER = RawAnimation.begin().thenPlayAndHold("cherry_scatter");
    private static final RawAnimation CHERRY_MOVE    = RawAnimation.begin().thenPlayAndHold("cherry_move");
    private static final RawAnimation PEACH_TICK    = RawAnimation.begin().thenPlayAndHold("peach_tick");
    private static final RawAnimation PEACH_START   = RawAnimation.begin().thenPlayAndHold("peach_start");
    private static final RawAnimation PEACH_SCATTER = RawAnimation.begin().thenPlayAndHold("peach_scatter");
    private static final RawAnimation PEACH_MOVE    = RawAnimation.begin().thenPlayAndHold("peach_move");
    private static final RawAnimation DRAGONFRUIT_TICK    = RawAnimation.begin().thenPlayAndHold("dragonfruit_tick");
    private static final RawAnimation DRAGONFRUIT_START   = RawAnimation.begin().thenPlayAndHold("dragonfruit_start");
    private static final RawAnimation DRAGONFRUIT_SCATTER = RawAnimation.begin().thenPlayAndHold("dragonfruit_scatter");
    private static final RawAnimation DRAGONFRUIT_MOVE    = RawAnimation.begin().thenPlayAndHold("dragonfruit_move");

    /* ------------------------- 标签常量 ------------------------- */
    private static final String TAG_BELT_MODE = "BeltMode";
    private static final String TAG_IS_SHOWING = "IsShowing";
    private static final String TAG_IS_ACTIVE = "IsActive";
    private static final String TAG_IS_HENSHIN = "IsHenshin";
    private static final String TAG_IS_RELEASE = "IsRelease";
    private static final String TAG_IS_EQUIPPED = "IsEquipped";
    private static final String TAG_LOCKSEED_ITEM = "LockseedItem";
    private static final String TAG_LOCKSEED_COUNT = "LockseedCount";

    private ItemStack currentStack;

    public enum BeltMode {
        DEFAULT, LEMON, MELON, CHERRY, PEACH, DRAGONFRUIT
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Genesis_driver(Properties properties) {
        super(properties);
    }

    /* ========================= GeoItem ========================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("lemon_tick", LEMON_TICK)
                .triggerableAnim("lemon_move", MOVE)
                .triggerableAnim("scatter", SCATTER)
                .triggerableAnim("start", START)
                .triggerableAnim("show", SHOW)
                .triggerableAnim("idles", IDLES)
                .triggerableAnim("cherry_tick", CHERRY_TICK)
                .triggerableAnim("cherry_start", CHERRY_START)
                .triggerableAnim("cherry_scatter", CHERRY_SCATTER)
                .triggerableAnim("cherry_move", CHERRY_MOVE)
                .triggerableAnim("melon_tick", MELON_TICK)
                .triggerableAnim("melon_start", MELON_START)
                .triggerableAnim("melon_scatter", MELON_SCATTER)
                .triggerableAnim("melon_move", MELON_MOVE)
                .triggerableAnim("peach_tick", PEACH_TICK)
                .triggerableAnim("peach_start", PEACH_START)
                .triggerableAnim("peach_scatter", PEACH_SCATTER)
                .triggerableAnim("peach_move", PEACH_MOVE)
                .triggerableAnim("dragonfruit_tick", DRAGONFRUIT_TICK)
                .triggerableAnim("dragonfruit_start", DRAGONFRUIT_START)
                .triggerableAnim("dragonfruit_scatter", DRAGONFRUIT_SCATTER)
                .triggerableAnim("dragonfruit_move", DRAGONFRUIT_MOVE));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || !(state.getAnimatable() instanceof Genesis_driver))
            return PlayState.STOP;

        BeltMode mode   = getMode(stack);
        boolean showing = getShowing(stack);
        boolean active  = getActive(stack);
        boolean hen     = getHenshin(stack);
        boolean rel     = getRelease(stack);

        String current = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        if (rel) {
            String releaseAnim = switch (mode) {
                case LEMON, MELON, DEFAULT -> "start";
                case CHERRY -> "cherry_start";
                case PEACH  -> "peach_start";
                case DRAGONFRUIT -> "dragonfruit_start";
            };
            if (!current.equals(releaseAnim))
                return state.setAndContinue(getAnimationByName(releaseAnim));

            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setRelease(stack, false);
                setShowing(stack, false);
                setMode(stack, DEFAULT);
                return state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        if (hen) {
            String moveAnim = switch (mode) {
                case LEMON  -> "lemon_move";
                case MELON  -> "melon_move";
                case CHERRY -> "cherry_move";
                case PEACH  -> "peach_move";
                case DRAGONFRUIT -> "dragonfruit_move";
                default     -> "move";
            };
            RawAnimation scatterAnim = switch (mode) {
                case LEMON  -> SCATTER;
                case MELON  -> MELON_SCATTER;
                case CHERRY -> CHERRY_SCATTER;
                case PEACH  -> PEACH_SCATTER;
                case DRAGONFRUIT -> DRAGONFRUIT_SCATTER;
                default     -> SCATTER;
            };

            if (!current.equals(moveAnim) && !current.contains("scatter"))
                return state.setAndContinue(getAnimationByName(moveAnim));

            if (current.equals(moveAnim) && state.getController().getAnimationState() == AnimationController.State.STOPPED)
                return state.setAndContinue(scatterAnim);

            String scatterName = switch (mode) {
                case LEMON  -> "scatter";
                case MELON  -> "melon_scatter";
                case CHERRY -> "cherry_scatter";
                case PEACH  -> "peach_scatter";
                case DRAGONFRUIT -> "dragonfruit_scatter";
                default     -> "scatter";
            };

            if (current.equals(scatterName) &&
                    state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setHenshin(stack, false);
                setShowing(stack, true);
                return state.setAndContinue(SHOW);
            }
            return PlayState.CONTINUE;
        }

        if (showing) {
            if (!"show".equals(current))
                return state.setAndContinue(SHOW);
            return PlayState.CONTINUE;
        }

        if (!"idles".equals(current))
            return state.setAndContinue(IDLES);

        return PlayState.CONTINUE;
    }

    /* =========================================================== */
    /* -------------------- 数据读/写 Helper -------------------- */
    private static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData == CustomData.EMPTY) {
            return new CompoundTag();
        }
        return customData.copyTag();
    }

    private static void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void modifyTag(ItemStack stack, Consumer<CompoundTag> action) {
        CompoundTag tag = getOrCreateTag(stack);
        action.accept(tag);
        saveTag(stack, tag);
    }

    public boolean getEquipped(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_EQUIPPED);
    }

    public void setEquipped(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_EQUIPPED, flag));
    }

    public BeltMode getMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return DEFAULT;
        CompoundTag tag = getOrCreateTag(stack);
        String modeName = tag.getString(TAG_BELT_MODE);
        if (!modeName.isEmpty()) {
            try {
                return BeltMode.valueOf(modeName);
            } catch (IllegalArgumentException ex) {
                return DEFAULT;
            }
        }
        return DEFAULT;
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putString(TAG_BELT_MODE, mode.name()));
    }

    public boolean getShowing(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_SHOWING);
    }

    public void setShowing(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_SHOWING, flag));
    }

    public boolean getActive(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_ACTIVE);
    }

    public void setActive(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_ACTIVE, flag));
    }

    public boolean getHenshin(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_HENSHIN);
    }

    public void setHenshin(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_HENSHIN, flag));
    }

    public boolean getRelease(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_RELEASE);
    }

    public void setRelease(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_RELEASE, flag));
    }

    /* ===================== 业务方法 ==================== */

    /**
     * 触发腰带动画 - 发送网络包到所有玩家
     */
    public void triggerAnim(@Nullable LivingEntity entity, String ctrl, String anim) {
        if (entity == null || entity.level() == null) return;

        // ✅ 服务端：发送网络包
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            BeltMode mode = getMode(getBeltStack(sp));
            com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.BeltAnimationPacket(
                            sp.getId(),
                            anim,
                            "genesisdriver",
                            mode.name()
                    )
            );
        }
    }

    public void startHenshinAnimation(LivingEntity entity, ItemStack stack) {
        setHenshin(stack, true);
        setRelease(stack, false);

        BeltMode mode = getMode(stack);
        String anim = switch (mode) {
            case LEMON  -> "lemon_move";
            case MELON  -> "melon_move";
            case CHERRY -> "cherry_move";
            case PEACH  -> "peach_move";
            case DRAGONFRUIT -> "dragonfruit_move";
            default     -> "move";
        };

        // 1. 服务端：发送腰带动画
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            // 发送腰带动画
            PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new BeltAnimationPacket(sp.getId(), anim, "genesisdriver", mode.name())
            );

            // ✅ 同时播放玩家 sodas 动画（使用 BYAnimationPacket）
            playPlayerAnimation(sp, "sodas");
        }

        // 2. 客户端：本地触发
        if (entity.level().isClientSide()) {
            triggerAnim(entity, "controller", anim);
        }
    }

    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        setRelease(stack, true);
        setHenshin(stack, false);

        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            String anim = switch (getMode(stack)) {
                case MELON  -> "melon_start";
                case LEMON  -> "start";
                case CHERRY -> "cherry_start";
                case PEACH  -> "peach_start";
                case DRAGONFRUIT -> "dragonfruit_start";
                default     -> "start";
            };
            com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler.sendToTrackingAndSelf(
                    sp,
                    new com.xiaoshi2022.kamenriderbossyouandme.network.Drivershenshin.BeltAnimationPacket(
                            sp.getId(), anim, "genesisdriver", getMode(stack).name()
                    )
            );
        }
    }

    /**
     * 先播放玩家 sodax 动画，然后播放解除变身动画
     */
    public void startReleaseWithPlayerAnimation(LivingEntity entity, ItemStack stack) {
        // 1. 首先播放玩家 sodax 动画
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            // ✅ 播放 sodax 玩家动画
            playPlayerAnimation(sp, "sodax");
        }

        // 2. 然后播放解除变身动画
        startReleaseAnimation(entity, stack);
    }

    /* =========================================================== */

    /* -------------------- 锁种 NBT 管理 -------------------- */
    public boolean hasLockseed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = getOrCreateTag(stack);
        return tag.contains(TAG_LOCKSEED_ITEM);
    }

    public void setLockseed(ItemStack stack, ItemStack lockseed) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> {
            if (lockseed == null || lockseed.isEmpty()) {
                tag.remove(TAG_LOCKSEED_ITEM);
                tag.remove(TAG_LOCKSEED_COUNT);
            } else {
                tag.putString(TAG_LOCKSEED_ITEM,
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(lockseed.getItem()).toString());
                tag.putInt(TAG_LOCKSEED_COUNT, lockseed.getCount());
            }
        });
    }

    public ItemStack getLockseed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        CompoundTag tag = getOrCreateTag(stack);
        if (tag.contains(TAG_LOCKSEED_ITEM)) {
            String itemId = tag.getString(TAG_LOCKSEED_ITEM);
            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .get(ResourceLocation.parse(itemId));
            if (item != null && item != Items.AIR) {
                int count = tag.getInt(TAG_LOCKSEED_COUNT);
                if (count <= 0) count = 1;
                return new ItemStack(item, count);
            }
        }
        return ItemStack.EMPTY;
    }

    public void clearLockseed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        modifyTag(stack, tag -> {
            tag.remove(TAG_LOCKSEED_ITEM);
            tag.remove(TAG_LOCKSEED_COUNT);
        });
    }

    /* -------------------- 其它必要实现 -------------------- */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GenesisDriverRenderer renderer;
            @Override
            public @Nullable GeoItemRenderer<Genesis_driver> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new GenesisDriverRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        super.onEquip(ctx, prev, stack);
        if (ctx.entity() instanceof ServerPlayer player) {
            onBeltEquipped(player, stack);
        }
    }

    @Override
    protected void onBeltEquipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null || beltStack.isEmpty()) return;

        setShowing(beltStack, true);
        setActive(beltStack, false);
        setHenshin(beltStack, false);
        setRelease(beltStack, false);

        triggerAnim(player, "controller", "start");

        BeltMode mode = getMode(beltStack);
        if (mode != DEFAULT) {
            setActive(beltStack, true);
        }
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) return;
        if (!(ctx.entity() instanceof LivingEntity)) return;

        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, false);
        setActive(stack, false);

        if (le.level() != null && !le.level().isClientSide() && le instanceof ServerPlayer sp) {
            sp.removeEffect(MobEffects.SATURATION);
        }

        triggerAnim(le, "controller", "idles");
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        // 留空，不需要 sync_state
    }

    private RawAnimation getAnimationByName(String name) {
        return switch (name) {
            case "idles"         -> IDLES;
            case "show"          -> SHOW;
            case "start"         -> START;
            case "scatter"       -> SCATTER;
            case "lemon_move"    -> MOVE;
            case "melon_tick"    -> MELON_TICK;
            case "melon_start"   -> MELON_START;
            case "melon_scatter" -> MELON_SCATTER;
            case "melon_move"    -> MELON_MOVE;
            case "cherry_tick"   -> CHERRY_TICK;
            case "cherry_start"  -> CHERRY_START;
            case "cherry_scatter"-> CHERRY_SCATTER;
            case "cherry_move"   -> CHERRY_MOVE;
            case "peach_tick"    -> PEACH_TICK;
            case "peach_start"   -> PEACH_START;
            case "peach_scatter" -> PEACH_SCATTER;
            case "peach_move"    -> PEACH_MOVE;
            case "dragonfruit_tick"    -> DRAGONFRUIT_TICK;
            case "dragonfruit_start"   -> DRAGONFRUIT_START;
            case "dragonfruit_scatter" -> DRAGONFRUIT_SCATTER;
            case "dragonfruit_move"    -> DRAGONFRUIT_MOVE;
            default              -> IDLES;
        };
    }

    public BeltMode getCurrentMode(ItemStack stack) {
        this.currentStack = stack;
        return getMode(stack);
    }

    public BeltMode getCurrentMode() {
        if (currentStack != null) {
            return getMode(currentStack);
        }
        ItemStack stack = new ItemStack(this);
        return getMode(stack);
    }

    public void setCurrentStack(ItemStack stack) {
        this.currentStack = stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.kamenriderbossyouandme.genesis_driver.desc"));
    }

    /* ========== 玩家动画支持 ========== */
    /* 发送动画到客户端 */
    public static void playPlayerAnimation(ServerPlayer player, String animationName) {
        if (player == null || player.isRemoved()) return;

        // ✅ 使用 BYAnimationPacket 发送玩家动画
        BYAnimationPacket packet = new BYAnimationPacket(player.getUUID(), animationName, 5);

        // 发送给玩家自己
        PacketHandler.sendToClient(player, packet);
        // 发送给所有追踪的玩家（其他人也能看到）
        PacketHandler.sendToAllTracking(player, packet);
    }

    /* 发送动画到客户端（带自定义淡入时间） */
    public static void playPlayerAnimation(ServerPlayer player, String animationName, int fadeDuration) {
        if (player == null || player.isRemoved()) return;

        BYAnimationPacket packet = new BYAnimationPacket(player.getUUID(), animationName, fadeDuration);

        PacketHandler.sendToClient(player, packet);
        PacketHandler.sendToAllTracking(player, packet);
    }

    private static ItemStack getBeltStack(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}