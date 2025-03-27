package com.xiaoshi2022.kamen_rider_boss_you_and_me.procedures;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.util.KeyBinding.RELIEVE_KEY;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class CheckTransformAndBraceletProcedure {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide) return;

        // 检查玩家是否按下了 RELIEVE_KEY 键
        if (RELIEVE_KEY.isDown()) {
            // 检查自定义槽位是否有 MEGA_UIORDER_ITEM
            boolean hasBracelet = hasBraceletInCurio(player);
            if (hasBracelet) {
                untransformPlayer(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        // 如果玩家死亡且处于变身状态，清除盔甲并保留手环回到背包
        if (isPlayerTransformed(player)) {
            clearArmorAndReturnBraceletToInventory(player);
        }
    }

    private static boolean isPlayerTransformed(Player player) {
        // 假设通过盔甲判断玩家是否处于变身状态
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() &&
                    (stack.getItem() == ModItems.RIDERNECROM_HELMET.get() ||
                            stack.getItem() == ModItems.RIDERNECROM_CHESTPLATE.get() ||
                            stack.getItem() == ModItems.RIDERNECROM_LEGGINGS.get() ||
                            stack.getItem() == ModItems.RIDERNECROM_BOOTS.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBraceletInCurio(Player player) {
        return CuriosApi.getCuriosInventory(player).map(curiosInventory -> {
            return curiosInventory.getStacksHandler("bracelet").map(slotInventory -> {
                IDynamicStackHandler stacks = slotInventory.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack curioStack = stacks.getStackInSlot(i);
                    if (!curioStack.isEmpty() && curioStack.getItem() == ModItems.MEGA_UIORDER_ITEM.get()) {
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        }).orElse(false);
    }

    private static void untransformPlayer(Player player) {
        // 清空盔甲槽位
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
        player.getInventory().setChanged();

        // 播放解除变身音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModBossSounds.SPLIT.get(), player.getSoundSource(), 1.0F, 1.0F);

        // 提示玩家解除变身
        player.displayClientMessage(Component.literal("你按下了解除变身键，变身状态已解除！"), true);

        // 移除指定的 buff
        removeBuff(player);
    }

    private static void clearArmorAndReturnBraceletToInventory(Player player) {
        // 清空盔甲槽位
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            player.getInventory().armor.set(i, ItemStack.EMPTY);
        }
        player.getInventory().setChanged();

        // 将手环从 Curio 槽位移回背包
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            curiosInventory.getStacksHandler("bracelet").ifPresent(slotInventory -> {
                IDynamicStackHandler stacks = slotInventory.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack curioStack = stacks.getStackInSlot(i);
                    if (!curioStack.isEmpty()) {
                        if (!player.getInventory().add(curioStack)) {
                            player.drop(curioStack, false);
                        }
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                        break;
                    }
                }
            });
        });

        // 移除指定的 buff
        removeBuff(player);
    }

    private static void removeBuff(Player player) {
        // 假设你的 buff 是一个自定义的效果，例如 ModEffects.INVISIBILITY
        // 这里以 Minecraft 自带的隐身效果为例
        player.removeEffect(MobEffects.INVISIBILITY);

        // 如果你有自定义的效果，可以这样移除
        // player.removeEffect(ModEffects.YOUR_CUSTOM_EFFECT);
    }
}