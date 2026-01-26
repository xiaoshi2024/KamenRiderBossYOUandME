package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.braindriver.BrainDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
public class BrainDriver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    /* ----------------- 动画常量 ----------------- */
    private static final RawAnimation IDLES = RawAnimation.begin().thenPlayAndHold("idles");
    private static final RawAnimation SHOW = RawAnimation.begin().thenPlayAndHold("show");
    private static final RawAnimation HENSHIN = RawAnimation.begin().thenPlayAndHold("henshin");
    private static final RawAnimation CANCEL = RawAnimation.begin().thenPlayAndHold("cancel");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public void setEquipped(ItemStack stack, boolean flag) {
        stack.getOrCreateTag().putBoolean("IsEquipped", flag);
    }

    public enum BeltMode {
        DEFAULT,
        BRAIN
    }

    public BrainDriver(Properties properties) {
        super(properties);
    }

    /* -------------------- 数据读/写 Helper -------------------- */
    /**
     * 获取腰带的模式
     */
    public BeltMode getMode(ItemStack stack) {
        if (stack == null) {
            return BeltMode.DEFAULT;
        }
        CompoundTag tag = stack.getOrCreateTag();
        String modeName = tag.getString("BeltMode");
        if (modeName.isEmpty()) {
            return BeltMode.DEFAULT;
        }
        try {
            return BeltMode.valueOf(modeName);
        } catch (IllegalArgumentException e) {
            return BeltMode.DEFAULT;
        }
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
    
    @Override
    public boolean isFoil(ItemStack stack) {
        // 根据腰带模式显示不同光效
        BeltMode mode = getMode(stack);
        return mode == BeltMode.BRAIN;
    }

    /* ================= GeoItem ================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("show", SHOW)
                .triggerableAnim("idles", IDLES)
                .triggerableAnim("henshin", HENSHIN)
                .triggerableAnim("cancel", CANCEL));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        // 添加null检查
        if (stack == null || !(state.getAnimatable() instanceof BrainDriver)) return PlayState.STOP;

        BeltMode mode = getMode(stack);
        boolean show = getShowing(stack);
        boolean release = getRelease(stack);
        boolean hen = getHenshin(stack);
        boolean active = getActive(stack);

        String cur = state.getController().getCurrentAnimation() == null
                ? "" : state.getController().getCurrentAnimation().animation().name();

        /* -------- 变身序列 -------- */
        if (hen) {
            if (!cur.equals("henshin"))
                return state.setAndContinue(HENSHIN);
            return PlayState.CONTINUE;
        }

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

        /* -------- 展示/待机 -------- */
        if (show) {
            if (!cur.equals("show")) return state.setAndContinue(SHOW);
            return PlayState.CONTINUE;
        }

        /* -------- 默认 -------- */
        if (!cur.equals("idles")) return state.setAndContinue(IDLES);
        return PlayState.CONTINUE;
    }

    /* -------------- NBT 工具 -------------- */
    public void setModeAndTriggerHenshin(LivingEntity entity, ItemStack stack, BeltMode mode) {
        setMode(stack, mode);
        triggerAnim(entity, "controller", "henshin");
    }

    /**
     * 设置腰带的模式
     */
    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack == null) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("BeltMode", mode.name());
        
        // 更新物品显示名称，使每条腰带在物品栏中显示其模式
        String modeText = switch (mode) {
            case BRAIN -> "Brain驱动器 - Brain骑士形态";
            default -> "Brain驱动器 - 普通形态";
        };
        
        stack.setHoverName(Component.literal(modeText));
    }

    /* -------------- 业务 -------------- */
    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        setRelease(stack, true);
        setShowing(stack, false);
        setHenshin(stack, false);

        triggerAnim(entity, "controller", "cancel");
    }

    /* -------------- 工具 -------------- */
    private RawAnimation getAnim(String name) {
        return switch (name) {
            case "show" -> SHOW;
            case "idles" -> IDLES;
            case "henshin" -> HENSHIN;
            case "cancel" -> CANCEL;
            default -> IDLES;
        };
    }

    /* -------------- 物品提示 -------------- */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        // 添加当前腰带形态提示
        BeltMode mode = getMode(stack);
        String modeText = switch (mode) {
            case BRAIN -> Component.translatable("tooltip.braindriver.mode.brain").getString();
            default -> Component.translatable("tooltip.braindriver.mode.normal").getString();
        };
        tooltipComponents.add(Component.translatable("tooltip.braindriver.mode", modeText));
    }

    /* -------------- Curio -------------- */
    @Override
    public void onEquip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        super.onEquip(ctx, prev, stack);
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

        // 更新玩家变量，标记装备了BrainDriver
        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            variables.isBrainDriverEquipped = true;
            
            // 检测玩家是否血量低于4颗心（8点生命值），如果低于且不是Roidmude，则转化为Roidmude种族
            if (!variables.isRoidmude && player.getHealth() < 8.0F) {
                variables.isRoidmude = true;
                variables.roidmudeType = "brain";
                variables.roidmudeNumber = 3;
                variables.isRoidmudeEvolved = false;
                
                // 触发Roidmude成就
                com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.RoidmudeTrigger.getInstance().trigger(player);
                
                // 通知玩家
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("你的生命垂危，Brain驱动器将你转化为机械变异体（003）以延续生命！")
                );
            }
            
            variables.syncPlayerVariables(player);
        });

        // 触发动画
        triggerAnim(player, "controller", "show");
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack prev, ItemStack stack) {
        if (ctx == null || stack == null) {
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
        
        // 更新玩家变量，标记卸下了BrainDriver
        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            variables.isBrainDriverEquipped = false;
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

        // 降低同步频率，5秒同步一次，减少动画异常
        if (sp.tickCount % 100 == 0) {
            PacketHandler.sendToClient(
                    new BeltAnimationPacket(sp.getId(), "sync_state", getMode(stack)), sp);
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
        if (nbt.contains("BeltMode")) setMode(stack, BeltMode.valueOf(nbt.getString("BeltMode")));
        if (nbt.contains("IsShowing")) setShowing(stack, nbt.getBoolean("IsShowing"));
        if (nbt.contains("IsRelease")) setRelease(stack, nbt.getBoolean("IsRelease"));
    }

    /* -------------- 客户端渲染器 -------------- */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BrainDriverRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new BrainDriverRenderer();
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
        if (entity instanceof ServerPlayer sp) {
            // 从Curio槽位获取腰带模式
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(sp).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof BrainDriver));
            
            // 确保腰带存在且不是null
            BeltMode mode = beltOptional.map(result -> {
                ItemStack stack = result.stack();
                return stack != null ? getMode(stack) : BeltMode.DEFAULT;
            }).orElse(BeltMode.DEFAULT);

            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), anim, mode), entity);
        }
    }
}