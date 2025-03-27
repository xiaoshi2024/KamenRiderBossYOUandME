package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item.Mega_uiorderRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.ghost_by_necrom.KnecromghostEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures.TransformPlayerProcedure;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.function.Consumer;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup.NECROM_HENSHIN;
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup.NECROM_HENSHINX;
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding.CHANGES_KEY;
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding.CHANGE_KEY;

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

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen == null) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                if (event.getAction() == GLFW.GLFW_PRESS) {
                    if (CHANGES_KEY.isDown()) {
                        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                            curiosInventory.getStacksHandler("bracelet").ifPresent(slotInventory -> {
                                for (int i = 0; i < slotInventory.getSlots(); i++) {
                                    IDynamicStackHandler curioStack = slotInventory.getStacks();
                                    ItemStack stack = curioStack.getStackInSlot(i);
                                    if (stack.getItem() instanceof Mega_uiorder) {
                                        Mega_uiorder mega = (Mega_uiorder) stack.getItem();
                                        Mode currentMode = mega.getCurrentMode(stack);

                                        // 检查当前模式是否为 NECROM_EYE
                                        if (currentMode == Mode.NECROM_EYE) {
                                            // 播放 LOGIN_BY 音效
                                            player.level().playSound(player, player.getX(), player.getY(), player.getZ(), ModBossSounds.LOGIN_BY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                                        }
                                    }
                                }
                            });
                        });
                    } else if (CHANGE_KEY.isDown()) {
                        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                            curiosInventory.getStacksHandler("bracelet").ifPresent(slotInventory -> {
                                for (int i = 0; i < slotInventory.getSlots(); i++) {
                                    IDynamicStackHandler curioStack = slotInventory.getStacks();
                                    ItemStack stack = curioStack.getStackInSlot(i);
                                    if (stack.getItem() instanceof Mega_uiorder) {
                                        Mega_uiorder mega = (Mega_uiorder) stack.getItem();
                                        Mode currentMode = mega.getCurrentMode(stack);

                                        // 检查当前模式是否为 NECROM_EYE
                                        if (currentMode == Mode.NECROM_EYE) {
                                            // 触发 heixn 动画
                                            HEIXN.thenPlay("heixn");

                                            // 播放玩家动画
                                            PacketHandler.sendToServer(new PlayerAnimationPacket(player.getUUID(), NECROM_HENSHINX));

                                            // 切换到 XW_MODE
                                            mega.switchMode(stack, Mode.XW_MODE);

                                            //调用变身盔甲
                                            TransformPlayerProcedure.execute(player);

                                            //播放变身音效
                                            player.level().playSound(player, player.getX(), player.getY(), player.getZ(), ModBossSounds.EYE_DROP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);


                                            // 播放幽灵实体的 possessed 动画并移除实体
                                            Level world = player.level();
                                            world.getEntitiesOfClass(KnecromghostEntity.class, player.getBoundingBox().inflate(10, 10, 10)).forEach(entity -> {
                                                if (entity instanceof KnecromghostEntity) {
                                                    ((KnecromghostEntity) entity).setAnimation("possessed");
                                                    ((KnecromghostEntity) entity).setRemoveDelay(25); // 设置延迟移除时间
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        });
                    }
                }
            }
        }
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
