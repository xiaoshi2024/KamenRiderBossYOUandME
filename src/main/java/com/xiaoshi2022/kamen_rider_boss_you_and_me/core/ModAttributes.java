package com.xiaoshi2022.kamen_rider_boss_you_and_me.core;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "kamen_rider_boss_you_and_me");

    public static final RegistryObject<Attribute> CUSTOM_ATTACK_DAMAGE = ATTRIBUTES.register(
            "generic.attack_damage",
            () -> new RangedAttribute("attribute.name.generic.attack_damage", 5.0D, 0.0D, 2048.0D)
                    .setSyncable(true)
    );
}