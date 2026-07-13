package com.xiaoshi2022.kamenriderbossyouandme;

import com.xiaoshi2022.kamenriderbossyouandme.block.client.DragonfruitBlockRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.BYCurioRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.client.renderer.entity.FusionEffectRenderer;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBlockEntities;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModEntitys;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
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
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DRAGONFRUITX_ENTITY.get(),
                (BlockEntityRendererProvider.Context context) -> new DragonfruitBlockRenderer(context));
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // 注册Curios相关内容
        CuriosRendererRegistry.register(ModItems.GENESIS_DRIVER.get(), () -> new BYCurioRenderer());
        CuriosRendererRegistry.register(ModItems.BRAIN_DRIVER.get(), () -> new BYCurioRenderer());
        CuriosRendererRegistry.register(ModItems.BUILD_DRIVER.get(), () -> new BYCurioRenderer());

        // 注册融合实体渲染器
        EntityRenderers.register(
                ModEntitys.FUSION_EFFECT.get(),
                FusionEffectRenderer::new
        );

        // Some client setup code
        KamenRiderBossYOUandME.LOGGER.info("HELLO FROM CLIENT SETUP");
        KamenRiderBossYOUandME.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
