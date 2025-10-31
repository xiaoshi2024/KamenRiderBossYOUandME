package com.xiaoshi2022.kamen_rider_boss_you_and_me.Tab;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.*;

public class ModTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, kamen_rider_boss_you_and_me.MODID);

    public static final RegistryObject<CreativeModeTab> TEST_TAB = TABS.register("kamen_rider_boss_you_and_me_tab", () -> CreativeModeTab.builder()
            .icon(() -> GIIFUSTEAMP.get().getDefaultInstance())
            .title(Component.translatable("item group.kamen_rider_boss_you_and_me_tab"))
            .displayItems(((parameters, output) -> {
                output.accept(GIIFUSTEAMP.get());
                output.accept(AIZIOWC.get());
                output.accept(STORIOUSMONSTERBOOK.get());

            })).build());
    public static final RegistryObject<CreativeModeTab> HEIXIN_TAB = TABS.register("kamen_rider_boss_you_and_me_heixin_tab", () -> CreativeModeTab.builder()
            .icon(() -> NECROM_EYE.get().getDefaultInstance())
            .title(Component.translatable("item group.kamen_rider_boss_you_and_me_heixin_tab"))
            .displayItems(((parameters, output) -> {
                output.accept(NECROM_EYE.get());
                output.accept(MEGA_UIORDER_ITEM.get());
                output.accept(SENGOKUDRIVERS_EPMTY.get());
                output.accept(GENESIS_DRIVER.get());
                output.accept(BANANAFRUIT.get());
                output.accept(KIVAT_BAT_TWO_ND_ITEM.get());
                output.accept(TWO_SIDRIVER.get());
                output.accept(BAT_STAMP.get());
                output.accept(TWO_WEAPON.get());
                output.accept(LEMON_ENERGY.get());
                output.accept(PEACH_ENERGY.get());
                output.accept(DRAGONFRUIT.get());
                // 把橘子锁种默认设为黑暗
                ItemStack darkOrange = new ItemStack(ORANGEFRUIT.get());
                darkOrange.getOrCreateTag().putBoolean("isDarkVariant", true);
                output.accept(darkOrange);
            })).build());
    public static final RegistryObject<CreativeModeTab> KINDS_TAB = TABS.register("kamen_rider_boss_you_and_me_kinds_tab", () -> CreativeModeTab.builder()
            .icon(() -> INVES_MEAT.get().getDefaultInstance())
            .title(Component.translatable("item group.kamen_rider_boss_you_and_me_kinds_tab"))
            .displayItems(((parameters, output) -> {
                output.accept(INVES_MEAT.get());
                output.accept(GLOBALISM.get());
                output.accept(THRONE_BLOCK_ITEM.get());
                output.accept(CURSE_BLOCK_ITEM.get());
                output.accept(GIIFU_EYEBALL.get());
                output.accept(GIIFU_SLEEPING_STATE_ITEM.get());
                output.accept(BLOODLINE_FANG.get());
                output.accept(GAMMA_EYECON.get());
                output.accept(SAKURAHURRICANE_LOCKSEED.get());
            })).build());

    public static final RegistryObject<CreativeModeTab> SPAWN_EGG_TAB = TABS.register("kamen_rider_boss_you_and_me_spawn_egg_tab", () -> CreativeModeTab.builder()
            .icon(() -> LORD_BARON_SPAWN_EGG.get().getDefaultInstance())
            .title(Component.translatable("item group.kamen_rider_boss_you_and_me_spawn_egg_tab"))
            .displayItems(((parameters, output) -> {
                output.accept(LORD_BARON_SPAWN_EGG.get());
                output.accept(KIVAT_BAT_TWO_ND_SPAWN_EGG.get());
                output.accept(ELEMENTARY_INVES_HELHEIM_SPAWN_EGG.get());
                output.accept(GIIFU_HUMAN_SPAWN_EGG.get());
                output.accept(GIFFTARIAN_SPAWN_EGG.get());
                output.accept(ANOTHER_ZI_O_SPAWN_EGG.get());
                output.accept(ANOTHER_DEN_O_SPAWN_EGG.get());
                output.accept(GIIFU_DEMOS_SPAWN_EGG.get());
                output.accept(STORIOUS_SPAWN_EGG.get());
                output.accept(GAMMA_S_SPAWN_EGG.get());
            })).build());

}