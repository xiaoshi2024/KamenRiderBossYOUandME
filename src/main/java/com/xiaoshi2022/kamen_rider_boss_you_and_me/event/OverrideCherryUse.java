package com.xiaoshi2022.kamen_rider_boss_you_and_me.event;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.Genesis_driver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.KRBVariables;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom.cheryy;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class OverrideCherryUse {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        Level level = player.level();
        InteractionHand hand = event.getHand();

        // 1. 必须是 weapon 的 cherry
        if (!(stack.getItem() instanceof cheryy)) return;

        // 2. 阻止 weapon 自己的 use 逻辑
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        // 3. 下面照抄樱桃锁种的完整流程
        Optional<SlotResult> belt = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(inv -> inv.findFirstCurio(i -> i.getItem() instanceof Genesis_driver));

        if (belt.isPresent()) {
            // 有创世纪驱动器
            CompoundTag tag = stack.getOrCreateTag();
            if (!tag.contains("first_click")) {
                // 第一次右键
                tag.putBoolean("first_click", true);
                BlockPos above = player.blockPosition().above(2);

                // 播放open动画
                if (level instanceof ServerLevel serverLevel) {
                    if (stack.getItem() instanceof GeoItem geoItem) {
                        geoItem.triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), "controller", "open");
                    }
                }


                if (level.isEmptyBlock(above)) {
                    level.setBlock(above,
                            /* 这里放樱桃专属方块 */
                            ModBlocks.CHERRYX_BLOCK.get().defaultBlockState(),
                            3);
                    level.playSound(null, above,
                            // 这里放樱桃专属音效
                            ModSounds.CHERYYENERGY.get(),
                            SoundSource.PLAYERS, 1, 1);
                }
                player.displayClientMessage(Component.literal("再次点击装备樱桃锁种"), true);
            } else {
                // 第二次右键
                if (!level.isClientSide) {
                    // 检查玩家是否已经装备了其他锁种
                    if (com.xiaoshi2022.kamen_rider_boss_you_and_me.util.BeltUtils.hasActiveLockseed(player)) {
                        player.sendSystemMessage(Component.literal("您已经装备了其他锁种，请先解除变身！"));
                        return;
                    }
                    
                    // 播放LOCK ON音效
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModBossSounds.LEMON_LOCKONBY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    stack.shrink(1);

                    ItemStack beltStack = belt.get().stack();
                    Genesis_driver genesisDriver = (Genesis_driver) beltStack.getItem();
                    genesisDriver.setMode(beltStack, Genesis_driver.BeltMode.CHERRY);

                    // 获取PlayerVariables实例并设置状态
                    KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new KRBVariables.PlayerVariables());
                    variables.cherry_ready = true;
                    variables.cherry_ready_time = level.getGameTime();
                    variables.syncPlayerVariables(player); // 同步变量到客户端

                    // 给玩家反馈
                    player.displayClientMessage(Component.literal("樱桃锁种已装载！按变身键变身"), true);
                    
                }
            }
        } else {
            // 没有腰带，直接 fallback 到 weapon 的原始逻辑
            InteractionResultHolder<ItemStack> result = 
                    ((cheryy) stack.getItem()).use(level, player, hand);
            // 这里不 cancel，让 weapon 的 use 继续跑
            event.setCanceled(false);
        }
    }
}