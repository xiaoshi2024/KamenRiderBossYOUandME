package com.xiaoshi2022.kamenriderbossyouandme.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME.MODID;

public class ModBossSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);

    // 锁种音效变身
    public static final Supplier<SoundEvent> LOCKOFF = registerSoundEvent("lockoff");
    public static final Supplier<SoundEvent> LOCKONS = registerSoundEvent("lockons");
    public static final Supplier<SoundEvent> BANANABY = registerSoundEvent("bananaby");
    public static final Supplier<SoundEvent> BANANAARMS = registerSoundEvent("bananaarms");
    public static final Supplier<SoundEvent> LEMON_TICK = registerSoundEvent("lemon_tick");
    public static final Supplier<SoundEvent> MELONX_ARMS = registerSoundEvent("melonx_arms");
    public static final Supplier<SoundEvent> CHERRY_ARMS = registerSoundEvent("cherry_amrs");
    public static final Supplier<SoundEvent> PEACH_ARMS = registerSoundEvent("peach_arms");
    public static final Supplier<SoundEvent> PEACH_ENERGY = registerSoundEvent("peach_energy");

    public static final Supplier<SoundEvent> DARK_KIVAS = registerSoundEvent("dark_kivas");
    public static final Supplier<SoundEvent> DRAK_KIVA_DISASSEMBLY = registerSoundEvent("drak_kiva_disassembly");

    public static final Supplier<SoundEvent> ORANGE = registerSoundEvent("orange");
    public static final Supplier<SoundEvent> ORANGEBY = registerSoundEvent("orangeby");
    public static final Supplier<SoundEvent> JIMBAR_LEMON = registerSoundEvent("jimbar_lemon");

    public static final Supplier<SoundEvent> LEMON_ENERGY = registerSoundEvent("lemon_energy");
    public static final Supplier<SoundEvent> LEMON_BARON = registerSoundEvent("lemon_baron");
    public static final Supplier<SoundEvent> LEMON_LOCKONBY = registerSoundEvent("lemon_lockonby");
    public static final Supplier<SoundEvent> XIUJC = registerSoundEvent("xiujc");

    public static final Supplier<SoundEvent> BAT = registerSoundEvent("bat");
    public static final Supplier<SoundEvent> EVILR = registerSoundEvent("evilr");
    public static final Supplier<SoundEvent> EVIL_BY = registerSoundEvent("evil_by");

    public static final Supplier<SoundEvent> SEAL = registerSoundEvent("seal");
    public static final Supplier<SoundEvent> STAND_BY_NECROM = registerSoundEvent("stand_by_necrom");
    public static final Supplier<SoundEvent> LOADING_EYE = registerSoundEvent("loading_eye");
    public static final Supplier<SoundEvent> DESTROY_EYE = registerSoundEvent("destroy_eye");
    public static final Supplier<SoundEvent> YES_SIR = registerSoundEvent("yes_sir");
    public static final Supplier<SoundEvent> LOGIN_BY = registerSoundEvent("login_by");
    public static final Supplier<SoundEvent> EYE_DROP = registerSoundEvent("eye_drop");
    public static final Supplier<SoundEvent> SPLIT = registerSoundEvent("split");

    public static final Supplier<SoundEvent> BRAINRIDER = registerSoundEvent("brainrider");

    public static final Supplier<SoundEvent> ERASE = registerSoundEvent("erase");
    public static final Supplier<SoundEvent> NOX_A = registerSoundEvent("nox_a");
    public static final Supplier<SoundEvent> NOX_B = registerSoundEvent("nox_b");
    public static final Supplier<SoundEvent> NOX_C = registerSoundEvent("nox_c");

    public static final Supplier<SoundEvent> QUEENBE_COM = registerSoundEvent("queenbe_com");
    public static final Supplier<SoundEvent> QUEENBE_BY = registerSoundEvent("queenbe_by");
    public static final Supplier<SoundEvent> QUNEN_BEE = registerSoundEvent("qunen_bee");

    public static final Supplier<SoundEvent> DARK_GHOST = registerSoundEvent("darks_ghost");
    public static final Supplier<SoundEvent> NAPOLEON_GHOST = registerSoundEvent("napoleon_ghost");

    public static final Supplier<SoundEvent> ANOTHER_DECADE_CLICK = registerSoundEvent("aidcds");
    public static final Supplier<SoundEvent> ANOTHER_ZI_O_CLICK = registerSoundEvent("another_zi_o_click");
    public static final Supplier<SoundEvent> AIDEN_OWC = registerSoundEvent("aiden_owc");
    public static final Supplier<SoundEvent> BANANAFRUITENERGY = registerSoundEvent("bananafruit_energy");

    public static final Supplier<SoundEvent> DRAGONFRUIT_ENERGY = registerSoundEvent("dragonfruit_energy");
    public static final Supplier<SoundEvent> DRAGONFRUIT_ARMS = registerSoundEvent("dragonfruit_arms");
    
    // Build音效
    public static final Supplier<SoundEvent> HAZARD_HENSHIN = registerSoundEvent("hazard_henshin");
    public static final Supplier<SoundEvent> SUPER_BEST_MATCH = registerSoundEvent("super_best_match");
    public static final Supplier<SoundEvent> RABBIT = registerSoundEvent("rabbit");
    public static final Supplier<SoundEvent> TANK = registerSoundEvent("tank");
    public static final Supplier<SoundEvent> RT_BY = registerSoundEvent("rt_by");

    public static final Supplier<SoundEvent> RESSYA = registerSoundEvent("ressya");
    public static final Supplier<SoundEvent> KAIZOKU = registerSoundEvent("kaizoku");

    public static final Supplier<SoundEvent> BUILD_HAZARD = registerSoundEvent("build_hazard");

    // ✅ 新增音效
    public static final Supplier<SoundEvent> GD_HENSHIN = registerSoundEvent("gd_henshin");
    public static final Supplier<SoundEvent> GREAT_DRAGON = registerSoundEvent("great_dragon");
    public static final Supplier<SoundEvent> PING = registerSoundEvent("ping");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}