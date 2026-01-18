package com.xiaoshi2022.kamen_rider_boss_you_and_me;


import com.xiaoshi2022.kamen_rider_boss_you_and_me.Tab.ModTab;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.*;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.BloodlineManager;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects.ModEffects;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.command.ModCommands;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.common.giifu.ModStructureProcessors;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.core.ModAttributes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.Accessory.BrainDriver;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.renderer.NoxSpecialRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.renderer.misc.DimensionalBarrierRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.HelheimVineHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.KeybindHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.KivatItemTossHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.CherrySigurdAbilityHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.DarkGaimJinbaLemonHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.event.Superpower.TyrantAbilityHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.playesani.PlayerAnimationSetup;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.particle.DarkBatParticle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.particle.LemonsliceParticle;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.*;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.villagers.VillagerRegistry;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.worldgen.dimension.CurseDimensionPlayerTickHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import software.bernie.geckolib.GeckoLib;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod("kamen_rider_boss_you_and_me")
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
    // 添加Roidmude成就触发器
    public static final com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.RoidmudeTrigger ROIDMUDE_TRIGGER = CriteriaTriggers.register(com.xiaoshi2022.kamen_rider_boss_you_and_me.advancement.RoidmudeTrigger.getInstance());

    public kamen_rider_boss_you_and_me()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册我们感兴趣的服务器和其他游戏活动
        MinecraftForge.EVENT_BUS.register(kamen_rider_boss_you_and_me.class);


        MinecraftForge.EVENT_BUS.register(KivatItemTossHandler.class);

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        
        // 注册村民职业相关内容
        VillagerRegistry.register(modEventBus);

        // 注册HelheimVineHandler
        MinecraftForge.EVENT_BUS.register(new HelheimVineHandler());
        //注册实体
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(DarkGaimJinbaLemonHandler.class);

        

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
        // 导入所有客户端专属类
        private static final net.minecraft.client.renderer.entity.EntityRenderers EntityRenderers = null;
        private static final top.theillusivec4.curios.api.client.CuriosRendererRegistry CuriosRendererRegistry = null;
        private static final net.minecraft.client.gui.screens.MenuScreens MenuScreens = null;

        @SubscribeEvent
        public static void registerRenderers(final net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.BANANAS_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Bananas.BananasRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.LEMONX_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Lemonx.LemoxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.MELONSX_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.MelonSX.melonsxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.CHERRYX_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Cherryx.cherryxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.PEACHX_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.Peachx.PeachxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.ORANGELSX_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.orange_lemons.OrangelsxRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.DRAGONFRUITX_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.dragon.dragonfruitRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.THRONE_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.kivas.thronex.ThroneBlockRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.GIIFU_SLEEPING_STATE_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.client.giifu.Giifs.GiifuSleepingStateBlockRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterParticleProviders(net.minecraftforge.client.event.RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ParticleTypesRegistry.LEMONSLICE.get(), LemonsliceParticle.Provider::new);
            event.registerSpriteSet(ParticleTypesRegistry.MLONSLICE.get(), LemonsliceParticle.Provider::new);
            event.registerSpriteSet(ParticleTypesRegistry.CHERRYSLICE.get(), LemonsliceParticle.Provider::new);
            event.registerSpriteSet(ParticleTypesRegistry.PEACHSLICE.get(), LemonsliceParticle.Provider::new);
            event.registerSpriteSet(ParticleTypesRegistry.DRAGONLICE.get(), LemonsliceParticle.Provider::new);
            // 修改这一行，使用新的DarkBatParticle.Provider
            event.registerSpriteSet(ParticleTypesRegistry.DARK_BAT.get(), DarkBatParticle.Provider::new);
        }

        @SubscribeEvent
        public static void addLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
            // 注册MCA村民渲染层
            // 获取村民渲染器
            net.minecraft.client.renderer.entity.EntityRenderer<? extends LivingEntity> villagerRenderer = event.getRenderer(EntityType.VILLAGER);
            if (villagerRenderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                net.minecraft.client.renderer.entity.LivingEntityRenderer<LivingEntity, net.minecraft.client.model.EntityModel<LivingEntity>> castVillager = 
                        (net.minecraft.client.renderer.entity.LivingEntityRenderer<LivingEntity, net.minecraft.client.model.EntityModel<LivingEntity>>) villagerRenderer;
                castVillager.addLayer(new com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer.VillagerCuriosLayer<>(castVillager));
            }
            //注册怪渲染器
            net.minecraft.client.renderer.entity.EntityRenderer<? extends LivingEntity> EliteMonsterRenderer = event.getRenderer(ModEntityTypes.ELITE_MONSTER_NPC.get());
            if (EliteMonsterRenderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                net.minecraft.client.renderer.entity.LivingEntityRenderer<LivingEntity, net.minecraft.client.model.EntityModel<LivingEntity>> castEmonster =
                        (net.minecraft.client.renderer.entity.LivingEntityRenderer<LivingEntity, net.minecraft.client.model.EntityModel<LivingEntity>>) EliteMonsterRenderer;
                castEmonster.addLayer(new com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer.EliteMonsterCuriosLayer<>(castEmonster));
            }
            
            // 注册僵尸渲染层
            net.minecraft.client.renderer.entity.EntityRenderer<? extends LivingEntity> zombieRenderer = event.getRenderer(EntityType.ZOMBIE);
            if (zombieRenderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                net.minecraft.client.renderer.entity.LivingEntityRenderer<LivingEntity, net.minecraft.client.model.EntityModel<LivingEntity>> castZombie = 
                        (net.minecraft.client.renderer.entity.LivingEntityRenderer<LivingEntity, net.minecraft.client.model.EntityModel<LivingEntity>>) zombieRenderer;
                castZombie.addLayer(new com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer.ZombieCuriosLayer<>(castZombie));
            }
            
            // 注册僵尸村民渲染层
            net.minecraft.client.renderer.entity.EntityRenderer<? extends LivingEntity> zombieVillagerRenderer = event.getRenderer(EntityType.ZOMBIE_VILLAGER);
            if (zombieVillagerRenderer instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?>) {
                @SuppressWarnings("unchecked")
                net.minecraft.client.renderer.entity.LivingEntityRenderer<LivingEntity, net.minecraft.client.model.EntityModel<LivingEntity>> castZombieVillager = 
                        (net.minecraft.client.renderer.entity.LivingEntityRenderer<LivingEntity, net.minecraft.client.model.EntityModel<LivingEntity>>) zombieVillagerRenderer;
                castZombieVillager.addLayer(new com.xiaoshi2022.kamen_rider_boss_you_and_me.client.layer.ZombieCuriosLayer<>(castZombieVillager));
            }
        }

        @SubscribeEvent
    public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            PacketHandler.registerPackets();  // <-- 加这一行
            System.out.println("Client packets registered");

            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.SEAT.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.block.kivas.entity.SeatEntityRenderer::new);

            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.LORD_BARON.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.lord_baron.LordBaronRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.GIIFUDEMOS_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.GiifuDems.GiifuDemosRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.STORIOUS.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Storious.StoriousRender::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.GIFFTARIAN.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gifftarian.GifftarianRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.ANOTHER_ZI_O.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Zi_os.Another_Zi_oRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.ANOTHER_DEN_O.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Den_os.Another_Den_oRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.ANOTHER_DECADE.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Another_Decades.Another_DecadeRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.INVES_HEILEHIM.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Inves.ElementaryInvesHelheimRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.KIVAT_BAT_II.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.kivat.KivatBatTwoNdRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.GIIFU_HUMAN.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.giifu.GiifuHumanRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.BAT_DARKS.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.batdrakss.BatDarksRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.BAT_STAMP_FINISH.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.batstampfinish.BatStampFinishRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.KNECROMGHOST.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.knecromghost.KnecromghostRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.DUKE_KNIGHT.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.duke_knight.DukeKnightRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.BRAIN_ROIDMUDE.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.BrainRoidmude.BrainRoidmudeRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.GHOST_EYE_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.ghosteye.GhostEyeRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.GAMMA_S.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gamma_s.Gamma_sRenderer::new);
            // 眼魔眼魂实体渲染器注册
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.GAMMA_EYECON_ENTITY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gamma_eyecon.GammaEyeconRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.NECROM_EYEX.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.necrom_eyex.NecromEyexRenderer::new);
            // 注册时劫者实体渲染器
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.TIME_JACKER.get(), forge.net.mca.client.render.VillagerEntityMCARenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.TIME_ROYALTY.get(), forge.net.mca.client.render.VillagerEntityMCARenderer::new);

            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.BARON_BANANA_ENERGY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.baron_banana_energy.BaronBananaEnergyRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.BARON_LEMON_ENERGY.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.baron_lemon_energy.BaronLemonEnergyRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.ANOTHER_DENLINER.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.client.renderer.entity.AnotherDenlinerRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.DARK_KIVA_SEAL_BARRIER.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.dark_kiva_seal_barrier.DarkKivaSealBarrierRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.CHERRY_ENERGY_ARROW.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.cherry_energy_arrow.CherryEnergyArrowRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.MELON_ENERGY_SLASH.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.melon_energy_slash.MelonEnergySlashRenderer::new);
            // 黑暗ghost骑士踢特效实体渲染器注册
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.DARKGHOST_RIDER_KICK.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.DarkGhostRider.DarkGhostRiderKickRenerer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.DIMENSIONAL_BARRIER.get(), DimensionalBarrierRenderer::new);

            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.SAKURA_HURRICANE.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.sakura_hurricane.SakuraHurricaneRenderer::new);

            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.ELITE_MONSTER_NPC.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.EliteMonster.EliteMonsterRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(ModEntityTypes.NOX_SPECIAL.get(), NoxSpecialRenderer::new);

            // 注册动画工厂
            PlayerAnimationSetup.clientInit();
            // 注册Curios相关内容
            if (ModList.get().isLoaded("curios")) {
                // 注册Curios饰品渲染器
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.MEGA_UIORDER_ITEM.get(), () -> new com.xiaoshi2022.kamen_rider_boss_you_and_me.client.GenericCurioRenderer());
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.SENGOKUDRIVERS_EPMTY.get(), () -> new com.xiaoshi2022.kamen_rider_boss_you_and_me.client.GenericCurioRenderer());
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.GENESIS_DRIVER.get(), () -> new com.xiaoshi2022.kamen_rider_boss_you_and_me.client.GenericCurioRenderer());
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.DRAK_KIVA_BELT.get(), ()->new com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.drakkivabelt.DrakKivaBeltRenderer());
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.TWO_SIDRIVER.get(), ()->new com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.two_sidriver.Two_sidriverRenderer());
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.GHOST_DRIVER.get(),()->new com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.ghostdriver.GhostDriverRenderer());
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.BRAIN_DRIVER.get(),()->new com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.braindriver.BrainDriverRenderer());
                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.KNIGHT_INVOKER_BUCKLE.get(),()-> new com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.knightinvoker.KnightInvokerBuckleRenderer());

                top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(ModItems.BRAIN_EYEGLASS.get(),()->new com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.BrainEyeglss.BrainEyeglassRenderer());

                // 注册Curio菜单屏幕
                net.minecraft.client.gui.screens.MenuScreens.register(ModMenus.ENTITY_CURIOS_LIST.get(), com.xiaoshi2022.kamen_rider_boss_you_and_me.curio.EntityCuriosListScreen::new);
            }
        });
    }
    }

}
