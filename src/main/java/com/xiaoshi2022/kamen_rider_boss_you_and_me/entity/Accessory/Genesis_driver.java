package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.henshin.HeartCoreEvent;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.Drivershenshin.BeltAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver.BeltMode.DEFAULT;
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver.BeltMode.LEMON;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class Genesis_driver extends AbstractRiderBelt implements GeoItem, ICurioItem {

    /* ------------------------- 静态变量 ------------------------- */
    // 玩家级别冷却地图，用于防止多个腰带同时消耗果实
    private static final Map<UUID, Integer> PLAYER_FRUIT_COOLDOWNS = new HashMap<>();
    
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


//    public void onBossInteraction(Player player, ItemStack driver, VillagerEntityMCA boss) {
//        player.sendSystemMessage(
//                net.minecraft.network.chat.Component.literal("驱纹戒斗：你也想参加这场游戏吗？")
//        );
//        // TODO: 在这里写真正的变身、给 Buff 等逻辑
//    }
    /* ----------------------------------------------------------- */

    public enum BeltMode {
        DEFAULT, LEMON, MELON, CHERRY, PEACH, DRAGONFRUIT
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Genesis_driver(Properties properties) {
        super(properties);
//        // 注册为同步可动画对象，确保多人游戏中材质变化能正确同步
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
    public BeltMode getMode(ItemStack stack) {
        if (stack == null) {
            return DEFAULT;
        }
        net.minecraft.nbt.CompoundTag tag = stack.getOrCreateTag();
        net.minecraft.nbt.Tag beltModeTag = tag.get("BeltMode");
        if (beltModeTag instanceof net.minecraft.nbt.StringTag) {
            String key = beltModeTag.getAsString();
            if (!key.isEmpty()) {
                try {
                    return BeltMode.valueOf(key);
                } catch (IllegalArgumentException ex) {
                    return DEFAULT;                       // ← 防止未来拼写错误
                }
            }
        }
        return DEFAULT;        // ← 兜底
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

        // 1. 服务端：把腰带动画名同步给所有追踪者
        if (!entity.level().isClientSide && entity instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(sp.getId(), anim, mode), sp);

            // 同时播放玩家sodas动画
            playPlayerAnimation(sp, "sodas");
        }

        // 2. 客户端：本地线程直接播，不再发包
        if (entity.level().isClientSide) {
            triggerAnim(entity, "controller", anim);
        }
    }

    /**
     * 先播放玩家sodax动画，然后播放解除变身动画
     * 这个方法只在解除变身时被调用，确保sodax动画只在解除变身时播放
     */
    public void startReleaseWithPlayerAnimation(LivingEntity entity, ItemStack stack) {
        // 1. 首先播放玩家sodax动画
        // 服务器端：发送动画数据包给所有客户端
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer sp) {
            // 只有在解除变身时播放sodax动画
            playPlayerAnimation(sp, "sodax");
        } else if (entity.level().isClientSide()) {
            // 客户端：直接触发本地动画
            // 确保客户端也能播放sodax动画
            System.out.println("客户端直接播放sodax动画");
            // 这里需要直接触发玩家动画，可能需要通过其他方式实现
            // 例如：调用客户端动画触发方法
        }
        
        // 2. 然后播放解除变身动画
        startReleaseAnimation(entity, stack);
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
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver.GenesisDriverRenderer renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.genesisdriver.GenesisDriverRenderer();
                return renderer;
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
        if (player == null || beltStack == null) {
            return;
        }
        
        setShowing(beltStack, true);
        setActive(beltStack, false);
        setHenshin(beltStack, false);
        setRelease(beltStack, false);

        // 同步腰带状态到所有跟踪的玩家
        PacketHandler.sendToAllTracking(
                new BeltAnimationPacket(player.getId(), "start", DEFAULT),
                player);
        
        // 更新玩家变量
        player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(variables -> {
            variables.isGenesisDriverEquipped = true;
            variables.syncPlayerVariables(player);
        });

        // 触发动画
        triggerAnim(player, "controller", "start");
        
        // 检查腰带是否已经有锁种形态（非默认模式）
        BeltMode mode = getMode(beltStack);
        if (mode != DEFAULT) {
            // 获取对应的变身类型
            String riderType = switch (mode) {
                case LEMON -> "GENESIS";
                case MELON -> "GENESIS_MELON";
                case CHERRY -> "GENESIS_CHERRY";
                case PEACH -> "GENESIS_PEACH";
                case DRAGONFRUIT -> "GENESIS_DRAGONFRUIT";
                default -> null;
            };
            
            if (riderType != null) {
                // 检查玩家是否已经穿着对应盔甲
                boolean isTransformed = false;
                switch (mode) {
                    case LEMON -> 
                        isTransformed = isWearingSpecificArmor(player, ModItems.RIDER_BARONS_HELMET.get());
                    case MELON -> 
                        isTransformed = isWearingSpecificArmor(player, ModItems.ZANGETSU_SHIN_HELMET.get());
                    case CHERRY -> 
                        isTransformed = isWearingSpecificArmor(player, ModItems.SIGURD_HELMET.get());
                    case PEACH -> 
                        isTransformed = isWearingSpecificArmor(player, ModItems.MARIKA_HELMET.get());
                    case DRAGONFRUIT -> 
                        isTransformed = isWearingSpecificArmor(player, ModItems.TYRANT_HELMET.get());
                }
                
                if (!isTransformed) {
                    // 设置准备变身状态，而不是直接触发变身
                    // 记录腰带模式，以便玩家按X键时能够正确触发对应形态的变身
                    setActive(beltStack, true);
                    
                    // 获取玩家变量
                    KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                    
                    // 设置对应的准备状态
                    switch (mode) {
                        case LEMON -> variables.lemon_ready = true;
                        case MELON -> variables.melon_ready = true;
                        case CHERRY -> variables.cherry_ready = true;
                        case PEACH -> variables.peach_ready = true;
                        case DRAGONFRUIT -> variables.dragonfruit_ready = true;
                        default -> {}
                    }
                    variables.syncPlayerVariables(player);
                    
                    // 通知玩家准备好变身，需要按X键触发
                    player.sendSystemMessage(
                            Component.literal("腰带已准备好变身！请按 X 键完成变身过程")
                    );
                    
                    // 播放待机音效，表明已经准备就绪
//                    String soundName = switch (mode) {
//                        case LEMON -> "lemon_lockonby";
//                        case MELON -> "lemon_lockonby";
//                        case CHERRY -> "lemon_lockonby";
//                        case PEACH -> "lemon_lockonby";
//                        case DRAGONFRUIT -> "lemon_lockonby";
//                        default -> "lemon_lockonby";
//                    };
//                    
//                    ResourceLocation soundLoc = new ResourceLocation(
//                            "kamen_rider_boss_you_and_me",
//                            soundName
//                    );
//                    
//                    player.level().playSound(
//                            null,
//                            player.getX(),
//                            player.getY(),
//                            player.getZ(),
//                            ForgeRegistries.SOUND_EVENTS.getValue(soundLoc),
//                            SoundSource.PLAYERS,
//                            1.0F,
//                            1.0F
//                    );
                }
            }
        }
    }
    
    /**
     * 检查玩家是否穿着特定类型的盔甲
     */
    private boolean isWearingSpecificArmor(Player player, Item armorItem) {
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() == armorItem;
    }
    
    /**
     * 检查玩家是否穿着巴隆基础套装，如果是则自动变身巴隆柠檬形态
     */
    private void checkBaronLemonTransformation(ServerPlayer player, ItemStack beltStack) {
        // 检查玩家是否穿着巴隆基础套装
        boolean wearingBaronBaseSet = isWearingBaronBaseArmor(player);
        
        // 如果玩家穿着巴隆基础套装，并且腰带是柠檬形态
        if (wearingBaronBaseSet) {
            // 设置腰带为柠檬模式
            setMode(beltStack, LEMON);
            
            // 触发巴隆柠檬形态变身
            new HeartCoreEvent(player, "LEMON_ENERGY:BARON_LEMON");
            
            // 发送系统消息通知玩家
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("已自动变身为巴隆柠檬形态！")
            );
        }
    }

    /* ========== 玩家动画支持 ========== */
    /* 发送动画到客户端 */
    public static void playPlayerAnimation(ServerPlayer player, String animationName) {
        if (player.level().isClientSide()) {
            // 在客户端，我们需要直接触发本地动画
            // 但由于Minecraft的限制，我们可能需要通过其他方式实现
            System.out.println("尝试在客户端播放玩家动画: " + animationName);
            return;
        }
        
        // 使用sendAnimationToAllTrackingAndSelf确保动画对玩家自己和所有跟踪者可见
        PacketHandler.sendAnimationToAllTrackingAndSelf(
                Component.literal(animationName),
                player.getId(),
                false,
                player
        );
    }
    
    /**
     * 检查玩家是否穿着巴隆基础盔甲套装
     */
    private boolean isWearingBaronBaseArmor(Player player) {
        // 检查头盔、胸甲和护腿是否都是巴隆基础装甲
        boolean hasHelmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() == ModItems.RIDER_BARONS_HELMET.get();
        boolean hasChestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() == ModItems.RIDER_BARONS_CHESTPLATE.get();
        boolean hasLeggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() == ModItems.RIDER_BARONS_LEGGINGS.get();
        
        // 返回是否穿着全套巴隆基础装甲
        return hasHelmet && hasChestplate && hasLeggings;
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
        
        if (le.level() != null && !le.level().isClientSide() && le instanceof ServerPlayer sp) {
            PacketHandler.sendToAllTracking(new BeltAnimationPacket(sp.getId(), "idles", getMode(stack)), sp);
            // 移除玩家的饱和效果
            sp.removeEffect(MobEffects.SATURATION);
        }
        
        triggerAnim(le, "controller", "idles");
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
            // 查找并消耗赫尔海姆果实
            return findAndConsumeItem(player, com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.HELHEIMFRUIT.get());
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
        if (entity instanceof ServerPlayer sp) {
            // 从Curio槽位获取腰带模式
            Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(sp).resolve()
                    .flatMap(curios -> curios.findFirstCurio(item -> item.getItem() instanceof Genesis_driver));
            BeltMode mode = beltOptional.map(result -> getMode(result.stack())).orElse(DEFAULT);
            
            PacketHandler.sendToAllTracking(
                    new BeltAnimationPacket(entity.getId(), anim, mode), entity);
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
}