package com.xiaoshi2022.kamen_rider_boss_you_and_me.bloodline.effects;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.effects.GiifuDnaEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "kamen_rider_boss_you_and_me");

    public static final RegistryObject<FangBloodlineEffect> FANG_BLOODLINE =
            EFFECTS.register("fang_bloodline", FangBloodlineEffect::new);

    public static final RegistryObject<MobEffect> GIIFU_DNA =
            EFFECTS.register("giifu_dna", GiifuDnaEffect::new);
}