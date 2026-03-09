package com.xiaoshi2022.kamenriderbossyouandme.Accessory;

import com.jpigeon.ridebattlelib.api.RiderManager;
import com.jpigeon.ridebattlelib.core.system.event.HenshinEvent;
import com.jpigeon.ridebattlelib.core.system.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.item.genesisdriver.GenesisDriverRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    // 玩家级别冷却地图，用于防止多个腰带同时消耗果实
    private static final Map<UUID, Integer> PLAYER_FRUIT_COOLDOWNS = new HashMap<>();

    /* ------------------------- 事件处理 ------------------------- */
    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Pre event) {
        Player player = event.getPlayer();
        // 处理创世纪驱动器的变身逻辑
        handleGenesisDriverHenshin(player);
    }

    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Post event) {
        Player player = event.getPlayer();
        // 处理创世纪驱动器的解除变身逻辑
        handleGenesisDriverUnhenshin(player);
    }

    private static void handleGenesisDriverHenshin(Player player) {
        // 检查玩家是否装备了创世纪驱动器
        if (player instanceof ServerPlayer sp) {
            ItemStack beltStack = getBeltStack(sp);
            if (beltStack.isEmpty()) {
                return;
            }

            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            BeltMode mode = belt.getMode(beltStack);

            // 播放变身动画
            // 触发腰带动画
            belt.startHenshinAnimation(sp, beltStack);
            
            // 完成变身
            RiderManager.completeIn(60, sp);
        }
    }

    private static void handleGenesisDriverUnhenshin(Player player) {
        // 检查玩家是否装备了创世纪驱动器
        if (player instanceof ServerPlayer sp) {
            ItemStack beltStack = getBeltStack(sp);
            if (beltStack.isEmpty()) {
                return;
            }

            Genesis_driver belt = (Genesis_driver) beltStack.getItem();
            
            // 播放解除变身动画
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

    // 保存当前的ItemStack，用于渲染时获取模式
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

    /* 读取实时 NBT 状态，不再使用任何字段 */
    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        // 添加null检查
        if (stack == null || !(state.getAnimatable() instanceof Genesis_driver))
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
    // 获取或创建 CompoundTag
    private static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData == CustomData.EMPTY) {
            return new CompoundTag();
        }
        return customData.copyTag();
    }

    // 保存 CompoundTag
    private static void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // 修改 CompoundTag 并保存
    private static void modifyTag(ItemStack stack, Consumer<CompoundTag> action) {
        CompoundTag tag = getOrCreateTag(stack);
        action.accept(tag);
        saveTag(stack, tag);
    }

    public boolean getEquipped(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_EQUIPPED);
    }

    public void setEquipped(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_EQUIPPED, flag));
    }

    public BeltMode getMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return DEFAULT;
        }
        CompoundTag tag = getOrCreateTag(stack);
        String modeName = tag.getString(TAG_BELT_MODE);
        if (!modeName.isEmpty()) {
            try {
                return BeltMode.valueOf(modeName);
            } catch (IllegalArgumentException ex) {
                return DEFAULT;                       // ← 防止未来拼写错误
            }
        }
        return DEFAULT;        // ← 兜底
    }

    public void setMode(ItemStack stack, BeltMode mode) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putString(TAG_BELT_MODE, mode.name()));
    }

    public boolean getShowing(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_SHOWING);
    }

    public void setShowing(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_SHOWING, flag));
    }

    public boolean getActive(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_ACTIVE);
    }

    public void setActive(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_ACTIVE, flag));
    }

    public boolean getHenshin(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_HENSHIN);
    }

    public void setHenshin(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_HENSHIN, flag));
    }

    public boolean getRelease(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = getOrCreateTag(stack);
        return tag.getBoolean(TAG_IS_RELEASE);
    }

    public void setRelease(ItemStack stack, boolean flag) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        modifyTag(stack, tag -> tag.putBoolean(TAG_IS_RELEASE, flag));
    }

    /* ----------------------------------------------------------- */

    /* ===================== 业务方法（无字段） ==================== */
    public void startHenshinAnimation(LivingEntity entity, ItemStack stack) {
        setHenshin(stack, true);
        setRelease(stack, false);

        // 触发本地动画
        if (entity.level().isClientSide()) {
            BeltMode mode = getMode(stack);
            String anim = switch (mode) {
                case LEMON  -> "lemon_move";
                case MELON  -> "melon_move";
                case CHERRY -> "cherry_move";
                case PEACH  -> "peach_move";
                case DRAGONFRUIT -> "dragonfruit_move";
                default     -> "move";
            };
            triggerAnim(entity, "controller", anim);
        }
    }

    /**
     * 先播放玩家sodax动画，然后播放解除变身动画
     * 这个方法只在解除变身时被调用，确保sodax动画只在解除变身时播放
     */
    public void startReleaseWithPlayerAnimation(LivingEntity entity, ItemStack stack) {
        // 直接播放解除变身动画
        startReleaseAnimation(entity, stack);
    }
    
    public void startReleaseAnimation(LivingEntity entity, ItemStack stack) {
        setRelease(stack, true);
        setHenshin(stack, false);

        // 触发本地动画
        if (entity.level().isClientSide()) {
            String anim = switch (getMode(stack)) {
                case MELON  -> "melon_start";
                case LEMON  -> "start";
                case CHERRY -> "cherry_start";
                case PEACH  -> "peach_start";
                case DRAGONFRUIT -> "dragonfruit_start";
                default     -> "start";
            };
            triggerAnim(entity, "controller", anim);
        }
    }

    /* =========================================================== */

    /* -------------------- 其它必要实现 -------------------- */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // 利用我们自己的渲染钩子来定义我们的自定义渲染器
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
        if (player == null || beltStack == null || beltStack.isEmpty()) {
            return;
        }
        
        setShowing(beltStack, true);
        setActive(beltStack, false);
        setHenshin(beltStack, false);
        setRelease(beltStack, false);

        // 触发动画
        triggerAnim(player, "controller", "start");
        
        // 检查腰带是否已经有锁种形态（非默认模式）
        BeltMode mode = getMode(beltStack);
        if (mode != DEFAULT) {
            // 设置准备变身状态，而不是直接触发变身
            // 记录腰带模式，以便玩家按X键时能够正确触发对应形态的变身
            setActive(beltStack, true);
            
            // 通知玩家准备好变身，需要按X键触发
            player.sendSystemMessage(
                    Component.literal("腰带已准备好变身！请按 X 键完成变身过程")
            );
        }
    }
    
    /* ========== 玩家动画支持 ========== */
    /* 发送动画到客户端 */
    public static void playPlayerAnimation(ServerPlayer player, String animationName) {
        if (player.level().isClientSide()) {
            // 在客户端，我们需要直接触发本地动画
            // 但由于Minecraft的限制，我们可能需要通过其他方式实现

            return;
        }
        
        // 简化实现，仅打印日志

    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) {
            return;
        }
        
        if (!(ctx.entity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity le = (LivingEntity) ctx.entity();
        setShowing(stack, false);
        setActive(stack, false);
        
        if (le.level() != null && !le.level().isClientSide() && le instanceof ServerPlayer sp) {
            // 移除玩家的饱和效果
            sp.removeEffect(MobEffects.SATURATION);
        }
        
        triggerAnim(le, "controller", "idles");
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null || stack.isEmpty()) {
            return;
        }
        
        if (ctx.entity().level() == null || ctx.entity().level().isClientSide()) {
            return;
        }
        
        if (!(ctx.entity() instanceof ServerPlayer sp)) {
            return;
        }

        // 每5秒检查一次饱和效果，如果没有则尝试消耗赫尔海姆果实
        // 使用玩家级别冷却，避免多个腰带同时消耗果实
        UUID playerId = sp.getUUID();
        int lastConsumeTime = PLAYER_FRUIT_COOLDOWNS.getOrDefault(playerId, 0);
        if (sp.tickCount - lastConsumeTime >= 100) {
            if (!sp.hasEffect(MobEffects.SATURATION)) {
                // 尝试消耗背包中的赫尔海姆果实
                if (consumeHelheimFruit(sp)) {
                    // 给予5分钟的饱和效果
                    sp.addEffect(new MobEffectInstance(MobEffects.SATURATION, 5 * 60 * 20, 0, true, false));
                    // 同时增加5点饱食度
                    sp.getFoodData().eat(5, 0.5f);
                    sp.sendSystemMessage(
                            Component.literal("消耗了一颗赫尔海姆果实，获得了5分钟的饱和效果和5点饱食度！")
                    );
                    // 更新玩家级别的冷却时间
                    PLAYER_FRUIT_COOLDOWNS.put(playerId, sp.tickCount);
                }
            }
        }
    }

    /**
     * 查找并消耗玩家背包中的赫尔海姆果实
     * @param player 玩家
     * @return 是否成功消耗果实
     */
    private boolean consumeHelheimFruit(ServerPlayer player) {
        // 再次检查玩家是否已经有饱和效果，避免多个腰带同时消耗果实
        if (!player.hasEffect(MobEffects.SATURATION)) {
            // 简化实现，暂时返回false
            // 实际实现需要根据项目中的赫尔海姆果实物品来修改
            return false;
        }
        return false;
    }
    
    /**
     * 在玩家背包中查找并消耗指定物品
     * @param player 玩家
     * @param item 要查找的物品
     * @return 是否成功消耗
     */
    private boolean findAndConsumeItem(Player player, Item item) {
        // 首先检查主手
        if (player.getMainHandItem().getItem() == item) {
            player.getMainHandItem().shrink(1);
            return true;
        }
        
        // 检查副手
        if (player.getOffhandItem().getItem() == item) {
            player.getOffhandItem().shrink(1);
            return true;
        }
        
        // 检查背包
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.getItem() == item) {
                stack.shrink(1);
                // 更新玩家背包
                player.getInventory().items.set(i, stack);
                return true;
            }
        }
        
        return false;
    }

    /* -------------------- NBT 同步 -------------------- */
    // 在1.21.1版本中，getShareTag和readShareTag方法已经被DataComponents.CUSTOM_DATA所取代
    // 数据同步通过CustomData自动处理

    /* -------------------- 动画触发工具 -------------------- */
    public void triggerAnim(@Nullable LivingEntity entity, String ctrl, String anim) {
        if (entity == null || entity.level() == null) return;
        // 简化实现，仅在客户端触发本地动画
        if (entity.level().isClientSide()) {
            // 这里可以添加客户端动画触发逻辑

        }
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

    // 获取当前物品的模式（用于模型渲染）
    public BeltMode getCurrentMode(ItemStack stack) {
        this.currentStack = stack;
        return getMode(stack);
    }

    public BeltMode getCurrentMode() {
        if (currentStack != null) {
            return getMode(currentStack);
        }
        // 创建一个默认的物品堆栈来获取模式
        ItemStack stack = new ItemStack(this);
        return getMode(stack);
    }
    
    // 设置当前的ItemStack
    public void setCurrentStack(ItemStack stack) {
        this.currentStack = stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        // 添加空行
        tooltipComponents.add(Component.literal(""));
        
        // 使用语言文件中的描述（带颜色代码）
        tooltipComponents.add(Component.translatable("item.kamenriderbossyouandme.genesis_driver.desc"));
    }

    private static ItemStack getBeltStack(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}