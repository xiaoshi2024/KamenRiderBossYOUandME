package com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.helheimtems;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class HelhimElites {
    // 在Mod主类或专用类中定义
    public static final TagKey<EntityType<?>> HELHEIM_ELITES =
            TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("kamen_rider_boss_you_and_me", "helheim_elites"));

    public static final TagKey<EntityType<?>> HELHEIM_MINIONS =
            TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation("kamen_rider_boss_you_and_me", "helheim_minions"));
}
