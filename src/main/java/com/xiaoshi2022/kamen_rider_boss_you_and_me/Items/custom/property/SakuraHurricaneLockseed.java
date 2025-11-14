package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom.property;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.SakuraHurricaneLockseed.SakuraHurricaneLockseedRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.sengokudrivers_epmty;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.EntitySakuraHurricane;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.Consumer;

public class SakuraHurricaneLockseed extends Item implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SakuraHurricaneLockseed(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SakuraHurricaneLockseedRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new SakuraHurricaneLockseedRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 如果是在服务器端，并且玩家正在骑乘摩托车，右键点击会让玩家下马并将摩托车变回锁种
        if (!level.isClientSide && player.isPassenger()) {
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof EntitySakuraHurricane) {
                // 让玩家下马
                player.stopRiding();
                
                // 将摩托车变回锁种（移除实体并给予玩家锁种）
                vehicle.remove(Entity.RemovalReason.DISCARDED);
                
                // 播放锁种关闭音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModBossSounds.LOCKOFF.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                
                // 如果玩家物品栏已满，则将锁种掉落在地上
                if (!player.addItem(new ItemStack(this))) {
                    level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, 
                            player.getX(), player.getY(), player.getZ(), 
                            new ItemStack(this)));
                }
                
                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        }

        // 检查玩家是否装备了创世纪驱动器或战极驱动器
        Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve().flatMap(
                curios -> curios.findFirstCurio(item -> item.getItem() instanceof Genesis_driver || item.getItem() instanceof sengokudrivers_epmty)
        );

        if (beltOptional.isPresent()) {
            // 如果玩家装备了创世纪驱动器
            if (!stack.getOrCreateTag().contains("first_click")) {
                // 第一次右键点击
                stack.getOrCreateTag().putBoolean("first_click", true);

                // 播放open动画
                if (level instanceof ServerLevel serverLevel) {
                    triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "open");
                }

                // 播放音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.OPENDLOCK.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                player.displayClientMessage(Component.literal("再次点击变身为摩托车！"), true);
                return InteractionResultHolder.success(stack);
            } else {
                // 第二次右键点击 - 变身为摩托车
                if (!level.isClientSide) {
                    // 消耗锁种
                    stack.shrink(1);

                    // 在玩家位置生成摩托车
                    EntitySakuraHurricane motorcycle = new EntitySakuraHurricane(
                           ModEntityTypes.SAKURA_HURRICANE.get(), level);
                    motorcycle.setPos(player.getX(), player.getY(), player.getZ());
                    level.addFreshEntity(motorcycle);
                    
                    // 让玩家骑上摩托车
                    player.startRiding(motorcycle);
                }
                return InteractionResultHolder.success(ItemStack.EMPTY);
            }
        } else {
            // 如果玩家没有装备创世纪驱动器
            player.displayClientMessage(Component.literal("需要装备GenesisDriver & SengoKuDriver才能使用这个锁种！"), true);
            return InteractionResultHolder.success(stack);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, state -> {
            // 默认播放idle动画
            state.getController().setAnimation(IDLE);
            return PlayState.CONTINUE;
        }).triggerableAnim("open", OPEN)
          .setSoundKeyframeHandler(state -> {
          }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}