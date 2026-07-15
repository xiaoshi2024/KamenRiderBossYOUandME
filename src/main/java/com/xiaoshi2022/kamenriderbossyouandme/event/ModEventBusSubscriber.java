package com.xiaoshi2022.kamenriderbossyouandme.event;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.core.handler.skills.BloodSkillHandler;
import com.xiaoshi2022.kamenriderbossyouandme.data.CuriosDataProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID)
public class ModEventBusSubscriber {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // Blood屏障吸收
        if (BloodSkillHandler.isBloodBarrierActive(player)) {
            float newDamage = BloodSkillHandler.handleBarrierAbsorption(player, event.getNewDamage());
            event.setNewDamage(newDamage);
        }
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        CuriosDataProvider.gatherData(event);
    }
}
