package com.xiaoshi2022.kamenriderbossyouandme;

import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.BYCurioRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.riders.driver.BrainConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = KamenRiderBossYOUandME.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = KamenRiderBossYOUandME.MODID, value = Dist.CLIENT)
public class KamenRiderBossYOUandMEClient {
    public KamenRiderBossYOUandMEClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

        BrainConfig.init();

        // 注册Curios相关内容
        CuriosRendererRegistry.register(ModItems.GENESIS_DRIVER.get(), () -> new BYCurioRenderer());
        CuriosRendererRegistry.register(ModItems.BRAIN_DRIVER.get(), () -> new BYCurioRenderer());

        // Some client setup code
        KamenRiderBossYOUandME.LOGGER.info("HELLO FROM CLIENT SETUP");
        KamenRiderBossYOUandME.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
