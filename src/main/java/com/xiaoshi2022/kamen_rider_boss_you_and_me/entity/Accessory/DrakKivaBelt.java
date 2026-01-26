package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.drakkivabelt.DrakKivaBeltRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.DarkKivaSequence;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class DrakKivaBelt extends AbstractRiderBelt implements GeoItem, ICurioItem {

    /* ----------------- 动画常量 ----------------- */
    private static final RawAnimation SAY = RawAnimation.begin().thenPlay("say");
    private static final RawAnimation SNAP = RawAnimation.begin().thenPlay("snap");
    private static final RawAnimation HENSHIN = RawAnimation.begin().thenPlayAndHold("henshin");
    private static final RawAnimation DISASSEMBLY = RawAnimation.begin().thenPlayAndHold("disassembly");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final String TAG_DISASSEMBLY_DONE = "DisassemblyDone";

    public static final String TAG_DISASSEMBLY_SENT     = "DisassemblySent";
    public static final String TAG_DISASSEMBLY_RUNNING  = "DisassemblyRunning";
    public static final String TAG_HENSHIN_SENT         = "HenshinSent";

    public DrakKivaBelt(Properties properties) {
        super(properties);
    }

    public enum DrakKivaBeltMode {
        DEFAULT
    }

    /* ================= GeoItem ================= */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::animationController)
                .triggerableAnim("say", SAY)
                .triggerableAnim("snap", SNAP)
                .triggerableAnim("henshin", HENSHIN)
                .triggerableAnim("disassembly", DISASSEMBLY));
    }

    private <E extends GeoItem> PlayState animationController(AnimationState<E> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        // 添加null检查
        if (stack == null || !(state.getAnimatable() instanceof DrakKivaBelt)) return PlayState.STOP;

        boolean henshin = getHenshin(stack);
        boolean disassembly = getDisassembly(stack);

        /* -------- 变身序列 -------- */
        if (henshin) {
            return state.setAndContinue(HENSHIN);
        }

        /* -------- 解除变身 -------- */
        if (disassembly) {
            if (state.getController().getCurrentAnimation() == null ||
                    !state.getController().getCurrentAnimation().animation().name().equals("disassembly")) {
                return state.setAndContinue(DISASSEMBLY);
            }

            if (state.getController().hasAnimationFinished()) {
                stack.getOrCreateTag().putBoolean(TAG_DISASSEMBLY_DONE, true);
                setDisassembly(stack, false);
                return PlayState.STOP;
            }
            return PlayState.CONTINUE;
        }

        /* -------- 默认状态 -------- */
        return state.setAndContinue(SAY);
    }

    /* ---------- 供 DarkKivaSequence 使用 ---------- */
    public static void playAnimation(ServerPlayer player, String anim) {
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), anim, DrakKivaBeltMode.DEFAULT),
                player);
    }

    public boolean getHenshin(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getOrCreateTag().getBoolean("IsHenshin");
    }

    public static void setHenshin(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsHenshin", flag);
    }

    public boolean getDisassembly(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        return stack.getOrCreateTag().getBoolean("IsDisassembly");
    }

    public static void setDisassembly(ItemStack stack, boolean flag) {
        if (stack == null) {
            return;
        }
        stack.getOrCreateTag().putBoolean("IsDisassembly", flag);
    }


    // 修改 inventoryTick 方法
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, 
                              int slot, boolean selected) {
        if (stack == null || level == null || entity == null) {
            return;
        }
        
        if (level.isClientSide || !(entity instanceof ServerPlayer sp)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();

        // 跳过标记检查
        if (tag.getBoolean("SkipInventoryTick")) {
            return;
        }

        /* 1. 首次变身处理 */
        if (getHenshin(stack) && !tag.getBoolean("HenshinHandled")) {
            tag.putBoolean("HenshinHandled", true);
            // 只调用一次，不要再套 TickTask
            DarkKivaSequence.startHenshin(sp);
            return;
        }

        /* 2. 解除动画完成处理 */
        if (getDisassembly(stack) && tag.getBoolean(TAG_DISASSEMBLY_DONE)) {
            tag.remove(TAG_DISASSEMBLY_DONE);
            setDisassembly(stack, false);

            // 延迟启动解除序列
            if (level.getServer() != null) {
                level.getServer().tell(new net.minecraft.server.TickTask(
                        level.getServer().getTickCount() + 5,
                        () -> DarkKivaSequence.startDisassembly(sp)
                ));
            }
            return;
        }

        /* 3. 手动取下腰带处理 */
        boolean stillEquipped = CuriosApi.getCuriosInventory(sp)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(s -> s != null && s == stack))
                .isPresent();

        if (!stillEquipped) {
            // 无论变身与否，统一完整解除
            DarkKivaSequence.doFullDisassembly(sp, stack);
            DrakKivaBelt.setHenshin(stack, false);       // 保险再清一次
            stack.getOrCreateTag().remove("HenshinHandled");
        }
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
        setDisassembly(beltStack, false);
        
        // 同步腰带状态到所有跟踪的玩家
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "say", DrakKivaBeltMode.DEFAULT),
                player);
        
        // 更新玩家变量
        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            variables.isDarkKivaBeltEquipped = true;
            variables.syncPlayerVariables(player);
        });
        
        // 触发动画
        triggerAnim(player, "controller", "say");
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (ctx == null || ctx.entity() == null || stack == null) {
            return;
        }
        
        if (!(ctx.entity() instanceof ServerPlayer player)) {
            return;
        }

        if (getHenshin(stack)) {
            DarkKivaSequence.doFullDisassembly(player, stack);
        }
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (ctx.entity().level().isClientSide()) return;
        if (!(ctx.entity() instanceof ServerPlayer sp)) return;

        // 1 秒同步一次
        if (sp.tickCount % 20 == 0) {
            PacketHandler.sendToClient(new BeltAnimationPacket(sp.getId(), "sync_state", DrakKivaBeltMode.DEFAULT), sp);
        }
    }

    /* -------------- 客户端渲染器 -------------- */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DrakKivaBeltRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new DrakKivaBeltRenderer();
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

        // 服务端逻辑
        // 服务端逻辑：广播给所有追踪者（包括自己）
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTrackingAndSelf(  // ← 关键修改
                    new BeltAnimationPacket(entity.getId(), anim, DrakKivaBeltMode.DEFAULT),
                    entity
            );
        }


        // 客户端逻辑（GeckoLib 4.0）
        if (entity.level().isClientSide && entity instanceof Player) {
            CuriosApi.getCuriosInventory(entity).ifPresent(inv ->
                    inv.findCurio("belt", 0).ifPresent(slot -> {
                        ItemStack stack = slot.stack();
                        if (stack.getItem() instanceof DrakKivaBelt belt) {
                            if ("henshin".equals(anim)) {
                                DrakKivaBelt.setHenshin(stack, true);
                            } else if ("disassembly".equals(anim)) {
                                DrakKivaBelt.setDisassembly(stack, true);
                            }
                            // 4.0 触发器写法
                            belt.triggerAnim((Player) entity, "controller", anim);
                        }
                    })
            );
        }
    }

    // 辅助方法：获取玩家的腰带ItemStack
    private ItemStack getBeltStack(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof DrakKivaBelt))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}