package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBossSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, kamen_rider_boss_you_and_me.MODID);

    //锁种音效变身
    public static final RegistryObject<SoundEvent> LOCKOFF = REGISTRY.register("lockoff",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "lockoff")));
    public static final RegistryObject<SoundEvent> LOCKONS = REGISTRY.register("lockons",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "lockons")));
    public static final RegistryObject<SoundEvent> BANANABY = REGISTRY.register("bananaby",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "bananaby")));
    public static final RegistryObject<SoundEvent> BANANAARMS = REGISTRY.register("bananaarms",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "bananaarms")));
  public static final RegistryObject<SoundEvent> LEMON_TICK = REGISTRY.register("lemon_tick",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_tick")));
  public static final RegistryObject<SoundEvent> MELONX_ARMS = REGISTRY.register("melonx_arms",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "melonx_arms")));
  public static final RegistryObject<SoundEvent> CHERRY_ARMS = REGISTRY.register("cherry_amrs",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "cherry_amrs")));
  public static final RegistryObject<SoundEvent> PEACH_ARMS = REGISTRY.register("peach_arms",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "peach_arms")));
  public static final RegistryObject<SoundEvent> PEACH_ENERGY = REGISTRY.register("peach_energy",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "peach_energy")));

  public static final RegistryObject<SoundEvent> DARK_KIVAS = REGISTRY.register("dark_kivas",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "dark_kivas")));
  public static final RegistryObject<SoundEvent> DRAK_KIVA_DISASSEMBLY = REGISTRY.register("drak_kiva_disassembly",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "drak_kiva_disassembly")));

  public static final RegistryObject<SoundEvent> ORANGE = REGISTRY.register("orange",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "orange")));
  public static final RegistryObject<SoundEvent> ORANGEBY = REGISTRY.register("orangeby",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "orangeby")));
  public static final RegistryObject<SoundEvent> JIMBAR_LEMON = REGISTRY.register("jimbar_lemon",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "jimbar_lemon")));

    public static final RegistryObject<SoundEvent> LEMON_ENERGY = REGISTRY.register("lemon_energy",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_energy")));
    public static final RegistryObject<SoundEvent> LEMON_BARON = REGISTRY.register("lemon_baron",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_baron")));
    public static final RegistryObject<SoundEvent> LEMON_LOCKONBY = REGISTRY.register("lemon_lockonby",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "lemon_lockonby")));
    public static final RegistryObject<SoundEvent> XIUJC = REGISTRY.register("xiujc",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "xiujc")));

    public static final RegistryObject<SoundEvent> BAT = REGISTRY.register("bat",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "bat")));
    public static final RegistryObject<SoundEvent> EVILR = REGISTRY.register("evilr",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "evilr")));
    public static final RegistryObject<SoundEvent> EVIL_BY = REGISTRY.register("evil_by",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "evil_by")));

    public static final RegistryObject<SoundEvent> SEAL = REGISTRY.register("seal",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "seal")));
    public static final RegistryObject<SoundEvent> STAND_BY_NECROM = REGISTRY.register("stand_by_necrom",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "stand_by_necrom")));
    public static final RegistryObject<SoundEvent> LOADING_EYE = REGISTRY.register("loading_eye",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "loading_eye")));
    public static final RegistryObject<SoundEvent> DESTROY_EYE = REGISTRY.register("destroy_eye",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "destroy_eye")));
 public static final RegistryObject<SoundEvent> YES_SIR = REGISTRY.register("yes_sir",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "yes_sir")));
 public static final RegistryObject<SoundEvent> LOGIN_BY = REGISTRY.register("login_by",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "login_by")));
 public static final RegistryObject<SoundEvent> EYE_DROP = REGISTRY.register("eye_drop",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "eye_drop")));
 public static final RegistryObject<SoundEvent> SPLIT = REGISTRY.register("split",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "split")));


 public static final RegistryObject<SoundEvent> DARK_GHOST = REGISTRY.register("darks_ghost",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "darks_ghost")));

 public static final RegistryObject<SoundEvent> NAPOLEON_GHOST = REGISTRY.register("napoleon_ghost",
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "napoleon_ghost")));

    public static final RegistryObject<SoundEvent> ANOTHER_DECADE_CLICK = REGISTRY.register("aidcds",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "aidcds")));

 public static final RegistryObject<SoundEvent> ANOTHER_ZI_O_CLICK = REGISTRY.register("another_zi_o_click",
         () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me","another_zi_o_click")));
public static final RegistryObject<SoundEvent> AIDEN_OWC = REGISTRY.register("aiden_owc",
         () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me","aiden_owc")));
 public static final RegistryObject<SoundEvent> BANANAFRUITENERGY = REGISTRY.register("bananafruit_energy",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me","bananafruit_energy")));

    public static final RegistryObject<SoundEvent> DRAGONFRUIT_ENERGY = REGISTRY.register("dragonfruit_energy",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "dragonfruit_energy")));
    public static final RegistryObject<SoundEvent> DRAGONFRUIT_ARMS = REGISTRY.register("dragonfruit_arms",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "dragonfruit_arms")));
}
