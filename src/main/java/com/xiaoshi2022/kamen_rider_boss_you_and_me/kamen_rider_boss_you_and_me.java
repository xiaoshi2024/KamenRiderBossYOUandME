package com.xiaoshi2022.kamen_rider_boss_you_and_me;

import com.mojang.brigadier.CommandDispatcher;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.drakkivabelt.DrakKivaBeltRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Tab.ModTab;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.TamedKivatTrigger;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Bananas.BananasRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Cherryx.cherryxRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Lemonx.LemoxRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.MelonSX.melonsxRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Peachx.PeachxRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.dragon.dragonfruitRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.Giifs.GiifuSleepingStateBlockRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas.thronex.ThroneBlockRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.orange_lemons.OrangelsxRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.entity.SeatEntityRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.BloodlineManager;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects.ModEffects;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.GenericCurioRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.command.ModCommands;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.giifu.ModStructureProcessors;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Zi_os.Another_Zi_oRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.GiifuDems.GiifuDemosRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Inves.ElementaryInvesHelheimRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Storious.StoriousRender;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gifftarian.GifftarianRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.giifu.GiifuHumanRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.kivat.KivatBatTwoNdRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.lord_baron.LordBaronRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.GiifuHumanEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.HelheimVineHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.KeybindHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.KivatItemTossHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.CherrySigurdAbilityHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGaimJinbaLemonHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.TyrantAbilityHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.particle.DarkBatParticle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.particle.LemonsliceParticle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import software.bernie.geckolib.GeckoLib;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod("kamen_rider_boss_you_and_me")
@Mod.EventBusSubscriber(modid = "kamen_rider_boss_you_and_me")
public class kamen_rider_boss_you_and_me
{
    public static final String MODID = "kamen_rider_boss_you_and_me";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    // 在 Mod 主类或 Setup 类中添加
    public static final TamedKivatTrigger TAMED_KIVAT_TRIGGER = CriteriaTriggers.register(new TamedKivatTrigger());

    public kamen_rider_boss_you_and_me()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册我们感兴趣的服务器和其他游戏活动
        MinecraftForge.EVENT_BUS.register(kamen_rider_boss_you_and_me.class);


        MinecraftForge.EVENT_BUS.register(KivatItemTossHandler.class);

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // 注册HelheimVineHandler
        MinecraftForge.EVENT_BUS.register(new HelheimVineHandler());
        //注册实体
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(DarkGaimJinbaLemonHandler.class);

        // 注册 KeybindHandler
        MinecraftForge.EVENT_BUS.register(KeybindHandler.class);

        // 注册 CherrySigurdAbilityHandler
        MinecraftForge.EVENT_BUS.register(CherrySigurdAbilityHandler.class);

        // 注册 TyrantAbilityHandler
        MinecraftForge.EVENT_BUS.register(TyrantAbilityHandler.class);

        //初始化自定义ModAttributes
        ModAttributes.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());

        //geckolib
        GeckoLib.initialize();

        ParticleTypesRegistry.PARTICLE_TYPES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(BloodlineManager.class);

        ModEffects.EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        //注册
        ModItems.ITEMS.register(modEventBus);

        ModTab.TABS.register(modEventBus);
        ModBossSounds.REGISTRY.register(modEventBus);

        PacketHandler.registerPackets();

        ModStructureProcessors.PROCESSORS.register(modEventBus);

        // 注册 Mixin
        Mixins.addConfiguration("kamen_rider_boss_you_and_me.mixins.json");
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    /// 注册的东西
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() == 0)
                    actions.add(work);
            });
            actions.forEach(e -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }


    //您可以使用 EventBusSubscriber 自动注册带有 @SubscribeEvent 注释的类中的所有静态方法
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.BANANAS_ENTITY.get(), BananasRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.LEMONX_ENTITY.get(), LemoxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.MELONSX_ENTITY.get(), melonsxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.CHERRYX_ENTITY.get(), cherryxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.PEACHX_ENTITY.get(), PeachxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.ORANGELSX_ENTITY.get(), OrangelsxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.DRAGONFRUITX_ENTITY.get(), dragonfruitRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.THRONE_ENTITY.get(), ThroneBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.GIIFU_SLEEPING_STATE_ENTITY.get(), GiifuSleepingStateBlockRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ParticleTypesRegistry.LEMONSLICE.get(), LemonsliceParticle.Provider::new);
            event.registerSpriteSet(ParticleTypesRegistry.MLONSLICE.get(), LemonsliceParticle.Provider::new);
            event.registerSpriteSet(ParticleTypesRegistry.CHERRYSLICE.get(), LemonsliceParticle.Provider::new);
            event.registerSpriteSet(ParticleTypesRegistry.PEACHSLICE.get(), LemonsliceParticle.Provider::new);
            event.registerSpriteSet(ParticleTypesRegistry.DRAGONLICE.get(), LemonsliceParticle.Provider::new);
            // 修改这一行，使用新的DarkBatParticle.Provider
            event.registerSpriteSet(ParticleTypesRegistry.DARK_BAT.get(), DarkBatParticle.Provider::new);
        }

