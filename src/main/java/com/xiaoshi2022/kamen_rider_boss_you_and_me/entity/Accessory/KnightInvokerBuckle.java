package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.ghostdriver.GhostDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.knightinvoker.KnightInvokerBuckleRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.CurioUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class KnightInvokerBuckle extends AbstractRiderBelt implements GeoItem, ICurioItem {

    /* ----------------- 动画常量 ----------------- */
    private static final RawAnimation IDLES = RawAnimation.begin().thenPlayAndHold("idles");
    private static final RawAnimation SHOW = RawAnimation.begin().thenPlayAndHold("show");
    private static final RawAnimation SPIN = RawAnimation.begin().thenPlayAndHold("spin");
    private static final RawAnimation PRESS = RawAnimation.begin().thenPlayAndHold("press");
    private static final RawAnimation CANCEL = RawAnimation.begin().thenPlayAndHold("cancel");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public boolean getEquipped(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getOrCreateTag().getBoolean("IsEquipped");
    }

    public void setEquipped(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsEquipped", flag);
    }

    public enum BeltMode {
        DEFAULT, NOX
    }

    public KnightInvokerBuckle(Properties properties) {
        super(properties);
    }
    
    /**
     * 右键使用腰带直接装备到背部槽位
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            // 在客户端不执行实际装备逻辑
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack beltStack = player.getItemInHand(hand).copy();

        // 检查玩家是否已经装备了腰带
        Optional<SlotResult> existingBeltOpt = CurioUtils.findFirstCurio(serverPlayer, 
                stack -> stack.getItem() instanceof KnightInvokerBuckle);

        // 如果已有腰带，将其替换回背包
        if (existingBeltOpt.isPresent()) {
            SlotResult slotResult = existingBeltOpt.get();
            ItemStack existingBelt = slotResult.stack();
            
            // 尝试将原有腰带添加到玩家背包
            if (!serverPlayer.getInventory().add(existingBelt)) {
                // 如果背包满了，将腰带掉落在地上
                serverPlayer.drop(existingBelt, false);
            }
            
            // 清空原有腰带槽位
            CuriosApi.getCuriosInventory(serverPlayer).ifPresent(inv -> {
                inv.getStacksHandler(slotResult.slotContext().identifier()).ifPresent(handler -> {
                    handler.getStacks().setStackInSlot(slotResult.slotContext().index(), ItemStack.EMPTY);
                    handler.update();
                });
            });
        }

        // 装备新腰带到背部槽位
        CurioUtils.forceEquipToBack(serverPlayer, beltStack);
        
        // 从玩家手中移除腰带
        if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
        }
        
        // 触发腰带装备后的自定义逻辑
        onBeltEquipped(serverPlayer, beltStack);
        
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    /* -------------------- 数据读/写 Helper -------------------- */
    public BeltMode getMode(ItemStack stack) {
        if (stack == null) {
            return BeltMode.DEFAULT;
        }
        String key = stack.getOrCreateTag().getString("BeltMode");
        if (key.isEmpty()) return BeltMode.DEFAULT;
        try {
            return BeltMode.valueOf(key);
        } catch (IllegalArgumentException ex) {
            return BeltMode.DEFAULT;
        }
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putString("BeltMode", mode.name());
        
        // 更新物品显示名称
        String modeText = switch (mode) {
            case NOX -> "暗夜祷告驱动器 - Nox Knight形态";
            default -> "暗夜祷告驱动器 - 普通形态";
        };
        
        stack.setHoverName(Component.literal(modeText));
    }

    public boolean getShowing(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getOrCreateTag().getBoolean("IsShowing");
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
        return stack.getOrCreateTag().getBoolean("IsActive");
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
        return stack.getOrCreateTag().getBoolean("IsHenshin");
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
        return stack.getOrCreateTag().getBoolean("IsRelease");
    }

    public void setRelease(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsRelease", flag);
    }
    
    public boolean getPressState(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getOrCreateTag().getBoolean("IsPressed");
    }
    
    public void setPressState(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsPressed", flag);
    }
    /* ----------------------------------------------------------- */
    
    @Override
    public boolean isFoil(ItemStack stack) {
        // 根据腰带模式显示不同光效
        BeltMode mode = getMode(stack);
        return mode == BeltMode.NOX;
    }

    /* ================= GeoItem ================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("show", SHOW)
                .triggerableAnim("idles", IDLES)
                .triggerableAnim("spin", SPIN)
                .triggerableAnim("press", PRESS)
                .triggerableAnim("cancel", CANCEL));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (!(state.getAnimatable() instanceof KnightInvokerBuckle)) return PlayState.STOP;

        BeltMode mode = getMode(stack);
        boolean show = getShowing(stack);
        boolean release = getRelease(stack);
        boolean hen = getHenshin(stack);
        boolean active = getActive(stack);

        String cur = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        /* -------- 解除变身 -------- */
        if (release) {
            if (!cur.equals("cancel"))
                return state.setAndContinue(CANCEL);

            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setRelease(stack, false);
                setShowing(stack, false);
                setMode(stack, BeltMode.DEFAULT);
                return state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        /* -------- 变身序列 -------- */
        if (hen) {
            if (!cur.equals("spin"))
                return state.setAndContinue(SPIN);
            
            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                setHenshin(stack, false);
                setShowing(stack, true);
                return state.setAndContinue(SHOW);
            }
            return PlayState.CONTINUE;
        }

        /* -------- Press动画 -------- */
        boolean pressed = getPressState(stack);
        if (pressed) {
            if (!cur.equals("press")) {
                // 播放Press动画
                return state.setAndContinue(PRESS);
            }

            // 动画播放完毕后不自动重置Press状态，保持为true直到玩家按下X键触发变身
            if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                // 回到show或idles状态，但保持Press状态为true
                return show ? state.setAndContinue(SHOW) : state.setAndContinue(IDLES);
            }
            return PlayState.CONTINUE;
        }

        /* -------- 展示/待机 -------- */
        if (show) {
            if (!cur.equals("show")) return state.setAndContinue(SHOW);
            return PlayState.CONTINUE;
        }

        /* -------- 默认 -------- */
        if (!cur.equals("idles")) return state.setAndContinue(IDLES);
        return PlayState.CONTINUE;
    }

    /* -------------- 业务方法 -------------- */
    public void startHenshinAnimation(LivingEntity entity, ItemStack stack) {
        setHenshin(stack, true);
        setRelease(stack, false);
        setPressState(stack, false); // 重置Press状态，避免多次按X键导致音效堆叠
    }

    /**
     * 播放Press动画和音效
     */
    public void startPressAnimation(LivingEntity entity, ItemStack stack) {
        // 设置Press状态为true，这样动画控制器可以正确响应
        setPressState(stack, true);

        // 播放Press动画
        String anim = "press";

        BeltMode mode = getMode(stack);

        // 触发动画
        this.triggerAnim(entity, "controller", anim);
    }

    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        setRelease(stack, true);
        setHenshin(stack, false);
        setPressState(stack, false);

        String anim = "cancel";

        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), anim, "knight_invoker", getMode(stack).name()), sp);
        }
        triggerAnim(entity, "controller", anim);
    }

    /* -------------- 物品提示 -------------- */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        // 添加当前腰带形态提示
        BeltMode mode = getMode(stack);
        String modeText = switch (mode) {
            case NOX -> Component.translatable("tooltip.knight_invoker.mode.nox").getString();
            default -> Component.translatable("tooltip.knight_invoker.mode.normal").getString();
        };
        tooltipComponents.add(Component.translatable("tooltip.knight_invoker.mode", modeText));
    }

    /* -------------- Curio -------------- */
    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        super.onEquip(ctx, prev, stack);
        // 确保实体是ServerPlayer类型
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
        
        setHenshin(beltStack, false);
        setRelease(beltStack, false);
        setShowing(beltStack, true);
        setActive(beltStack, false);

        // 更新玩家变量，标记装备了KnightInvokerBuckle
        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            variables.isKnightInvokerEquipped = true;
            variables.syncPlayerVariables(player);
        });

        // 触发动画
        triggerAnim(player, "controller", "show");
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null) {
            return;
        }
        
        super.onUnequip(ctx, prev, stack);
        if (ctx.entity() instanceof ServerPlayer player) {
            onBeltUnequipped(player, stack);
        }
    }

    /**
     * 实现基类的腰带卸下逻辑
     */
    protected void onBeltUnequipped(ServerPlayer player, ItemStack beltStack) {
        if (player == null || beltStack == null) {
            return;
        }
        
        setShowing(beltStack, false);
        setRelease(beltStack, false);
        setPressState(beltStack, false); // 重置Press状态，避免下次装备时状态异常
        
        // 更新玩家变量，标记卸下了KnightInvokerBuckle
        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            variables.isKnightInvokerEquipped = false;
            variables.syncPlayerVariables(player);
        });
        
        // 触发动画
        triggerAnim(player, "controller", "idles");
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
                    new BeltAnimationPacket(sp.getId(), "sync_state", "knight_invoker", getMode(stack).name()), sp);
        }
    }

    /* -------------- 同步 -------------- */
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        if (tag == null) tag = new CompoundTag();
        tag.putString("BeltMode", getMode(stack).name());
        tag.putBoolean("IsShowing", getShowing(stack));
        tag.putBoolean("IsActive", getActive(stack));
        tag.putBoolean("IsHenshin", getHenshin(stack));
        tag.putBoolean("IsRelease", getRelease(stack));
        tag.putBoolean("IsPressed", getPressState(stack)); // 添加IsPressed状态同步
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
        if (nbt.contains("IsPressed")) setPressState(stack, nbt.getBoolean("IsPressed")); // 添加IsPressed状态同步
    }

    /* -------------- 客户端渲染器 -------------- */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private KnightInvokerBuckleRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new KnightInvokerBuckleRenderer();
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
        
        // 服务端：发送动画数据包给所有追踪者
        if (entity instanceof ServerPlayer sp) {
            // 从Curio槽位获取腰带模式
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(sp).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof KnightInvokerBuckle));
            KnightInvokerBuckle.BeltMode mode = beltOptional.map(result -> getMode(result.stack())).orElse(BeltMode.DEFAULT);

            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), anim, "knight_invoker", mode.name()), entity);
        }
    }

    /**
     * 开始Erase动画和音效
     */
    public void startEraseAnimation(LivingEntity entity, ItemStack stack) {
        setHenshin(stack, false);
        setPressState(stack, true);
        String anim = "press";
        // Play erase sound
        if (!entity.level().isClientSide()) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModBossSounds.ERASE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        // Send animation packet
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), anim, "knight_invoker", getMode(stack).name()), sp);
        }
        triggerAnim(entity, "controller", anim);
    }
}