package com.xiaoshi2022.kamen_rider_boss_you_and_me;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.drakkivabelt.DrakKivaBeltRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.ghostdriver.GhostDriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.two_sidriver.Two_sidriverRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Tab.ModTab;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.GhostEyeTrigger;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.GiifuTrigger;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.MixedRaceTrigger;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.OverlordTrigger;
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
import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.renderer.entity.AnotherDenlinerRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.command.ModCommands;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.giifu.ModStructureProcessors;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.EntityCuriosListScreen;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Zi_os.Another_Zi_oRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.GiifuDems.GiifuDemosRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Inves.ElementaryInvesHelheimRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Storious.StoriousRender;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.baron_banana_energy.BaronBananaEnergyRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.baron_lemon_energy.BaronLemonEnergyRenderer;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.batdrakss.BatDarksRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.batstampfinish.BatStampFinishRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.cherry_energy_arrow.CherryEnergyArrowRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.dark_kiva_seal_barrier.DarkKivaSealBarrierRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.duke_knight.DukeKnightRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gamma_s.Gamma_sRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gamma_eyecon.GammaEyeconRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.ghosteye.GhostEyeRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gifftarian.GifftarianRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.giifu.GiifuHumanRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.kivat.KivatBatTwoNdRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.knecromghost.KnecromghostRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.lord_baron.LordBaronRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.melon_energy_slash.MelonEnergySlashRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.necrom_eyex.NecromEyexRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.sakura_hurricane.SakuraHurricaneRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.DarkGhostRider.DarkGhostRiderKickRenerer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.renderer.misc.DimensionalBarrierRenderer;
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
import com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension.CurseDimensionPlayerTickHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import software.bernie.geckolib.GeckoLib;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer.VillagerCuriosLayer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer.ZombieCuriosLayer;

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
    public static final OverlordTrigger OVERLORD_TRIGGER = CriteriaTriggers.register(OverlordTrigger.getInstance());
    public static final GiifuTrigger GIIFU_TRIGGER = CriteriaTriggers.register(GiifuTrigger.getInstance());
    public static final GhostEyeTrigger GHOST_EYE_TRIGGER = CriteriaTriggers.register(GhostEyeTrigger.getInstance());
    // 添加混合种族成就触发器
    public static final MixedRaceTrigger MIXED_RACE_TRIGGER = CriteriaTriggers.register(MixedRaceTrigger.getInstance());

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

        MinecraftForge.EVENT_BUS.addListener(CurseDimensionPlayerTickHandler::onPlayerTick);


        ModTab.TABS.register(modEventBus);
        ModBossSounds.REGISTRY.register(modEventBus);

        PacketHandler.registerPackets();
        
        // 移除重复的触发器注册，触发器已在静态变量定义时注册

        ModStructureProcessors.PROCESSORS.register(modEventBus);
        
        // 注册菜单类型
        ModMenus.MENUS.register(modEventBus);

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

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            // 注册MCA村民渲染层
            // 获取村民渲染器
            EntityRenderer<? extends LivingEntity> villagerRenderer = event.getRenderer(EntityType.VILLAGER);
            if (villagerRenderer instanceof LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> castVillager = 
                        (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) villagerRenderer;
                castVillager.addLayer(new VillagerCuriosLayer<>(castVillager));
            }
            
            // 注册僵尸渲染层
            EntityRenderer<? extends LivingEntity> zombieRenderer = event.getRenderer(EntityType.ZOMBIE);
            if (zombieRenderer instanceof LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> castZombie = 
                        (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) zombieRenderer;
                castZombie.addLayer(new ZombieCuriosLayer<>(castZombie));
            }
            
            // 注册僵尸村民渲染层
            EntityRenderer<? extends LivingEntity> zombieVillagerRenderer = event.getRenderer(EntityType.ZOMBIE_VILLAGER);
            if (zombieVillagerRenderer instanceof LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> castZombieVillager = 
                        (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) zombieVillagerRenderer;
                castZombieVillager.addLayer(new ZombieCuriosLayer<>(castZombieVillager));
            }
        }

        @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            PacketHandler.registerPackets();  // <-- 加这一行
            System.out.println("Client packets registered");

            EntityRenderers.register(ModEntityTypes.SEAT.get(), SeatEntityRenderer::new);

            EntityRenderers.register(ModEntityTypes.LORD_BARON.get(), LordBaronRenderer::new);
            EntityRenderers.register(ModEntityTypes.GIIFUDEMOS_ENTITY.get(), GiifuDemosRenderer::new);
            EntityRenderers.register(ModEntityTypes.STORIOUS.get(), StoriousRender::new);
            EntityRenderers.register(ModEntityTypes.GIFFTARIAN.get(), GifftarianRenderer::new);
            EntityRenderers.register(ModEntityTypes.ANOTHER_ZI_O.get(), Another_Zi_oRenderer::new);
            EntityRenderers.register(ModEntityTypes.ANOTHER_DEN_O.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Den_os.Another_Den_oRenderer::new);
            EntityRenderers.register(ModEntityTypes.ANOTHER_DECADE.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Decades.Another_DecadeRenderer::new);
            EntityRenderers.register(ModEntityTypes.INVES_HEILEHIM.get(), ElementaryInvesHelheimRenderer::new);
            EntityRenderers.register(ModEntityTypes.KIVAT_BAT_II.get(), KivatBatTwoNdRenderer::new);
            EntityRenderers.register(ModEntityTypes.GIIFU_HUMAN.get(), GiifuHumanRenderer::new);
            EntityRenderers.register(ModEntityTypes.BAT_DARKS.get(), BatDarksRenderer::new);
            EntityRenderers.register(ModEntityTypes.BAT_STAMP_FINISH.get(), BatStampFinishRenderer::new);
            EntityRenderers.register(ModEntityTypes.KNECROMGHOST.get(), KnecromghostRenderer::new);
            EntityRenderers.register(ModEntityTypes.DUKE_KNIGHT.get(), DukeKnightRenderer::new);
            EntityRenderers.register(ModEntityTypes.GHOST_EYE_ENTITY.get(), GhostEyeRenderer::new);
            EntityRenderers.register(ModEntityTypes.GAMMA_S.get(), Gamma_sRenderer::new);
            // 眼魔眼魂实体渲染器注册
            EntityRenderers.register(ModEntityTypes.GAMMA_EYECON_ENTITY.get(), GammaEyeconRenderer::new);
            EntityRenderers.register(ModEntityTypes.NECROM_EYEX.get(), NecromEyexRenderer::new);
            // 注册时劫者实体渲染器
            EntityRenderers.register(ModEntityTypes.TIME_JACKER.get(), forge.net.mca.client.render.VillagerEntityMCARenderer::new);
            EntityRenderers.register(ModEntityTypes.TIME_ROYALTY.get(), forge.net.mca.client.render.VillagerEntityMCARenderer::new);

            EntityRenderers.register(ModEntityTypes.BARON_BANANA_ENERGY.get(), BaronBananaEnergyRenderer::new);
            EntityRenderers.register(ModEntityTypes.BARON_LEMON_ENERGY.get(), BaronLemonEnergyRenderer::new);
            EntityRenderers.register(ModEntityTypes.ANOTHER_DENLINER.get(), AnotherDenlinerRenderer::new);
            EntityRenderers.register(ModEntityTypes.DARK_KIVA_SEAL_BARRIER.get(), DarkKivaSealBarrierRenderer::new);
            EntityRenderers.register(ModEntityTypes.CHERRY_ENERGY_ARROW.get(), CherryEnergyArrowRenderer::new);
            EntityRenderers.register(ModEntityTypes.MELON_ENERGY_SLASH.get(), MelonEnergySlashRenderer::new);
            // 黑暗ghost骑士踢特效实体渲染器注册
            EntityRenderers.register(ModEntityTypes.DARKGHOST_RIDER_KICK.get(), DarkGhostRiderKickRenerer::new);
            EntityRenderers.register(ModEntityTypes.DIMENSIONAL_BARRIER.get(), DimensionalBarrierRenderer::new);

            EntityRenderers.register(ModEntityTypes.SAKURA_HURRICANE.get(), SakuraHurricaneRenderer::new);

            // 注册动画工厂
            PlayerAnimationSetup.clientInit();
            // 注册Curios相关内容
            if (ModList.get().isLoaded("curios")) {
                // 注册Curios饰品渲染器
                CuriosRendererRegistry.register(ModItems.MEGA_UIORDER_ITEM.get(), () -> new GenericCurioRenderer());
                CuriosRendererRegistry.register(ModItems.SENGOKUDRIVERS_EPMTY.get(), () -> new GenericCurioRenderer());
                CuriosRendererRegistry.register(ModItems.GENESIS_DRIVER.get(), () -> new GenericCurioRenderer());
                CuriosRendererRegistry.register(ModItems.DRAK_KIVA_BELT.get(), ()->new DrakKivaBeltRenderer());
                CuriosRendererRegistry.register(ModItems.TWO_SIDRIVER.get(), ()->new Two_sidriverRenderer());
                CuriosRendererRegistry.register(ModItems.GHOST_DRIVER.get(),()->new GhostDriverRenderer());
                
                // 注册Curio菜单屏幕
                MenuScreens.register(ModMenus.ENTITY_CURIOS_LIST.get(), EntityCuriosListScreen::new);
            }
        });
    }
    }

}