//        @SubscribeEvent
//        public static void addLayers(EntityRenderersEvent.AddLayers event) {
//            // 你的 Kaito 村民（或任何继承 Villager 的实体）
//            LivingEntityRenderer<?, ?> kaitoRenderer = event.getRenderer(ModEntityTypes.KAITO.get());
//            if (kaitoRenderer instanceof LivingEntityRenderer<?, ?>) {
//                // 强制转型到正确的父类
//                @SuppressWarnings("unchecked")
//                LivingEntityRenderer<Villager, VillagerModel<Villager>> cast =
//                        (LivingEntityRenderer<Villager, VillagerModel<Villager>>) kaitoRenderer;
//                cast.addLayer(new KaitoCuriosLayer(cast));
//            }
//        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            PacketHandler.registerPackets();  // <-- 加这一行
            System.out.println("Client packets registered");

            EntityRenderers.register(ModEntityTypes.SEAT.get(), SeatEntityRenderer::new);

            EntityRenderers.register(ModEntityTypes.LORD_BARON.get(), LordBaronRenderer::new);
            EntityRenderers.register(ModEntityTypes.GIIFUDEMOS_ENTITY.get(), GiifuDemosRenderer::new);
            EntityRenderers.register(ModEntityTypes.STORIOUS.get(), StoriousRender::new);
            EntityRenderers.register(ModEntityTypes.GIFFTARIAN.get(), GifftarianRenderer::new);
            EntityRenderers.register(ModEntityTypes.ANOTHER_ZI_O.get(), Another_Zi_oRenderer::new);
            EntityRenderers.register(ModEntityTypes.INVES_HEILEHIM.get(), ElementaryInvesHelheimRenderer::new);
            EntityRenderers.register(ModEntityTypes.KIVAT_BAT_II.get(), KivatBatTwoNdRenderer::new);
            EntityRenderers.register(ModEntityTypes.GIIFU_HUMAN.get(), GiifuHumanRenderer::new);

//            EntityRenderers.register(ModEntityTypes.KAITO.get(),
//                    VillagerEntityMCARenderer::new);

//            EntityRenderers.register(ModEntityTypes.KNECROMGHOST.get(), KnecromghostRenderer::new);
            // 注册动画工厂
            PlayerAnimationSetup.clientInit();
            // 注册饰品渲染器
            // 注册 Curios 饰品渲染器
            CuriosRendererRegistry.register(ModItems.MEGA_UIORDER_ITEM.get(), () -> new GenericCurioRenderer());
            CuriosRendererRegistry.register(ModItems.SENGOKUDRIVERS_EPMTY.get(), () -> new GenericCurioRenderer());
            CuriosRendererRegistry.register(ModItems.GENESIS_DRIVER.get(), () -> new GenericCurioRenderer());
            CuriosRendererRegistry.register(ModItems.DRAK_KIVA_BELT.get(), ()->new DrakKivaBeltRenderer());
        }
    }

}
