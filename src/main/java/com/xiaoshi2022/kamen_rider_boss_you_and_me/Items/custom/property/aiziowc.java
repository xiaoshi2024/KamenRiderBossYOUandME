package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Another_zi_o_click.aiziowcRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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

import java.util.function.Consumer;

public class aiziowc extends Item implements GeoItem {
    private static final RawAnimation CLICK = RawAnimation.begin().thenPlay("click");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final int MAX_DAMAGE = 64;   // 与原版工具耐久同概念
    
    public static final String MODE_KEY = "Mode";

    public enum Mode {
        DEFAULT,
        ANOTHER,
        DEN_O,
        DCD,
    }

    public aiziowc(Properties props) {
        super(props.durability(MAX_DAMAGE));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
    
    public void switchMode(ItemStack stack, Mode mode) {
        stack.getOrCreateTag().putString(MODE_KEY, mode.name());

        Player player = (Player) stack.getEntityRepresentation();
        if (player != null) player.containerMenu.broadcastChanges();
    }
    
    public Mode getCurrentMode(ItemStack stack) {
        String name = stack.getOrCreateTag().getString(MODE_KEY);
        try {
            return Mode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Mode.DEFAULT;
        }
    }

    public static int getUseCount(ItemStack stack) {
        return stack.getOrCreateTag().getInt("DiskUse");
    }
    public static void incrUseCount(ItemStack stack) {
        stack.getOrCreateTag().putInt("DiskUse", getUseCount(stack) + 1);
    }

    public static void setUseCount(ItemStack disk, int i) {
        disk.getOrCreateTag().putInt("DiskUse", i);
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(MODE_KEY, Tag.TAG_STRING)) {
            tag.putString(MODE_KEY, Mode.DEFAULT.name());
        }
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private aiziowcRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new aiziowcRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 切换模式功能（使用shift+右键切换）
        if (player.isShiftKeyDown()) {
            Mode currentMode = getCurrentMode(stack);
            Mode newMode;
            // 循环切换：默认 -> 异类 -> 电王 -> DCD -> 默认
            if (currentMode == Mode.DEFAULT) {
                newMode = Mode.ANOTHER;
            } else if (currentMode == Mode.ANOTHER) {
                newMode = Mode.DEN_O;
            } else if (currentMode == Mode.DEN_O) {
                newMode = Mode.DCD;
            } else {
                newMode = Mode.DEFAULT;
            }
            switchMode(stack, newMode);
            String modeName;
            if (newMode == Mode.DEFAULT) modeName = "默认";
            else if (newMode == Mode.ANOTHER) modeName = "异类";
            else if (newMode == Mode.DEN_O) modeName = "电王";
            else modeName = "Decade";
            player.displayClientMessage(Component.literal("已切换至" + modeName + "模式"), true);
            return InteractionResultHolder.success(stack);
        }
        
        // 原有功能：点击触发动画和音效
        final int INTERVAL = 12 * 20; // 12秒间隔，1秒 = 20 tick
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");
        long currentTime = level.getGameTime();

        if (currentTime - lastPlayed >= INTERVAL) {
            if (level instanceof ServerLevel serverLevel) {
                // 触发动画
                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "click", "click");

                // 根据当前模式播放不同的音效
                Mode currentMode = getCurrentMode(stack);
                if (currentMode == Mode.DEN_O) {
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), 
                            ModBossSounds.AIDEN_OWC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                } else if (currentMode == Mode.DCD) {
                    // 检查是否有DCD模式对应的音效，如果没有则使用默认音效
                    try {
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), 
                                ModBossSounds.ANOTHER_DECADE_CLICK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    } catch (Exception e) {
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), 
                                ModBossSounds.ANOTHER_ZI_O_CLICK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                } else {
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), 
                            ModBossSounds.ANOTHER_ZI_O_CLICK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                // 更新玩家最后一次播放音效的时间
                player.getPersistentData().putLong("lastPlayedSound", currentTime);
                
                // 增加使用次数
                incrUseCount(stack);
            }
        } else {
            // 提示玩家冷却时间未结束
            player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
        }

        return super.use(level, player, hand);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Mode mode = getCurrentMode(stack);
        String modeName;
        if (mode == Mode.DEFAULT) modeName = "默认";
        else if (mode == Mode.ANOTHER) modeName = "异类";
        else if (mode == Mode.DEN_O) modeName = "电王";
        else modeName = "Decade";
        tooltip.add(Component.literal("当前模式: " + modeName)
                .withStyle(net.minecraft.ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Shift+右键切换模式 (默认 -> 异类 -> 电王 -> Decade)")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    // Let's add our animation controller
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "click", 20, state -> PlayState.STOP)
                .triggerableAnim("click", CLICK)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                    // Use helper method to avoid client-code in common class
                    Player player = ClientUtils.getClientPlayer();

                    if (player != null) {
                        // 检查玩家手中的物品模式
                        if (player.getMainHandItem().getItem() instanceof aiziowc) {
                            aiziowc item = (aiziowc) player.getMainHandItem().getItem();
                            if (item.getCurrentMode(player.getMainHandItem()) == Mode.DEN_O) {
                                player.playSound(ModBossSounds.AIDEN_OWC.get(), 1, 1);
                            } else if (item.getCurrentMode(player.getMainHandItem()) == Mode.DCD) {
                                try {
                                    player.playSound(ModBossSounds.ANOTHER_DECADE_CLICK.get(), 1, 1);
                                } catch (Exception e) {
                                    player.playSound(ModBossSounds.ANOTHER_ZI_O_CLICK.get(), 1, 1);
                                }
                            } else {
                                player.playSound(ModBossSounds.ANOTHER_ZI_O_CLICK.get(), 1, 1);
                            }
                        } else {
                            player.playSound(ModBossSounds.ANOTHER_ZI_O_CLICK.get(), 1, 1);
                        }
                    }
                }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

