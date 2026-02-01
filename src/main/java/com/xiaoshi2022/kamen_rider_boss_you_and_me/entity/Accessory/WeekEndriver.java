package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.weekendriver.WeekEndriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.WeekEndriver.BeltMode.DEFAULT;
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.WeekEndriver.BeltMode.QUEEN_BEE;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class WeekEndriver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    /* ------------------------- 动画常量 ------------------------- */
    private static final RawAnimation IDLES   = RawAnimation.begin().thenPlayAndHold("idles");
    private static final RawAnimation SHOW    = RawAnimation.begin().thenPlayAndHold("show");
    private static final RawAnimation HENSHIN_A = RawAnimation.begin().thenPlayAndHold("henshin-a");
    private static final RawAnimation HENSHIN_B = RawAnimation.begin().thenPlayAndHold("henshin-b");
    private static final RawAnimation CANCEL  = RawAnimation.begin().thenPlayAndHold("cancel");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public enum BeltMode {
        DEFAULT, QUEEN_BEE
    }

    public WeekEndriver(Properties properties) {
        super(properties);
    }

    /* ========================= GeoItem ========================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("show", SHOW)
                .triggerableAnim("idles", IDLES)
                .triggerableAnim("henshin-a", HENSHIN_A)
                .triggerableAnim("henshin-b", HENSHIN_B)
                .triggerableAnim("cancel", CANCEL));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null || !(state.getAnimatable() instanceof WeekEndriver))
            return PlayState.STOP;

        BeltMode mode = getMode(stack);
        boolean showing = getShowing(stack);
        boolean active = getActive(stack);
        boolean hen = getHenshin(stack);
        boolean rel = getRelease(stack);
        boolean shouldPlayHenshinB = getShouldPlayHenshinB(stack); // 新增：是否需要播放henshin-b

        String current = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        /* -------- 解除变身 -------- */
        if (rel) {
            if (!current.equals("cancel"))
                return state.setAndContinue(CANCEL);

            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setRelease(stack, false);
                setShowing(stack, false);
                setMode(stack, DEFAULT);
                setShouldPlayHenshinB(stack, false); // 重置状态
                return state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        /* -------- 变身序列 -------- */
        if (hen) {
            // 如果需要播放henshin-b
            if (shouldPlayHenshinB) {
                if (!current.equals("henshin-b"))
                    return state.setAndContinue(HENSHIN_B);

                // 保持henshin-b动画
                if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    return state.setAndContinue(HENSHIN_B); // 保持henshin-b
                }
                return PlayState.CONTINUE;
            }
            // 否则播放henshin-a
            else {
                if (!current.equals("henshin-a"))
                    return state.setAndContinue(HENSHIN_A);

                // 保持henshin-a动画（等待X键触发henshin-b）
                if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    return state.setAndContinue(HENSHIN_A); // 保持henshin-a
                }
                return PlayState.CONTINUE;
            }
        }

        /* -------- 展示 -------- */
        if (showing) {
            // 如果腰带模式是女王蜂，保持henshin-a动画（等待X键触发）
            if (mode == BeltMode.QUEEN_BEE) {
                if (!"henshin-a".equals(current))
                    return state.setAndContinue(HENSHIN_A);
                return PlayState.CONTINUE;
            } else {
                // 其他模式使用默认的show动画
                if (!"show".equals(current))
                    return state.setAndContinue(SHOW);
                return PlayState.CONTINUE;
            }
        }

        /* -------- 空闲 -------- */
        if (!"idles".equals(current))
            return state.setAndContinue(IDLES);

        return PlayState.CONTINUE;
    }

    public boolean getShouldPlayHenshinB(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("ShouldPlayHenshinB") ? tag.getBoolean("ShouldPlayHenshinB") : false;
    }

    public void setShouldPlayHenshinB(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("ShouldPlayHenshinB", flag);
    }

    /* =========================================================== */
    /* -------------------- 数据读/写 Helper -------------------- */
    public BeltMode getMode(ItemStack stack) {
        if (stack == null) {
            return DEFAULT;
        }
        CompoundTag tag = stack.getOrCreateTag();
        String modeName = tag.getString("BeltMode");
        if (modeName.isEmpty()) {
            return DEFAULT;
        }
        try {
            return BeltMode.valueOf(modeName);
        } catch (IllegalArgumentException ex) {
            return DEFAULT;
        }
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putString("BeltMode", mode.name());
    }

    public boolean getShowing(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsShowing") ? tag.getBoolean("IsShowing") : false;
    }

    public void setShowing(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsShowing", flag);
    }

    public boolean getActive(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsActive") ? tag.getBoolean("IsActive") : false;
    }

    public void setActive(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsActive", flag);
    }

    public boolean getHenshin(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsHenshin") ? tag.getBoolean("IsHenshin") : false;
    }

    public void setHenshin(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsHenshin", flag);
    }

    public boolean getRelease(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains("IsRelease") ? tag.getBoolean("IsRelease") : false;
    }

    public void setRelease(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsRelease", flag);
    }

    /* ----------------------------------------------------------- */

    /* ===================== 业务方法 ==================== */
    public void startHenshinAnimation(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack == null || entity.level() == null) {
            return;
        }

        setHenshin(stack, true);
        setShouldPlayHenshinB(stack, false); // 设置为播放henshin-a
        setRelease(stack, false);

        BeltMode mode = getMode(stack);
        String anim = "henshin-a";

        System.out.println(">>> Server send packet: " + anim);

        // 1. 服务端：把腰带动画名同步给所有追踪者
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), anim, mode), sp);
        }

        // 2. 客户端：本地线程直接播，不再发包
        if (entity.level().isClientSide) {
            triggerAnim(entity, "controller", anim);
        }
    }

    public void startHenshinBAnimation(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack == null || entity.level() == null) {
            return;
        }

        // ===== 关键修复：设置Henshin状态为true，并标记播放henshin-b =====
        setHenshin(stack, true);
        setShouldPlayHenshinB(stack, true); // 设置为播放henshin-b
        setRelease(stack, false);

        BeltMode mode = getMode(stack);
        String anim = "henshin-b";

        System.out.println(">>> Server send packet for HENSHIN-B: " + anim);

        // 1. 服务端：把腰带动画名同步给所有追踪者
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
        if (entity == null || stack == null || entity.level() == null) {
            return;
        }
        
        setRelease(stack, true);
        setHenshin(stack, false);

        BeltMode mode = getMode(stack);
        String anim = "cancel";

        System.out.println(">>> Server send packet: " + anim);

        // 1. 服务端：把腰带动画名同步给所有追踪者
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), anim, mode), sp);
        }

        // 2. 客户端：本地线程直接播，不再发包
        if (entity.level().isClientSide) {
            triggerAnim(entity, "controller", anim);
        }
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
            private WeekEndriverRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new WeekEndriverRenderer();
                return renderer;
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
    
    /**
     * 实现基类的腰带装备逻辑
     */
    @Override
    protected void onBeltEquipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null) {
            return;
        }
        
        setShowing(beltStack, true);
        setActive(beltStack, false);
        setHenshin(beltStack, false);
        setRelease(beltStack, false);

        // 同步腰带状态到所有跟踪的玩家
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "show", DEFAULT),
                player);
        
        // 更新玩家变量
        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            variables.isWeekEndriverEquipped = true;
            variables.syncPlayerVariables(player);
        });

        // 触发动画
        triggerAnim(player, "controller", "show");
        
        // 检查腰带是否已经有女王蜂形态
        BeltMode mode = getMode(beltStack);
        if (mode != DEFAULT) {
            // 设置准备变身状态
            setActive(beltStack, true);
            
            // 获取玩家变量
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                variables.queenBee_ready = true;
                variables.syncPlayerVariables(player);
            });
            
            // 触发henshin-a动画并保持
            triggerAnim(player, "controller", "henshin-a");
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(player.getId(), "henshin-a", QUEEN_BEE),
                    player);
            
            // 通知玩家准备好变身
            player.sendSystemMessage(
                    Component.literal("腰带已准备好变身！请按 X 键完成变身过程")
            );
        }
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null) {
            return;
        }
        
        if (!(ctx.entity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, false);
        setActive(stack, false);
        
        if (le.level() != null && !le.level().isClientSide && le instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), "idles", getMode(stack)), sp);
        }
        
        triggerAnim(le, "controller", "idles");
        
        // 更新玩家变量
        if (le instanceof ServerPlayer player) {
            player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
                variables.isWeekEndriverEquipped = false;
                variables.syncPlayerVariables(player);
            });
        }
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null) {
            return;
        }
        if (ctx.entity().level() == null || ctx.entity().level().isClientSide()) {
            return;
        }
        if (!(ctx.entity() instanceof ServerPlayer sp)) {
            return;
        }

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
        tag.putBoolean("ShouldPlayHenshinB", getShouldPlayHenshinB(stack)); // 新增
        return tag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt == null) return;
        if (nbt.contains("BeltMode")) setMode(stack, BeltMode.valueOf(nbt.getString("BeltMode")));
        if (nbt.contains("IsShowing")) setShowing(stack, nbt.getBoolean("IsShowing"));
        if (nbt.contains("IsActive")) setActive(stack, nbt.getBoolean("IsActive"));
        if (nbt.contains("IsHenshin")) setHenshin(stack, nbt.getBoolean("IsHenshin"));
        if (nbt.contains("IsRelease")) setRelease(stack, nbt.getBoolean("IsRelease"));
        if (nbt.contains("ShouldPlayHenshinB")) setShouldPlayHenshinB(stack, nbt.getBoolean("ShouldPlayHenshinB")); // 新增
    }

    /* -------------------- 动画触发工具 -------------------- */
    public void triggerAnim(@Nullable LivingEntity entity, String ctrl, String anim) {
        if (entity == null || entity.level() == null) return;
        
        // 服务端处理：发送动画包到所有跟踪者
        if (!entity.level().isClientSide) {
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(entity).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof WeekEndriver));
            BeltMode mode = beltOptional.map(result -> getMode(result.stack())).orElse(DEFAULT);
            
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), anim, mode), entity);
        }
    }
}