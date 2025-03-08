package com.xiaoshi2022.kamen_rider_boss_you_and_me;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Tab.ModTab;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.GiifuDems.GiifuDemosRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Inves.ElementaryInvesHelheimRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Storious.StoriousRender;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gifftarian.GifftarianRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModSounds;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import software.bernie.geckolib.GeckoLib;


@Mod("kamen_rider_boss_you_and_me")
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class kamen_rider_boss_you_and_me
{
    public static final String MODID = "kamen_rider_boss_you_and_me";

    public static final Logger LOGGER = LogManager.getLogger(MODID);


    public kamen_rider_boss_you_and_me()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册我们感兴趣的服务器和其他游戏活动
        MinecraftForge.EVENT_BUS.register(this);

        //注册实体
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        //geckolib
        GeckoLib.initialize();

        //注册
        ModItems.ITEMS.register(modEventBus);

        ModTab.TABS.register(modEventBus);
        ModSounds.REGISTRY.register(modEventBus);

        PacketHandler.registerPackets();

        // 注册 Mixin
        Mixins.addConfiguration("xiaoshi2022.mixins.json");
    }


    //您可以使用 EventBusSubscriber 自动注册带有 @SubscribeEvent 注释的类中的所有静态方法
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntityTypes.GIIFUDEMOS_ENTITY.get(), GiifuDemosRenderer::new);
            EntityRenderers.register(ModEntityTypes.STORIOUS.get(), StoriousRender::new);
            EntityRenderers.register(ModEntityTypes.GIFFTARIAN.get(), GifftarianRenderer::new);
            EntityRenderers.register(ModEntityTypes.INVES_HEILEHIM.get(), ElementaryInvesHelheimRenderer::new);
            // 注册动画工厂
            PlayerAnimationSetup.clientInit();
        }
    }

}
