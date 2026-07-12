package com.xiaoshi2022.kamenriderbossyouandme;

import com.mojang.logging.LogUtils;
import com.jpigeon.ridebattlelib.server.system.HenshinSystem;
import com.xiaoshi2022.kamenriderbossyouandme.core.manager.BrainHenshinSystem;
import com.xiaoshi2022.kamenriderbossyouandme.network.PacketHandler;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModBossSounds;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModCreativeModeTabs;
import com.xiaoshi2022.kamenriderbossyouandme.registry.ModItems;
import com.xiaoshi2022.kamenriderbossyouandme.riders.RiderSkills;
import com.xiaoshi2022.kamenriderbossyouandme.riders.driver.BrainConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
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
        // 注册声音
        ModBossSounds.register(modEventBus);

        // 注册网络包
        modEventBus.addListener(PacketHandler::register);

        // 注册Forge事件总线
        NeoForge.EVENT_BUS.register(this);

        // 注册配置
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // ✅ 替换 HenshinSystem 为 BrainHenshinSystem
        replaceHenshinSystem();

        LOGGER.info("KamenRiderBossYOUandME 模组初始化完成");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            RiderSkills.registerSkills();
            LOGGER.info("骑士技能注册完成");

            BrainConfig.init();
            LOGGER.info("Brain骑士配置注册完成");
        });
    }

    /**
     * 使用反射替换 HenshinSystem 单例为 BrainHenshinSystem
     */
    private void replaceHenshinSystem() {
        try {
            // 获取 HenshinSystem 类的 INSTANCE 字段
            java.lang.reflect.Field instanceField = HenshinSystem.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);

            // 将 INSTANCE 替换为 BrainHenshinSystem 的实例
            instanceField.set(null, BrainHenshinSystem.getInstance());

            LOGGER.info("✅ 成功替换 HenshinSystem 为 BrainHenshinSystem");
        } catch (NoSuchFieldException e) {
            LOGGER.error("❌ 找不到 HenshinSystem.INSTANCE 字段，可能API版本不同", e);
            // 尝试使用其他字段名
            try {
                java.lang.reflect.Field instanceField = HenshinSystem.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                instanceField.set(null, BrainHenshinSystem.getInstance());
                LOGGER.info("✅ 成功替换 HenshinSystem 为 BrainHenshinSystem (使用字段名 'instance')");
            } catch (Exception ex) {
                LOGGER.error("❌ 替换 HenshinSystem 失败", ex);
            }
        } catch (Exception e) {
            LOGGER.error("❌ 替换 HenshinSystem 失败", e);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("KamenRiderBossYOUandME 服务器启动");
    }
}