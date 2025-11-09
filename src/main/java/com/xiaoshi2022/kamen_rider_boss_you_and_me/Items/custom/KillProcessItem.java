package com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.custom;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.KillProcess.KillProcessRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.TimeJackerEntity;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.ITeleporter;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class KillProcessItem extends Item implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation START = RawAnimation.begin().thenPlay("start");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public KillProcessItem(Item.Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new KillProcessRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Trigger start animation on both client and server
        triggerStartAnimation();
        
        if (level.isClientSide) {
            // Client side only plays animation
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        // Trigger function on server side
        triggerKillProcessEffect((ServerPlayer) player, level);
        
        // Send message using translation key
        player.sendSystemMessage(Component.translatable("item.kamen_rider_boss_you_and_me.kill_process.activated"));

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private void triggerKillProcessEffect(ServerPlayer player, Level level) {
        // Detect all players within 10 blocks radius
        double radius = 10.0;
        AABB searchBox = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox);
        boolean foundGenesisDriver = false;

        for (Player nearbyPlayer : nearbyPlayers) {
            if (nearbyPlayer == player) continue;

            // Check if player is equipped with Genesis Driver (using the same pattern as other successful code in the project)
            Optional<SlotResult> beltSlot = CuriosApi.getCuriosInventory(nearbyPlayer)
                    .resolve().flatMap(inv -> inv.findFirstCurio(stack -> stack.getItem() instanceof Genesis_driver));

            if (beltSlot.isPresent()) {
                ItemStack beltStack = beltSlot.get().stack();
                Genesis_driver belt = (Genesis_driver) beltStack.getItem();

                // Check if belt has a lock seed (not default mode) - this is more reliable than checking transformation state
                if (belt.getMode(beltStack) != Genesis_driver.BeltMode.DEFAULT) {
                    foundGenesisDriver = true;
                    
                    // Create explosion at player's waist position
                    Vec3 waistPosition = new Vec3(
                            nearbyPlayer.getX(),
                            nearbyPlayer.getY() + 0.9,
                            nearbyPlayer.getZ()
                    );
                    
                    // Create explosion that doesn't break blocks
                    level.explode(null, waistPosition.x, waistPosition.y, waistPosition.z, 1.0F, Level.ExplosionInteraction.NONE);
                    
                    // Play explosion sound
                    level.playSound(null, waistPosition.x, waistPosition.y, waistPosition.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // Add particle effects
                    if (level instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 20; i++) {
                            serverLevel.sendParticles(
                                    ParticleTypes.EXPLOSION,
                                    waistPosition.x + (level.random.nextDouble() - 0.5) * 2.0,
                                    waistPosition.y + level.random.nextDouble() * 2.0,
                                    waistPosition.z + (level.random.nextDouble() - 0.5) * 2.0,
                                    1, 0, 0, 0, 0
                            );
                        }
                    }
                    
                    // Force transformation to cancel
                    belt.setHenshin(beltStack, false);
                    belt.setActive(beltStack, false);
                    
                    // Remove all armor and clear transformation state
                    removeAllArmor(nearbyPlayer);
                    
                    // Check belt mode and drop the corresponding lock seed
                    Genesis_driver.BeltMode mode = belt.getMode(beltStack);
                    if (mode != Genesis_driver.BeltMode.DEFAULT) {
                        // Create corresponding lock seed item
                        ItemStack lockSeed = null;
                        
                        switch (mode) {
                            case LEMON:
                                lockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.LEMON_ENERGY.get());
                                break;
                            case MELON:
                                lockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.MELON.get());
                                break;
                            case CHERRY:
                                lockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.CHERYY.get());
                                break;
                            case PEACH:
                                lockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.PEACH_ENERGY.get());
                                break;
                            case DRAGONFRUIT:
                                lockSeed = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.DRAGONFRUIT.get());
                                break;
                        }
                        
                        // Always drop the lock seed to the ground
                        nearbyPlayer.spawnAtLocation(lockSeed, 0.5f);
                        
                        // Reset belt mode to default
                        belt.setMode(beltStack, Genesis_driver.BeltMode.DEFAULT);
                    }
                    
                    // Add cooldown to the belt (via NBT)
                    beltStack.getOrCreateTag().putLong("cooldownUntil", level.getGameTime() + 200); // 10 seconds cooldown
                    
                    nearbyPlayer.sendSystemMessage(Component.translatable("item.kamen_rider_boss_you_and_me.kill_process.disabled"));
                }
            }
        }

        if (!foundGenesisDriver) {
            player.sendSystemMessage(Component.translatable("item.kamen_rider_boss_you_and_me.kill_process.not_found"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            // Play different animations based on state
            if (isStarting) {
                state.getController().setAnimation(START);
                // Reset state after animation completes
                if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    isStarting = false;
                    state.getController().setAnimation(IDLE);
                }
            } else {
                state.getController().setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    // Field for syncing animation state
    private boolean isStarting = false;
    
    // Method to trigger start animation
    public void triggerStartAnimation() {
        isStarting = true;
    }
    
    /**
     * Removes all armor from the player and clears transformation state
     * Called when a player's transformation is canceled
     */
    private void removeAllArmor(Player player) {
        // Clear all armor slots
        for (EquipmentSlot slot : new EquipmentSlot[]{
            EquipmentSlot.HEAD, 
            EquipmentSlot.CHEST, 
            EquipmentSlot.LEGS, 
            EquipmentSlot.FEET
        }) {
            if (!player.getItemBySlot(slot).isEmpty()) {
                // Drop the item
                player.spawnAtLocation(player.getItemBySlot(slot));
                player.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        
        // Clear transformation state marker
        player.getPersistentData().remove("is_transformed_ridder");
    }
}