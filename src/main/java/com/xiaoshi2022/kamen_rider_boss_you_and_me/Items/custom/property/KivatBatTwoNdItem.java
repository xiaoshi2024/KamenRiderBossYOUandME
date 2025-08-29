package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.KivatBatTwoNdm.KivatBatTwoItemRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.DarkKivaSequence;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;
import java.util.function.Consumer;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.henshin.TransformationRequestPacket;
import net.minecraft.client.player.LocalPlayer;

public class KivatBatTwoNdItem extends Item implements GeoItem {

    /* ---------- 动画定义 ---------- */
    private static final RawAnimation SAY   = RawAnimation.begin().thenPlay("say");
    private static final RawAnimation FLY   = RawAnimation.begin().thenPlay("fly");
    private static final RawAnimation SNAP  = RawAnimation.begin().thenPlay("snap");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public KivatBatTwoNdItem(Properties properties) {
        super(properties);
        /* 注册同步，让动画能在客户端之间同步播放 */
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private KivatBatTwoItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new KivatBatTwoItemRenderer();

                return this.renderer;
            }
        });
    }
    
    /* ---------- 右键切换动画 ---------- */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // 如果没有UUID，初始化一个
            if (!stack.getOrCreateTag().hasUUID("UUID")) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.putUUID("UUID", UUID.randomUUID());
                tag.putUUID("OwnerUUID", player.getUUID()); // 默认绑定使用者
                tag.putBoolean("FromKivatEntity", true);
                tag.putFloat("Health", 80.0F);
                tag.putString("CustomName", "{\"text\":\"Kivat Bat II\"}");
            }

            int state = stack.getOrCreateTag().getInt("animationState");
            state = (state + 1) % 5;
            stack.getOrCreateTag().putInt("animationState", state);
        }
        return InteractionResultHolder.success(stack);
    }

    /* ---------- 动画控制器 ---------- */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<KivatBatTwoNdItem> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        if (stack == null) return PlayState.STOP;

        int mode = stack.getOrCreateTag().getInt("animationState");
        switch (mode) {
            case 0 -> state.setAndContinue(SNAP);
            case 1 -> state.setAndContinue(FLY);
            case 2 -> state.setAndContinue(SAY);
            default -> { return PlayState.STOP; }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    
    /* ---------- 注册按键检测事件 ---------- */
    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class TransformationHandler {
        private static boolean transformationKeyPressed = false; // 添加标志位，确保只发送一次请求
        
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                
                if (player == null || mc.isPaused()) return;
                
                boolean isKeyDown = KeyBinding.CHANGE_KEY.isDown() && isHoldingKivatBat(player);
                
                // 只有在按键刚被按下的瞬间才发送请求
                if (isKeyDown && !transformationKeyPressed) {
                    // 发送KIVAT_BAT_ITEM类型的变身请求到服务器
                    PacketHandler.INSTANCE.sendToServer(
                            new TransformationRequestPacket(player.getUUID(), "KIVAT_BAT_ITEM", false)
                    );
                }
                
                // 更新标志位
                transformationKeyPressed = isKeyDown;
            }
        }
        
        private static boolean isHoldingKivatBat(Player player) {
            // 检查主手或副手是否持有月蝠
            return player.getMainHandItem().getItem() instanceof KivatBatTwoNdItem || 
                   player.getOffhandItem().getItem() instanceof KivatBatTwoNdItem;
        }
    }
}