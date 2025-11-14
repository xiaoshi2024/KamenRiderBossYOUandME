package com.xiaoshi2022.kamen_rider_boss_you_and_me.network;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.DrakKivaBelt;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.varibales.KRBVariables;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

// 添加新的事件处理器类，处理玩家退出事件
@Mod.EventBusSubscriber
public class PlayerLogoutHandler {
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            KRBVariables.PlayerVariables variables = player.getCapability(KRBVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .orElse(new KRBVariables.PlayerVariables());
            
            // 检查并保存黑暗Kiva腰带状态
            try {
                Optional<SlotResult> beltOptional = CuriosApi.getCuriosInventory(player).resolve().flatMap(
                        curios -> curios.findFirstCurio(item -> item.getItem() instanceof DrakKivaBelt)
                );
                variables.isDarkKivaBeltEquipped = beltOptional.isPresent();
            } catch (Exception e) {
                kamen_rider_boss_you_and_me.LOGGER.error("保存腰带状态时出错", e);
            }
        }
    }
}
