package com.xiaoshi2022.kamenriderbossyouandme.event;

import com.xiaoshi2022.kamenriderbossyouandme.Accessory.AbstractRiderBelt;
import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.util.CurioUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class BeltDurabilityEventHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        // 检查死亡实体是否为玩家
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 在服务端处理
        if (player.level().isClientSide) {
            return;
        }

        // 查找玩家装备的腰带
        Optional<SlotResult> beltSlotOpt = CurioUtils.findFirstCurio(player,
                stack -> stack != null && stack.getItem() instanceof AbstractRiderBelt);

        if (beltSlotOpt.isPresent()) {
            SlotResult slotResult = beltSlotOpt.get();
            ItemStack beltStack = slotResult.stack();
            AbstractRiderBelt belt = (AbstractRiderBelt) beltStack.getItem();

            // 扣除耐久（相当于一次死亡扣除25点耐久，4次死亡后达到100点）
            boolean shouldRemove = belt.damageBelt(beltStack, 25, player);

            // 获取当前耐久值
            int currentDamage = beltStack.getDamageValue();
            int maxDamage = beltStack.getMaxDamage();
            int remainingUses = (maxDamage - currentDamage + 24) / 25; // 向上取整

            // 发送提示消息
            if (player instanceof ServerPlayer serverPlayer) {
                if (shouldRemove) {
                    // 腰带损坏，移除
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.belt.destroyed", beltStack.getDisplayName())
                    );

                    // 从 curios 槽位中移除腰带
                    CurioUtils.forceUnequipBelt(serverPlayer, slotResult.slotContext());
                } else {
                    // 发送剩余使用次数提示
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.belt.damage",
                                    beltStack.getDisplayName(),
                                    remainingUses)
                    );
                }
            }
        }
    }
}