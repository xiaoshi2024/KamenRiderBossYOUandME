package com.xiaoshi2022.kamen_rider_boss_you_and_me;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Tab.ModTab;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.GiifuDems.GiifuDemosRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Inves.ElementaryInvesHelheimRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.Storious.StoriousRender;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.client.gifftarian.GifftarianRenderer;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PacketHandler;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.network.PlayerAnimationSetup;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModBossSounds;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.Items.client.Mega_uiorder_item.Mega_uiorderRenderer;
import top.theillusivec4.curios.api.client.ICurioRenderer;

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
        ModBossSounds.REGISTRY.register(modEventBus);

        PacketHandler.registerPackets();

        // 注册 Mixin
        Mixins.addConfiguration("kamen_rider_boss_you_and_me.mixins.json");
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
    public void tick(TickEvent.ServerTickEvent event) {
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
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntityTypes.GIIFUDEMOS_ENTITY.get(), GiifuDemosRenderer::new);
            EntityRenderers.register(ModEntityTypes.STORIOUS.get(), StoriousRender::new);
            EntityRenderers.register(ModEntityTypes.GIFFTARIAN.get(), GifftarianRenderer::new);
            EntityRenderers.register(ModEntityTypes.INVES_HEILEHIM.get(), ElementaryInvesHelheimRenderer::new);
//            EntityRenderers.register(ModEntityTypes.KNECROMGHOST.get(), KnecromghostRenderer::new);
            // 注册动画工厂
            PlayerAnimationSetup.clientInit();
            // 注册饰品渲染器
            CuriosRendererRegistry.register(ModItems.MEGA_UIORDER_ITEM.get(), () -> new ICurioRenderer() {
                private final Mega_uiorderRenderer renderer = new Mega_uiorderRenderer();

                @Override
                public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                    if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
                        renderer.render(stack, slotContext, matrixStack, renderLayerParent, renderTypeBuffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
                    }
                }
            });
        }
    }

}
