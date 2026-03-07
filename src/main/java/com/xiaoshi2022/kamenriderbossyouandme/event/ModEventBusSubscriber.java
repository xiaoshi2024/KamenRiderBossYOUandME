package com.xiaoshi2022.kamenriderbossyouandme.event;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import com.xiaoshi2022.kamenriderbossyouandme.data.CuriosDataProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID, value = Dist.CLIENT)
public class ModEventBusSubscriber {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        CuriosDataProvider.gatherData(event);
    }
}
