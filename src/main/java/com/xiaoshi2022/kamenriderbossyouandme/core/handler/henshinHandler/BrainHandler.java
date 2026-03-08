package com.xiaoshi2022.kamenriderbossyouandme.core.handler.henshinHandler;

import com.jpigeon.ridebattlelib.api.RiderManager;
import com.jpigeon.ridebattlelib.core.system.event.HenshinEvent;
import com.jpigeon.ridebattlelib.core.system.event.UnhenshinEvent;
import com.xiaoshi2022.kamenriderbossyouandme.Accessory.BrainDriver;
import com.xiaoshi2022.kamenriderbossyouandme.Config;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderIds;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

@EventBusSubscriber(modid = MODID)
public class BrainHandler {
    
    @SubscribeEvent
    public static void onHenshin(HenshinEvent.Pre event) {
        Player player = event.getPlayer();
        ResourceLocation riderId = event.getRiderId();
        
        if (riderId.equals(RiderIds.BRAIN_ID)) {
            handleBrainHenshin(player);
        }
    }
    
    @SubscribeEvent
    public static void onUnhenshin(UnhenshinEvent.Post event) {
        Player player = event.getPlayer();
        ResourceLocation riderId = event.getRiderId();
        
        if (riderId.equals(RiderIds.BRAIN_ID)) {
            handleBrainUnhenshin(player);
        }
    }
    
    private static void handleBrainHenshin(Player player) {
        // 播放变身音效
        playSound(player, ModBossSounds.BRAINRIDER.get());
        // 获取腰带（从饰品槽位）
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BrainDriver).ifPresent(slotResult -> {
            var beltStack = slotResult.stack();
            BrainDriver belt = (BrainDriver) beltStack.getItem();
            // 标记状态并触发动画
            belt.setHenshin(beltStack, true);
            belt.setModeAndTriggerHenshin(player, beltStack, BrainDriver.BeltMode.BRAIN);
            belt.setShowing(beltStack, false);
            belt.setActive(beltStack, true);
        });
        
        // 延迟后完成变身
        RiderManager.scheduleTicks(60, () -> {
            RiderManager.completeHenshin(player);
        });
    }
    
    private static void handleBrainUnhenshin(Player player) {
        // 播放解除变身音效
        playSound(player, ModBossSounds.LOCKOFF.get());

        // 获取腰带（从饰品槽位）
        CurioUtils.findFirstCurio(player, stack -> stack.getItem() instanceof BrainDriver).ifPresent(slotResult -> {
            var beltStack = slotResult.stack();
            BrainDriver belt = (BrainDriver) beltStack.getItem();
            // 重置腰带状态
            belt.setHenshin(beltStack, false);
            belt.setShowing(beltStack, true);
            belt.setActive(beltStack, false);
            
            // 开始解除变身动画
            belt.startReleaseAnimation(player, beltStack);
        });
    }

    public static void playSound(Player player, SoundEvent soundEvent) {
        RiderManager.playPublicSound(player, soundEvent, ((float) Config.RIDER_SOUNDS_VOLUME.get() / 100));
    }
}
