package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBossSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, kamen_rider_boss_you_and_me.MODID);

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

}
