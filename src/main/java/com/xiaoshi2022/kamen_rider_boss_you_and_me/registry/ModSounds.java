package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.kamen_rider_boss_you_and_me;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, kamen_rider_boss_you_and_me.MODID);

    public static final RegistryObject<SoundEvent> SEAL = REGISTRY.register("seal",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("kamen_rider_boss_you_and_me", "seal")));

}
