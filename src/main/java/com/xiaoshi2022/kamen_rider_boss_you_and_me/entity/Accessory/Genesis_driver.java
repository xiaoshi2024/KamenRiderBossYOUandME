package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver.GenesisDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver.BeltMode.*;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class Genesis_driver extends Item implements GeoItem, ICurioItem {

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

    public boolean getEquipped(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("IsEquipped");
    }

    public void setEquipped(ItemStack stack, boolean flag) {
        stack.getOrCreateTag().putBoolean("IsEquipped", flag);
    }

    /* ----------------------------------------------------------- */

    public enum BeltMode {
        DEFAULT, LEMON, MELON, CHERRY, PEACH, DRAGONFRUIT
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Genesis_driver(Properties properties) {
        super(properties);
//        SingletonGeoAnimatable.registerSyncedAnimatable(this);
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

    /* 读取实时 NBT 状态，不再使用任何字段 */
    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (!(state.getAnimatable() instanceof Genesis_driver))
            return PlayState.STOP;

        BeltMode mode   = getMode(stack);
        boolean showing = getShowing(stack);
        boolean active  = getActive(stack);
        boolean hen     = getHenshin(stack);
        boolean rel     = getRelease(stack);

        String current = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        /* -------- 解除变身 -------- */
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

        /* -------- 变身序列 -------- */
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

        /* -------- 展示 -------- */
        if (showing) {
            if (!"show".equals(current))
                return state.setAndContinue(SHOW);
            return PlayState.CONTINUE;
        }

        /* -------- 空闲 -------- */
        if (!"idles".equals(current))
            return state.setAndContinue(IDLES);

        return PlayState.CONTINUE;
    }

    /* =========================================================== */
    /* -------------------- 数据读/写 Helper -------------------- */
    public BeltMode getMode(ItemStack stack) {
        String key = stack.getOrCreateTag().getString("BeltMode");
        if (key.isEmpty()) return DEFAULT;        // ← 兜底
        try {
            return BeltMode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return DEFAULT;                       // ← 防止未来拼写错误
        }
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        stack.getOrCreateTag().putString("BeltMode", mode.name());
    }

    public boolean getShowing(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("IsShowing");
    }

    public void setShowing(ItemStack stack, boolean flag) {
        stack.getOrCreateTag().putBoolean("IsShowing", flag);
    }

    public boolean getActive(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("IsActive");
    }

    public void setActive(ItemStack stack, boolean flag) {
        stack.getOrCreateTag().putBoolean("IsActive", flag);
    }

    public boolean getHenshin(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("IsHenshin");
    }

    public void setHenshin(ItemStack stack, boolean flag) {
        stack.getOrCreateTag().putBoolean("IsHenshin", flag);
    }

    public boolean getRelease(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("IsRelease");
    }

    public void setRelease(ItemStack stack, boolean flag) {
        stack.getOrCreateTag().putBoolean("IsRelease", flag);
    }

    /* ----------------------------------------------------------- */

    /* ===================== 业务方法（无字段） ==================== */
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

        System.out.println(">>> Server send packet: " + anim);

        // 1. 服务端：把动画名同步给所有追踪者
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), anim, mode), sp);
        }

        // 2. 客户端：本地线程直接播，不再发包
        if (entity.level().isClientSide) {
            triggerAnim(entity, "controller", anim);
        }
    }

    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        setRelease(stack, true);
        setHenshin(stack, false);

        String anim = switch (getMode(stack)) {
            case MELON  -> "melon_start";
            case LEMON  -> "start";
            case CHERRY -> "cherry_start";
            case PEACH  -> "peach_start";
            case DRAGONFRUIT -> "dragonfruit_start";
            default     -> "start";
        };

        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), anim, getMode(stack)), sp);
        }
        triggerAnim(entity, "controller", anim);
    }

    /* =========================================================== */

    /* -------------------- 其它必要实现 -------------------- */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GenesisDriverRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new GenesisDriverRenderer();
                return renderer;
            }
        });
    }

    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        if (!(ctx.entity() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, true);
        setActive(stack, false);
        if (!le.level().isClientSide() && le instanceof ServerPlayer sp)
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), "show", getMode(stack)), sp);
        triggerAnim(le, "controller", "show");
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (!(ctx.entity() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, false);
        setActive(stack, false);
        if (!le.level().isClientSide() && le instanceof ServerPlayer sp)
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), "idles", getMode(stack)), sp);
        triggerAnim(le, "controller", "idles");
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (ctx.entity().level().isClientSide()) return;
        if (!(ctx.entity() instanceof ServerPlayer sp)) return;

        // 每 5 秒同步一次，避免频繁刷新
        if (sp.tickCount % 100 == 0) {
            PacketHandler.sendToClient(
                    new BeltAnimationPacket(sp.getId(), "sync_state", getMode(stack)), sp);
        }
    }

    /* -------------------- NBT 同步 -------------------- */
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        if (tag == null) tag = new CompoundTag();
        tag.putString("BeltMode", getMode(stack).name());
        tag.putBoolean("IsShowing", getShowing(stack));
        tag.putBoolean("IsActive", getActive(stack));
        tag.putBoolean("IsHenshin", getHenshin(stack));
        tag.putBoolean("IsRelease", getRelease(stack));
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt == null) return;
        if (nbt.contains("BeltMode"))   setMode(stack, BeltMode.valueOf(nbt.getString("BeltMode")));
        if (nbt.contains("IsShowing"))  setShowing(stack, nbt.getBoolean("IsShowing"));
        if (nbt.contains("IsActive"))   setActive(stack, nbt.getBoolean("IsActive"));
        if (nbt.contains("IsHenshin"))  setHenshin(stack, nbt.getBoolean("IsHenshin"));
        if (nbt.contains("IsRelease"))  setRelease(stack, nbt.getBoolean("IsRelease"));
    }

    /* -------------------- 动画触发工具 -------------------- */
    public void triggerAnim(@Nullable LivingEntity entity, String ctrl, String anim) {
        if (entity == null || entity.level() == null) return;
        if (entity instanceof ServerPlayer sp)
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), anim, getMode(sp.getMainHandItem())), entity);
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
}