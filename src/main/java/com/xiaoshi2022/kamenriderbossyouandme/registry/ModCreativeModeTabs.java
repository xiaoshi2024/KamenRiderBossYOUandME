package com.xiaoshi2022.kamenriderbossyouandme.registry;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "kamenriderbossyouandme" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KamenRiderBossYOUandME.MODID);

    // 创建变身系统选项卡
    public static final CreativeModeTab HENSHIN_TAB = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.kamenriderbossyouandme.henshin"))
            .icon(() -> ModItems.GENESIS_DRIVER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                // 添加创世纪驱动器
                output.accept(ModItems.GENESIS_DRIVER.get());
                // 添加其他变身相关物品
            })
            .build();

    // 注册选项卡
    static {
        CREATIVE_MODE_TABS.register("henshin", () -> HENSHIN_TAB);
    }
}