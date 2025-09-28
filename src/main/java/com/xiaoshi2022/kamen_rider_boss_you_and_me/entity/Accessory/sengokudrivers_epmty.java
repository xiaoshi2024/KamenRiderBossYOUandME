package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.sengokudriver.sengokudrivers_epmtysRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class sengokudrivers_epmty extends Item implements GeoItem, ICurioItem {

    /* ----------------- 动画常量 ----------------- */
    private static final RawAnimation IDLES          = RawAnimation.begin().thenPlayAndHold("idles");
    private static final RawAnimation SHOW           = RawAnimation.begin().thenPlayAndHold("show");
    private static final RawAnimation BANANA_IDLE    = RawAnimation.begin().thenPlayAndHold("banana_idle");
    private static final RawAnimation CUT            = RawAnimation.begin().thenPlayAndHold("cut");
    private static final RawAnimation RELEASE        = RawAnimation.begin().thenPlayAndHold("release");
    private static final RawAnimation ORANGELS_CUT   = RawAnimation.begin().thenPlayAndHold("orangels_cut");
    private static final RawAnimation ORANGELS_RELEASE = RawAnimation.begin().thenPlayAndHold("orangels_release");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public void setEquipped(ItemStack stack, boolean flag) {
        stack.getOrCreateTag().putBoolean("IsEquipped", flag);
    }

    public void setHenshin(ItemStack stack, boolean flag) {
        stack.getOrCreateTag().putBoolean("IsHenshin", flag);
    }

    public void setBeltMode(ItemStack beltStack, BeltMode beltMode) {
        beltStack.getOrCreateTag().putString("BeltMode", beltMode.name());
    }

    public enum BeltMode {
        DEFAULT, BANANA, ORANGELS
    }

    public sengokudrivers_epmty(Properties properties) {
        super(properties);
    }

    /* ================= GeoItem ================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("show",  SHOW)
                .triggerableAnim("idles", IDLES)
                .triggerableAnim("banana_idle", BANANA_IDLE)
                .triggerableAnim("cut",   CUT)
                .triggerableAnim("release", RELEASE)
                .triggerableAnim("orangels_cut", ORANGELS_CUT)
                .triggerableAnim("orangels_release", ORANGELS_RELEASE));

    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (!(state.getAnimatable() instanceof sengokudrivers_epmty)) return PlayState.STOP;

        BeltMode mode   = getMode(stack);
        boolean show    = getShowing(stack);
        boolean release = getRelease(stack);
        boolean hen     = getHenshin(stack);

        String cur = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        /* -------- 变身序列 -------- */
        if (hen) {
            String moveAnim = switch (mode) {
                case BANANA   -> "cut";
                case ORANGELS -> "orangels_cut";
                default       -> "cut";
            };
            if (!cur.equals(moveAnim))
                return state.setAndContinue(getAnim(moveAnim));
            return PlayState.CONTINUE;
        }

        /* -------- 解除变身 -------- */
        if (release) {
            String releaseAnim = switch (mode) {
                case BANANA   -> "release";
                case ORANGELS -> "orangels_release";
                default       -> "release";
            };
            if (!cur.equals(releaseAnim))
                return state.setAndContinue(getAnim(releaseAnim));

            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setRelease(stack, false);
                setShowing(stack, false);
                setMode(stack, BeltMode.DEFAULT);
                return state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        /* -------- 展示/待机 -------- */
        if (show) {
            String idleAnim = switch (mode) {
                case BANANA   -> "banana_idle";
                case ORANGELS -> "show";  // ORANGELS模式使用默认展示动画
                default       -> "show";
            };
            if (!cur.equals(idleAnim)) return state.setAndContinue(getAnim(idleAnim));
            return PlayState.CONTINUE;
        }

        /* -------- 默认 -------- */
        if (!cur.equals("idles")) return state.setAndContinue(IDLES);
        return PlayState.CONTINUE;
    }

    public boolean getHenshin(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("IsHenshin");
    }

    /* -------------- NBT 工具 -------------- */
    public BeltMode getMode(ItemStack stack) {
        try {
            return BeltMode.valueOf(stack.getOrCreateTag().getString("BeltMode"));
        } catch (IllegalArgumentException e) {
            return BeltMode.DEFAULT;
        }
    }

    public void setModeAndTriggerCut(LivingEntity entity, ItemStack stack, BeltMode mode) {
        setMode(stack, mode);
        String anim = switch (mode) {
            case BANANA   -> "cut";
            case ORANGELS -> "orangels_cut";
            default       -> "cut";
        };
        triggerAnim(entity, "controller", anim);
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        stack.getOrCreateTag().putString("BeltMode", mode.name());
    }

    public boolean getShowing(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("IsShowing");
    }
    public void setShowing(ItemStack stack, boolean v) {
        stack.getOrCreateTag().putBoolean("IsShowing", v);
    }

    public boolean getRelease(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("IsRelease");
    }
    public void setRelease(ItemStack stack, boolean v) {
        stack.getOrCreateTag().putBoolean("IsRelease", v);
    }

    /* -------------- 业务 -------------- */
    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        setRelease(stack, true);
        setShowing(stack, false);

        String anim = switch (getMode(stack)) {
            case BANANA   -> "release";
            case ORANGELS -> "orangels_release";
            default       -> "release";
        };

        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp)
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), anim, getMode(stack)), sp);

        triggerAnim(entity, "controller", anim);
    }

    /* -------------- 工具 -------------- */
    private RawAnimation getAnim(String name) {
        return switch (name) {
            case "show"            -> SHOW;
            case "idles"           -> IDLES;
            case "banana_idle"     -> BANANA_IDLE;
            case "cut"             -> CUT;
            case "release"         -> RELEASE;
            case "orangels_cut"    -> ORANGELS_CUT;
            case "orangels_release"-> ORANGELS_RELEASE;
            default                -> IDLES;
        };
    }

    /* -------------- Curio -------------- */
    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        if (!(ctx.entity() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, true);
        setRelease(stack, false);

        String anim = switch (getMode(stack)) {
            case BANANA   -> "banana_idle";
            case ORANGELS -> "show";  // ORANGELS模式使用默认展示动画
            default       -> "show";
        };

        if (!le.level().isClientSide() && le instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), anim, getMode(stack)), sp);
            // 给玩家添加饱和效果
            sp.addEffect(new MobEffectInstance(MobEffects.SATURATION, Integer.MAX_VALUE, 0, true, false));
        }

        triggerAnim(le, "controller", anim);
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (!(ctx.entity() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, false);
        setRelease(stack, false);

        if (!le.level().isClientSide() && le instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), "idles", getMode(stack)), sp);
            // 移除玩家的饱和效果
            sp.removeEffect(MobEffects.SATURATION);
        }

        triggerAnim(le, "controller", "idles");
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (ctx.entity().level().isClientSide()) return;
        if (!(ctx.entity() instanceof ServerPlayer sp)) return;

        // 1 秒同步一次
        if (sp.tickCount % 20 == 0) {
            PacketHandler.sendToClient(
                    new BeltAnimationPacket(sp.getId(), "sync_state", getMode(stack)), sp);
        }

        // 每5秒检查一次并重新应用饱和效果，确保效果持续存在
        if (sp.tickCount % 100 == 0) {
            if (!sp.hasEffect(MobEffects.SATURATION)) {
                sp.addEffect(new MobEffectInstance(MobEffects.SATURATION, Integer.MAX_VALUE, 0, true, false));
            }
        }
    }

    /* -------------- 同步 -------------- */
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        if (tag == null) tag = new CompoundTag();
        tag.putString("BeltMode", getMode(stack).name());
        tag.putBoolean("IsShowing", getShowing(stack));
        tag.putBoolean("IsRelease", getRelease(stack));
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt == null) return;
        if (nbt.contains("BeltMode"))  setMode(stack, BeltMode.valueOf(nbt.getString("BeltMode")));
        if (nbt.contains("IsShowing")) setShowing(stack, nbt.getBoolean("IsShowing"));
        if (nbt.contains("IsRelease")) setRelease(stack, nbt.getBoolean("IsRelease"));
    }

    /* -------------- 客户端渲染器 -------------- */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private sengokudrivers_epmtysRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new sengokudrivers_epmtysRenderer();
                return renderer;
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /* -------------- 触发工具 -------------- */
    public void triggerAnim(LivingEntity entity, String ctrl, String anim) {
        if (entity == null || entity.level() == null) return;
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            // 从Curio槽位获取腰带模式
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(sp).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof sengokudrivers_epmty));
            sengokudrivers_epmty.BeltMode mode = beltOptional.map(result -> getMode(result.stack())).orElse(BeltMode.DEFAULT);
            
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), anim, mode), entity);
        }
    }
}