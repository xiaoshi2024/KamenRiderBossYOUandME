package com.xiaoshi2022.kamen_rider_boss_you_and_me.entity;

import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.GiifuDemosEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Inves.ElementaryInvesHelheim;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.Lord.LordBaronEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.StoriousEntity;
import com.xiaoshi2022.kamen_rider_boss_you_and_me.entity.custom.giifu.Gifftarian;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.Gender;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "kamen_rider_boss_you_and_me");

    public static final RegistryObject<EntityType<GiifuDemosEntity>> GIIFUDEMOS_ENTITY = registerMob("giifudemos", GiifuDemosEntity::new,
            0.6f, 1.8f, 0x1F1F1F, 0x0D0D0D);
    public static final RegistryObject<EntityType<StoriousEntity>> STORIOUS = registerMob("storious", StoriousEntity::new,
                0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    public static final RegistryObject<EntityType<Gifftarian>> GIFFTARIAN = registerMob("gifftarian", Gifftarian::new,
                0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    public static final RegistryObject<EntityType<ElementaryInvesHelheim>> INVES_HEILEHIM = registerMob("inves_heilehim", ElementaryInvesHelheim::new,
                0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
    // 确保注册名是 "lord_baron"
    public static final RegistryObject<EntityType<LordBaronEntity>> LORD_BARON = registerMob("lord_baron", LordBaronEntity::new,
            0.6f, 1.9f, 0x1F1F1F, 0x0D0D0D);
//    public static final RegistryObject<EntityType<KnecromghostEntity>> KNECROMGHOST = registerMob("knecromghost", KnecromghostEntity::new,
//            0.6f, 1.6f, 0x1F1F1F, 0x0D0D0D);

    public static <T extends Mob> RegistryObject<EntityType<T>> registerMob(String name, EntityType.EntityFactory<T> entity,
                                                                            float width, float height, int primaryEggColor, int secondaryEggColor) {
        RegistryObject<EntityType<T>> entityType = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(entity, MobCategory.CREATURE).sized(width, height).build(name));

        return entityType;
    }
}