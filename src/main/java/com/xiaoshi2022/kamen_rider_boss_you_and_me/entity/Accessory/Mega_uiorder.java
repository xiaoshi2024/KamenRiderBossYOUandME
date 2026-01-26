package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item.Mega_uiorderRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.TransformationRequestPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class Mega_uiorder extends Item implements GeoItem, ICurioItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlayAndHold("idle");
    public static final RawAnimation GHOST = RawAnimation.begin().thenPlay("ghost");
    public static final RawAnimation HEIXN = RawAnimation.begin().thenPlay("heixn");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public void triggerAnim(LivingEntity entity, String controller, String animationName) {
        // 在这里添加触发动画的逻辑
        // 例如，使用 GeckoLib 的 AnimationController 来触发动画
    }

    public enum Mode {
        DEFAULT,
        NECROM_EYE,
        XW_MODE // 新增 XW_MODE
    }

    public Mega_uiorder(Properties properties) {
        super(properties);
        // Register our item as server-side handled.
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void switchMode(ItemStack stack, Mode mode) {
        if (stack == null) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Mode", mode.name());
        stack.setTag(tag);
    }

    public Mode  getCurrentMode(ItemStack stack) {
        if (stack == null) {
            return Mode.DEFAULT;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("Mode")) {
            return Mode.valueOf(tag.getString("Mode"));
        }
        return Mode.DEFAULT;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private Mega_uiorderRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new Mega_uiorderRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 20, state -> PlayState.STOP)
                .triggerableAnim("idle", IDLE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level == null || player == null || hand == null) {
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        }
        
        ItemStack stack = player.getItemInHand(hand);
        if (stack == null || stack.isEmpty()) {
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        }
        
        if (level.isClientSide) {
            return super.use(level, player, hand);
        }

        // 检查玩家是否持有 Necrom_eye
        boolean hasNecromEye = player.getInventory().contains(new ItemStack(ModItems.NECROM_EYE.get()));
        // 检查玩家是否持有额外的道具（例如 Mega_uiorder 的特定模式）
        boolean hasAdditionalItem = getCurrentMode(stack) == Mode.NECROM_EYE;

        if (hasNecromEye && hasAdditionalItem) {
            // 触发变身
            PacketHandler.sendToServer(new TransformationRequestPacket(player.getUUID(), "RIDERNECROM", false));
            switchMode(stack, Mode.NECROM_EYE); // 切换到 NECROM_EYE 模式
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        } else {
            // 提示玩家缺少必要的道具
            if (!hasNecromEye) {
                player.displayClientMessage(Component.literal("You need a Necrom Eye to transform!"), true);
            }
            if (!hasAdditionalItem) {
                player.displayClientMessage(Component.literal("You need to set the Mega_uiorder to NECROM_EYE mode!"), true);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}