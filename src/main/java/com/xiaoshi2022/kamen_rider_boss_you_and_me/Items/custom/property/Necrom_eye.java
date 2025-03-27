package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Necrom_eye.Necrom_eyeRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Mega_uiorder;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.ghost_by_necrom.KnecromghostEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationPacket;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Random;
import java.util.function.Consumer;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup.NECROM_HENSHIN;


public class Necrom_eye extends Item implements GeoItem {
    private static final RawAnimation START = RawAnimation.begin().thenPlay("start");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public Necrom_eye(net.minecraft.world.item.Item.Properties properties) {
        super(properties);

        // Register our item as server-side handled.
        // This enables both animation data syncing and server-side animation triggering
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    // Utilise the existing forge hook to define our custom renderer (which we created in createRenderer)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private Necrom_eyeRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new Necrom_eyeRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final int INTERVAL = 5 * 20; // 5秒间隔，1秒 = 20 tick
        long lastPlayed = player.getPersistentData().getLong("lastPlayedSound");
        long currentTime = level.getGameTime();

        if (currentTime - lastPlayed >= INTERVAL) {
            if (level instanceof ServerLevel serverLevel) {
                // 触发动画
                triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "start", "start");

                // 随机播放音效
                Random random = new Random();
                int randomIndex = random.nextInt(3); // 生成0到2的随机数
                switch (randomIndex) {
                    case 0:
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.STAND_BY_NECROM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        break;
                    case 1:
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.LOADING_EYE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        break;
                    case 2:
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.DESTROY_EYE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        break;
                }

                // 检查 Curios 槽位中是否有 Mega_uiorder
                CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                    curiosInventory.getStacksHandler("bracelet").ifPresent(slotInventory -> {
                        for (int i = 0; i < slotInventory.getSlots(); i++) {
                            IDynamicStackHandler curioStack = slotInventory.getStacks();
                            ItemStack stack = curioStack.getStackInSlot(i);
                            if (stack.getItem() instanceof Mega_uiorder) {
                                // 触发玩家动画（客户端逻辑）
                                if (!level.isClientSide) {
                                    // 发送网络消息到客户端触发动画
                                    PacketHandler.sendToServer(new PlayerAnimationPacket(player.getUUID(), NECROM_HENSHIN));
                                }

                                // 延迟60 tick后移除 Necrom_eye 并改变 Mega_uiorder 模型
                                if (level instanceof ServerLevel serverLevels) {
                                    serverLevels.getServer().tell(new TickTask(60, () -> {
                                        if (!player.isDeadOrDying()) {
                                            // 移除 Necrom_eye
                                            ItemStack necromEyeStack = player.getItemInHand(hand);
                                            if (!necromEyeStack.isEmpty() && necromEyeStack.getItem() instanceof Necrom_eye) {
                                                necromEyeStack.shrink(1);
                                            }

                                            // 改变 Mega_uiorder 模型为 NECROM_EYE 模型变种
                                            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventorys -> {
                                                curiosInventorys.getStacksHandler("bracelet").ifPresent(slotInventorys -> {
                                                    for (int j = 0; j < slotInventorys.getSlots(); j++) {
                                                        ItemStack curioStacks = slotInventorys.getStacks().getStackInSlot(j);
                                                        if (curioStacks.getItem() instanceof Mega_uiorder) {
                                                            // 切换到 NECROM_EYE 模式
                                                            ((Mega_uiorder) curioStacks.getItem()).switchMode(curioStacks, Mega_uiorder.Mode.NECROM_EYE);

                                                            // 添加 necrom_eye NBT
                                                            CompoundTag tag = curioStacks.getOrCreateTag();
                                                            tag.putBoolean("necrom_eye", true);
                                                            curioStacks.setTag(tag);

                                                            break;
                                                        }
                                                    }
                                                });
                                            });

                                            // 播放 YES_SIR 音效
                                            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.YES_SIR.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                                            // 生成 KnecromghostEntity
                                            KnecromghostEntity knecromghostEntity = new KnecromghostEntity(ModEntityTypes.KNECROMGHOST.get(), serverLevel);
                                            knecromghostEntity.setPos(player.getX(), player.getY(), player.getZ());
                                            serverLevel.addFreshEntity(knecromghostEntity);
                                        }
                                    }));
                                }
                            }
                        }
                    });
                });


                // 更新玩家最后一次播放音效的时间
                player.getPersistentData().putLong("lastPlayedSound", currentTime);
            }
        } else {
            // 提示玩家冷却时间未结束
            player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (INTERVAL - (currentTime - lastPlayed)) / 20 + " 秒"), true);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "start", 20, state -> PlayState.STOP)
                .triggerableAnim("start", START)
                // We've marked the "box_open" animation as being triggerable from the server
                .setSoundKeyframeHandler(state -> {
                    // Use helper method to avoid client-code in common class
                    Player player = ClientUtils.getClientPlayer();

                    if (player != null)
                        player.playSound(ModBossSounds.STAND_BY_NECROM.get(), 1, 1);
                }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}


