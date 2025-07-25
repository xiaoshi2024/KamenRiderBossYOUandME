package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item.Mega_uiorderRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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

    public enum Mode {
        DEFAULT,
        NECROM_EYE,
        XW_MODE // 新增 XW_MODE
    }

    public Mega_uiorder(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void switchMode(ItemStack stack, Mode mode) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Mode", mode.name());
        stack.setTag(tag);
    }

    public Mode getCurrentMode(ItemStack stack) {
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
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return super.use(level, player, hand);

        if (player.isShiftKeyDown() && stack.hasTag() && stack.getTag().contains("necrom_eye")) {
            // 取出手环中的眼魂
            ItemStack eye = new ItemStack(ModItems.NECROM_EYE.get());
            eye.setTag(stack.getTag().getCompound("necrom_eye"));

            // 将眼魂放入玩家背包
            if (!player.getInventory().add(eye)) {
                player.drop(eye, false);
            }

            // 清除手环中的眼魂 NBT
            stack.getTag().remove("necrom_eye");

            switchMode(stack, Mode.DEFAULT);

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return super.use(level, player, hand);
    }
    

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
