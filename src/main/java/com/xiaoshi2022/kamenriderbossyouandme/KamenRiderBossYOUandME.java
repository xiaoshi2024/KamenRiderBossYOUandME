package com.xiaoshi2022.kamenriderbossyouandme;

import com.mojang.logging.LogUtils;
import com.xiaoshi2022.kamenriderbossyouandme.command.FusionCommand;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBlocks;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBlockEntities;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModCreativeModeTabs;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModEntitys;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderSkills;
import com.xiaoshi2022.kamenriderbossyouandme.riders.build.BloodConfig;
import com.xiaoshi2022.kamenriderbossyouandme.riders.driver.BrainConfig;
import com.xiaoshi2022.kamenriderbossyouandme.riders.gaim.TyrantConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(KamenRiderBossYOUandME.MODID)
public class KamenRiderBossYOUandME {
    public static final String MODID = "kamenriderbossyouandme";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KamenRiderBossYOUandME(IEventBus modEventBus, ModContainer modContainer) {
        // 注册commonSetup方法
        modEventBus.addListener(this::commonSetup);

        // 注册创造模式标签页
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        // 注册物品
        ModItems.ITEMS.register(modEventBus);
        // 注册方块
        ModBlocks.BLOCKS.register(modEventBus);
        // 注册方块实体
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        // 注册声音
        ModBossSounds.register(modEventBus);

        ModEntitys.ENTITIES.register(modEventBus);

        // 注册网络包
        modEventBus.addListener(PacketHandler::register);

        // 注册Forge事件总线
        NeoForge.EVENT_BUS.register(this);

        // 注册配置
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("KamenRiderBossYOUandME 模组初始化完成");
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RiderSkills.registerSkills();
            LOGGER.info("骑士技能注册完成");

            BrainConfig.init();
            LOGGER.info("Brain骑士配置注册完成");

            TyrantConfig.init();
            LOGGER.info("Tyrant骑士配置注册完成");

            BloodConfig.init();
            LOGGER.info("Blood骑士配置注册完成");
        });
    }

    // 注册指令
    @EventBusSubscriber(modid = MODID)
    public static class Events {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            FusionCommand.register(event.getDispatcher());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("KamenRiderBossYOUandME 服务器启动");
    }
}