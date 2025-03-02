package com.xiaoshi2022.kamen_rider_boss_you_and_me.Tab;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.GIIFUSTEAMP;
import static com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.STORIOUSMONSTERBOOK;

public class ModTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, kamen_rider_boss_you_and_me.MODID);

    public static final RegistryObject<CreativeModeTab> TEST_TAB = TABS.register("kamen_rider_boss_you_and_me_tab", () -> CreativeModeTab.builder()
            .icon(() -> GIIFUSTEAMP.get().getDefaultInstance())
            .title(Component.translatable("item group.kamen_rider_boss_you_and_me_tab"))
            .displayItems(((parameters, output) -> {
                output.accept(GIIFUSTEAMP.get());
                output.accept(STORIOUSMONSTERBOOK.get());
            })).build());
}